package com.wms.service.transfer

import com.wms.dto.transfer.AddStockTransferLineRequest
import com.wms.dto.transfer.CreateStockTransferRequest
import com.wms.dto.transfer.StockTransferLineResponse
import com.wms.dto.transfer.StockTransferResponse
import com.wms.entity.inventory.Inventory
import com.wms.entity.movement.InventoryMovement
import com.wms.entity.movement.InventoryMovementType
import com.wms.entity.transfers.StockTransfer
import com.wms.entity.transfers.StockTransferLine
import com.wms.entity.transfers.StockTransferStatus
import com.wms.repository.inventory.InventoryRepository
import com.wms.repository.location.StorageLocationRepository
import com.wms.repository.movement.InventoryMovementRepository
import com.wms.repository.product.ProductRepository
import com.wms.repository.tenant.TenantRepository
import com.wms.repository.transfer.StockTransferLineRepository
import com.wms.repository.transfer.StockTransferRepository
import com.wms.repository.warehouse.WarehouseRepository
import com.wms.service.audit.AuditLogService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime

@Service
class StockTransferService(
    private val tenantRepository: TenantRepository,
    private val warehouseRepository: WarehouseRepository,
    private val productRepository: ProductRepository,
    private val locationRepository: StorageLocationRepository,
    private val transferRepository: StockTransferRepository,
    private val lineRepository: StockTransferLineRepository,
    private val inventoryRepository: InventoryRepository,
    private val movementRepository: InventoryMovementRepository,
    private val stockTransferRepository: StockTransferRepository,
    private val auditLogService: AuditLogService
) {

    @Transactional
    fun createDocument(
        tenantId: Long,
        warehouseId: Long,
        request: CreateStockTransferRequest
    ): StockTransferResponse {
        validateTenant(tenantId)

        val warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow { notFound("Склад с ID $warehouseId не найден") }

        if (warehouse.tenant.id != tenantId) {
            throw badRequest("Склад не принадлежит указанной компании")
        }

        val number = request.documentNumber.trim()

        if (transferRepository.existsByTenantIdAndDocumentNumber(tenantId, number)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Документ с номером $number уже существует"
            )
        }

        val document = transferRepository.save(
            StockTransfer(
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
        request: AddStockTransferLineRequest
    ): StockTransferResponse {
        val document = getDocument(tenantId, warehouseId, documentId)
        ensureDraft(document)

        if (request.sourceLocationId == request.targetLocationId) {
            throw badRequest("Исходная и целевая ячейки должны отличаться")
        }

        val product = productRepository.findById(request.productId)
            .orElseThrow { notFound("Товар с ID ${request.productId} не найден") }

        if (product.tenant.id != tenantId) {
            throw badRequest("Товар не принадлежит указанной компании")
        }

        if (!product.isActive) {
            throw badRequest("Нельзя перемещать неактивный товар")
        }

        val source = getLocation(request.sourceLocationId)
        val target = getLocation(request.targetLocationId)

        if (source.tenantId != tenantId || target.tenantId != tenantId) {
            throw badRequest("Ячейки должны принадлежать указанной компании")
        }

        if (source.zone.warehouse.id != warehouseId ||
            target.zone.warehouse.id != warehouseId
        ) {
            throw badRequest("Обе ячейки должны принадлежать указанному складу")
        }

        if (!source.isActive || !target.isActive) {
            throw badRequest("Нельзя перемещать товар через неактивную ячейку")
        }

        lineRepository.save(
            StockTransferLine(
                document = document,
                tenantId = tenantId,
                product = product,
                sourceLocationId = request.sourceLocationId,
                targetLocationId = request.targetLocationId,
                quantity = request.quantity
            )
        )

        return document.toResponse()
    }

    @Transactional(readOnly = true)
    fun getAll(
        tenantId: Long,
        warehouseId: Long
    ): List<StockTransferResponse> {
        validateWarehouse(tenantId, warehouseId)

        return transferRepository
            .findAllByTenantIdAndWarehouse_IdOrderByCreatedAtDesc(
                tenantId,
                warehouseId
            )
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getById(
        tenantId: Long,
        warehouseId: Long,
        documentId: Long
    ): StockTransferResponse =
        getDocument(tenantId, warehouseId, documentId).toResponse()

    @Transactional
    fun post(
        tenantId: Long,
        warehouseId: Long,
        documentId: Long
    ): StockTransferResponse {
        // Репозиторий блокирует документ до завершения транзакции.
        val document = stockTransferRepository
            .findForUpdate(documentId, tenantId, warehouseId)
            .orElseThrow {
                // Используй здесь то же исключение,
                // которое уже применяется в твоём сервисе
                // для отсутствующего документа.
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Документ перемещения не найден"
                )
            }
        ensureDraft(document)

        val lines = lineRepository
            .findAllByDocument_IdOrderByIdAsc(documentId)

        if (lines.isEmpty()) {
            throw badRequest("Нельзя провести перемещение без товаров")
        }

        for (line in lines) {
            val productId = requireNotNull(line.product.id)

            // Всегда блокируем ячейки в одном порядке,
            // чтобы уменьшить вероятность взаимных блокировок.
            val locationIds = listOf(
                line.sourceLocationId,
                line.targetLocationId
            ).sorted()

            val lockedInventories = locationIds.associateWith { locationId ->
                inventoryRepository.findForUpdate(
                    tenantId,
                    productId,
                    locationId
                ).orElse(null)
            }

            val sourceInventory = lockedInventories[line.sourceLocationId]
                ?: throw badRequest(
                    "В исходной ячейке нет остатка этого товара"
                )

            val sourceAvailable = sourceInventory.quantity
                .subtract(sourceInventory.reservedQuantity)

            if (sourceAvailable < line.quantity) {
                throw badRequest(
                    "Недостаточно доступного товара. " +
                            "Доступно: $sourceAvailable, требуется: ${line.quantity}"
                )
            }

            val sourceLocation = getLocation(line.sourceLocationId)
            val targetLocation = getLocation(line.targetLocationId)

            if (sourceLocation.tenantId != tenantId ||
                targetLocation.tenantId != tenantId ||
                sourceLocation.zone.warehouse.id != warehouseId ||
                targetLocation.zone.warehouse.id != warehouseId
            ) {
                throw badRequest("Ячейки не принадлежат указанному складу")
            }

            if (!sourceLocation.isActive || !targetLocation.isActive) {
                throw badRequest("Нельзя перемещать товар через неактивную ячейку")
            }

            sourceInventory.quantity =
                sourceInventory.quantity.subtract(line.quantity)
            sourceInventory.updatedAt = OffsetDateTime.now()
            inventoryRepository.save(sourceInventory)

            val targetInventory = lockedInventories[line.targetLocationId]
                ?: Inventory(
                    tenantId = tenantId,
                    product = line.product,
                    location = targetLocation
                )

            targetInventory.quantity =
                targetInventory.quantity.add(line.quantity)
            targetInventory.updatedAt = OffsetDateTime.now()
            inventoryRepository.save(targetInventory)

            movementRepository.saveAll(
                listOf(
                    InventoryMovement(
                        tenantId = tenantId,
                        product = line.product,
                        locationId = line.sourceLocationId,
                        movementType = InventoryMovementType.TRANSFER_OUT,
                        quantity = line.quantity
                    ),
                    InventoryMovement(
                        tenantId = tenantId,
                        product = line.product,
                        locationId = line.targetLocationId,
                        movementType = InventoryMovementType.TRANSFER_IN,
                        quantity = line.quantity
                    )
                )
            )
        }

        document.status = StockTransferStatus.POSTED
        document.postedAt = OffsetDateTime.now()

        transferRepository.save(document)

        auditLogService.log(
            tenantId = tenantId,
            action = "STOCK_TRANSFER_POSTED",
            entityType = "STOCK_TRANSFER",
            entityId = document.id,
            details = mapOf(
                "documentNumber" to document.documentNumber,
                "linesCount" to lines.size,
                "status" to document.status.name
            )
        )

        return document.toResponse()
    }

    private fun getDocument(
        tenantId: Long,
        warehouseId: Long,
        documentId: Long
    ): StockTransfer =
        transferRepository.findByIdAndTenantIdAndWarehouse_Id(
            documentId,
            tenantId,
            warehouseId
        ).orElseThrow {
            notFound("Документ перемещения с ID $documentId не найден")
        }

    private fun getLocation(locationId: Long) =
        locationRepository.findById(locationId)
            .orElseThrow {
                notFound("Ячейка с ID $locationId не найдена")
            }

    private fun validateTenant(tenantId: Long) {
        if (!tenantRepository.existsById(tenantId)) {
            throw notFound("Компания с ID $tenantId не найдена")
        }
    }

    private fun validateWarehouse(tenantId: Long, warehouseId: Long) {
        val warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow { notFound("Склад с ID $warehouseId не найден") }

        if (warehouse.tenant.id != tenantId) {
            throw badRequest("Склад не принадлежит указанной компании")
        }
    }

    private fun ensureDraft(document: StockTransfer) {
        if (document.status != StockTransferStatus.DRAFT) {
            throw badRequest("Изменять можно только документ в статусе DRAFT")
        }
    }

    private fun notFound(message: String) =
        ResponseStatusException(HttpStatus.NOT_FOUND, message)

    private fun badRequest(message: String) =
        ResponseStatusException(HttpStatus.BAD_REQUEST, message)

    private fun StockTransfer.toResponse(): StockTransferResponse {
        val lines = lineRepository
            .findAllByDocument_IdOrderByIdAsc(requireNotNull(id))
            .map { it.toResponse() }

        return StockTransferResponse(
            id = requireNotNull(id),
            tenantId = tenantId,
            warehouseId = requireNotNull(warehouse.id),
            documentNumber = documentNumber,
            status = status,
            notes = notes,
            createdAt = createdAt,
            postedAt = postedAt,
            lines = lines
        )
    }

    private fun StockTransferLine.toResponse(): StockTransferLineResponse {
        val source = getLocation(sourceLocationId)
        val target = getLocation(targetLocationId)

        return StockTransferLineResponse(
            id = requireNotNull(id),
            productId = requireNotNull(product.id),
            sku = product.sku,
            productName = product.name,
            sourceLocationId = sourceLocationId,
            sourceLocationCode = source.code,
            targetLocationId = targetLocationId,
            targetLocationCode = target.code,
            quantity = quantity
        )
    }
}