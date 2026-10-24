package com.wms.service.receiving

import com.wms.dto.receiving.AddReceivingLineRequest
import com.wms.dto.receiving.CreateReceivingDocumentRequest
import com.wms.dto.receiving.ReceivingDocumentLineResponse
import com.wms.dto.receiving.ReceivingDocumentResponse
import com.wms.entity.inventory.Inventory
import com.wms.entity.movement.InventoryMovement
import com.wms.entity.movement.InventoryMovementType
import com.wms.entity.receiving.ReceivingDocument
import com.wms.entity.receiving.ReceivingDocumentLine
import com.wms.entity.receiving.ReceivingDocumentStatus
import com.wms.repository.inventory.InventoryRepository
import com.wms.repository.location.StorageLocationRepository
import com.wms.repository.movement.InventoryMovementRepository
import com.wms.repository.product.ProductRepository
import com.wms.repository.receiving.ReceivingDocumentLineRepository
import com.wms.repository.receiving.ReceivingDocumentRepository
import com.wms.repository.tenant.TenantRepository
import com.wms.repository.warehouse.WarehouseRepository
import com.wms.service.audit.AuditLogService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime

@Service
class ReceivingService(
    private val tenantRepository: TenantRepository,
    private val warehouseRepository: WarehouseRepository,
    private val productRepository: ProductRepository,
    private val locationRepository: StorageLocationRepository,
    private val documentRepository: ReceivingDocumentRepository,
    private val lineRepository: ReceivingDocumentLineRepository,
    private val inventoryRepository: InventoryRepository,
    private val movementRepository: InventoryMovementRepository,
    private val auditLogService: AuditLogService
) {

    @Transactional
    fun createDocument(
        tenantId: Long,
        warehouseId: Long,
        request: CreateReceivingDocumentRequest
    ): ReceivingDocumentResponse {
        if (!tenantRepository.existsById(tenantId)) {
            throw notFound("Компания с ID: $tenantId не найдена")
        }

        val warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow { notFound("Склад с ID: $warehouseId не найден") }

        if (warehouse.tenant.id != tenantId) {
            throw badRequest("Склад не принадлежит указанной компании")
        }

        val number = request.documentNumber.trim()

        if (documentRepository.existsByTenantIdAndDocumentNumber(tenantId, number)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Документ с номером $number уже существует"
            )
        }

        val document = documentRepository.save(
            ReceivingDocument(
                tenantId = tenantId,
                warehouse = warehouse,
                documentNumber = number,
                notes = request.notes?.trim()?.takeIf { it.isNotEmpty() }
            )
        )

        return document.toResponse()
    }

    @Transactional
    fun addLine(
        tenantId: Long,
        warehouseId: Long,
        documentId: Long,
        request: AddReceivingLineRequest
    ): ReceivingDocumentResponse {
        val document = getDocument(tenantId, warehouseId, documentId)
        ensureDraft(document)

        val product = productRepository.findById(request.productId)
            .orElseThrow { notFound("Товар с ID ${request.productId} не найден") }

        val location = locationRepository.findById(request.locationId)
            .orElseThrow { notFound("Ячейка с ID ${request.locationId} не найдена") }

        if (product.tenant.id != tenantId || location.tenantId != tenantId) {
            throw badRequest("Товар и ячейка должны принадлежать указанной компании")
        }

        if (location.zone.warehouse.id != warehouseId) {
            throw badRequest("Ячейка не принадлежит указанному складу")
        }

        if (!product.isActive || !location.isActive) {
            throw badRequest("Нельзя добавить неактивный товар или ячейку")
        }

        lineRepository.save(
            ReceivingDocumentLine(
                document = document,
                tenantId = tenantId,
                product = product,
                locationId = request.locationId,
                quantity = request.quantity
            )
        )

        return document.toResponse()
    }

    @Transactional(readOnly = true)
    fun getAll(
        tenantId: Long,
        warehouseId: Long
    ): List<ReceivingDocumentResponse> {
        validateWarehouse(tenantId, warehouseId)

        return documentRepository
            .findAllByTenantIdOrderByCreatedAtDesc(tenantId)
            .filter { it.warehouse.id == warehouseId }
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getById(
        tenantId: Long,
        warehouseId: Long,
        documentId: Long
    ): ReceivingDocumentResponse =
        getDocument(tenantId, warehouseId, documentId).toResponse()

    @Transactional
    fun post(
        tenantId: Long,
        warehouseId: Long,
        documentId: Long
    ): ReceivingDocumentResponse {
        val document = getDocument(tenantId, warehouseId, documentId)
        ensureDraft(document)

        val lines = lineRepository.findAllByDocument_IdOrderByIdAsc(documentId)

        if (lines.isEmpty()) {
            throw badRequest("Нельзя провести приёмку без товаров")
        }

        for (line in lines) {
            val location = locationRepository.findById(line.locationId)
                .orElseThrow { notFound("Ячейка с ID ${line.locationId} не найдена") }

            if (location.tenantId != tenantId || location.zone.warehouse.id != warehouseId) {
                throw badRequest("Ячейка размещения не принадлежит указанному складу")
            }

            if (!location.isActive) {
                throw badRequest("Нельзя провести приёмку в неактивную ячейку")
            }

            val inventory = inventoryRepository
                .findByTenantIdAndProduct_IdAndLocation_Id(
                    tenantId,
                    requireNotNull(line.product.id),
                    line.locationId
                )
                .orElseGet {
                    Inventory(
                        tenantId = tenantId,
                        product = line.product,
                        location = location
                    )
                }

            inventory.quantity = inventory.quantity.add(line.quantity)
            inventory.updatedAt = OffsetDateTime.now()

            inventoryRepository.save(inventory)

            movementRepository.save(
                InventoryMovement(
                    tenantId = tenantId,
                    product = line.product,
                    locationId = line.locationId,
                    movementType = InventoryMovementType.RECEIPT,
                    quantity = line.quantity,
                    receivingDocumentId = requireNotNull(document.id),
                    receivingLineId = requireNotNull(line.id)
                )
            )
        }

        document.status = ReceivingDocumentStatus.POSTED
        document.postedAt = OffsetDateTime.now()

        auditLogService.log(
            tenantId = tenantId,
            action = "RECEIPT_POSTED",
            entityType = "RECEIVING",
            entityId = document.id,
            details = mapOf(
                "documentNumber" to document.documentNumber,
                "linesCount" to lines.size
            )
        )

        return documentRepository.save(document).toResponse()
    }

    private fun getDocument(
        tenantId: Long,
        warehouseId: Long,
        documentId: Long
    ): ReceivingDocument =
        documentRepository.findByIdAndTenantIdAndWarehouse_Id(
            documentId,
            tenantId,
            warehouseId
        ).orElseThrow {
            notFound("Документ приёмки с ID $documentId не найден")
        }

    private fun validateWarehouse(tenantId: Long, warehouseId: Long) {
        val warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow { notFound("Склад с ID $warehouseId не найден") }

        if (warehouse.tenant.id != tenantId) {
            throw badRequest("Склад не принадлежит указанной компании")
        }
    }

    private fun ensureDraft(document: ReceivingDocument) {
        if (document.status != ReceivingDocumentStatus.DRAFT) {
            throw badRequest("Изменять можно только документ в статусе DRAFT")
        }
    }

    private fun notFound(message: String) =
        ResponseStatusException(HttpStatus.NOT_FOUND, message)

    private fun badRequest(message: String) =
        ResponseStatusException(HttpStatus.BAD_REQUEST, message)

    private fun ReceivingDocument.toResponse(): ReceivingDocumentResponse {
        val lines = lineRepository
            .findAllByDocument_IdOrderByIdAsc(requireNotNull(id))
            .map { it.toResponse() }

        return ReceivingDocumentResponse(
            id = requireNotNull(id),
            tenantId = tenantId,
            warehouseId = requireNotNull(warehouse.id),
            status = status,
            documentNumber = documentNumber,
            notes = notes,
            createdAt = createdAt,
            postedAt = postedAt,
            lines = lines
        )
    }

    private fun ReceivingDocumentLine.toResponse() =
        ReceivingDocumentLineResponse(
            id = requireNotNull(id),
            productId = requireNotNull(product.id),
            sku = product.sku,
            productName = product.name,
            locationId = locationId,
            locationCode = locationRepository.findById(locationId)
                .orElseThrow { notFound("Ячейка с ID $locationId не найдена") }
                .code,
            quantity = quantity
        )
}