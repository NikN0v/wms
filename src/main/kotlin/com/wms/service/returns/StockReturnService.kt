
package com.wms.service.returns

import com.wms.dto.returns.AddStockReturnLineRequest
import com.wms.dto.returns.CreateStockReturnRequest
import com.wms.dto.returns.StockReturnLineResponse
import com.wms.dto.returns.StockReturnResponse
import com.wms.entity.movement.InventoryMovement
import com.wms.entity.movement.InventoryMovementType
import com.wms.entity.returns.StockReturn
import com.wms.entity.returns.StockReturnLine
import com.wms.entity.returns.StockReturnStatus
import com.wms.repository.inventory.InventoryRepository
import com.wms.repository.location.StorageLocationRepository
import com.wms.repository.movement.InventoryMovementRepository
import com.wms.repository.product.ProductRepository
import com.wms.repository.returns.StockReturnLineRepository
import com.wms.repository.returns.StockReturnRepository
import com.wms.repository.warehouse.WarehouseRepository
import com.wms.service.audit.AuditLogService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.OffsetDateTime

@Service
class StockReturnService(
    private val returnRepository: StockReturnRepository,
    private val lineRepository: StockReturnLineRepository,
    private val warehouseRepository: WarehouseRepository,
    private val productRepository: ProductRepository,
    private val locationRepository: StorageLocationRepository,
    private val inventoryRepository: InventoryRepository,
    private val inventoryMovementRepository: InventoryMovementRepository,
    private val auditLogService: AuditLogService
) {

    @Transactional
    fun createDocument(
        tenantId: Long,
        warehouseId: Long,
        request: CreateStockReturnRequest
    ): StockReturnResponse {
        warehouseRepository
            .findByIdAndTenantId(warehouseId, tenantId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Склад не найден"
                )
            }

        if (
            returnRepository.existsByTenantIdAndDocumentNumber(
                tenantId,
                request.documentNumber
            )
        ) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Документ с таким номером уже существует"
            )
        }

        val document = returnRepository.save(
            StockReturn(
                tenantId = tenantId,
                warehouse = warehouseRepository
                    .findByIdAndTenantId(warehouseId, tenantId)
                    .orElseThrow {
                        ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Склад не найден"
                        )
                    },
                documentNumber = request.documentNumber.trim(),
                notes = request.notes?.trim()
            )
        )

        return toResponse(document)
    }


    @Transactional
    fun post(
        tenantId: Long,
        warehouseId: Long,
        returnId: Long
    ): StockReturnResponse {
        val document = returnRepository
            .findForUpdate(returnId, tenantId, warehouseId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Документ возврата не найден"
                )
            }

        if (document.status != StockReturnStatus.DRAFT) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Возврат уже проведён или его статус недопустим"
            )
        }

        val documentId = requireNotNull(document.id)

        val lines = lineRepository
            .findAllByDocument_IdOrderByIdAsc(documentId)

        if (lines.isEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Нельзя провести возврат без товаров"
            )
        }

        for (line in lines) {
            val location = locationRepository
                .findById(line.locationId)
                .orElseThrow {
                    ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Ячейка хранения не найдена"
                    )
                }

            if (
                location.tenantId != tenantId ||
                location.zone.warehouse.id != warehouseId ||
                !location.isActive
            ) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ячейка неактивна или принадлежит другому складу"
                )
            }

            val inventory = inventoryRepository
                .findForUpdate(
                    tenantId,
                    requireNotNull(line.product.id),
                    line.locationId
                )
                .orElseGet {
                    // Если остатка ещё нет, создаём новую запись.
                    com.wms.entity.inventory.Inventory(
                        tenantId = tenantId,
                        product = line.product,
                        location = location,
                        quantity = BigDecimal.ZERO,
                        reservedQuantity = BigDecimal.ZERO
                    )
                }

            inventory.quantity = inventory.quantity.add(line.quantity)
            inventory.updatedAt = OffsetDateTime.now()
            val savedInventory = inventoryRepository.save(inventory)

            inventoryMovementRepository.save(
                InventoryMovement(
                    tenantId = tenantId,
                    product = line.product,
                    locationId = line.locationId,
                    movementType = InventoryMovementType.RETURN,
                    quantity = line.quantity
                )
            )
        }

        document.status = StockReturnStatus.POSTED
        document.postedAt = OffsetDateTime.now()

        returnRepository.save(document)

        auditLogService.log(
            tenantId = tenantId,
            action = "STOCK_RETURN_POSTED",
            entityType = "STOCK_RETURN",
            entityId = document.id,
            details = mapOf(
                "documentNumber" to document.documentNumber,
                "linesCount" to lines.size,
                "status" to document.status.name
            )
        )

        return toResponse(document)
    }


    @Transactional
    fun addLine(
        tenantId: Long,
        warehouseId: Long,
        returnId: Long,
        request: AddStockReturnLineRequest
    ): StockReturnResponse {
        val document = returnRepository
            .findByIdAndTenantIdAndWarehouse_Id(
                returnId,
                tenantId,
                warehouseId
            )
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Документ возврата не найден"
                )
            }

        if (document.status != StockReturnStatus.DRAFT) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Нельзя изменять проведённый возврат"
            )
        }

        val product = productRepository
            .findByIdAndTenant_Id(request.productId, tenantId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Товар не найден"
                )
            }

        if (!product.isActive) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Товар неактивен"
            )
        }

        val location = locationRepository
            .findByIdAndTenantId(request.locationId, tenantId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Ячейка хранения не найдена"
                )
            }

        if (
            !location.isActive ||
            location.zone.warehouse.id != warehouseId
        ) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Ячейка неактивна или принадлежит другому складу"
            )
        }

        val line = StockReturnLine(
            document = document,
            tenantId = tenantId,
            productId = requireNotNull(product.id),
            product = product,
            locationId = request.locationId,
            quantity = request.quantity
        )

        lineRepository.save(line)

        return toResponse(document)
    }

    @Transactional(readOnly = true)
    fun getAll(
        tenantId: Long,
        warehouseId: Long
    ): List<StockReturnResponse> {
        warehouseRepository
            .findByIdAndTenantId(warehouseId, tenantId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Склад не найден"
                )
            }

        return returnRepository
            .findAllByTenantIdAndWarehouse_IdOrderByCreatedAtDesc(
                tenantId,
                warehouseId
            )
            .map(::toResponse)
    }

    @Transactional(readOnly = true)
    fun getById(
        tenantId: Long,
        warehouseId: Long,
        returnId: Long
    ): StockReturnResponse {
        val document = returnRepository
            .findByIdAndTenantIdAndWarehouse_Id(
                returnId,
                tenantId,
                warehouseId
            )
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Документ возврата не найден"
                )
            }

        return toResponse(document)
    }

    private fun toResponse(
        document: StockReturn
    ): StockReturnResponse {
        val documentId = requireNotNull(document.id)

        val lines = lineRepository
            .findAllByDocument_IdOrderByIdAsc(documentId)
            .map { line ->
                StockReturnLineResponse(
                    id = requireNotNull(line.id),
                    productId = requireNotNull(line.product.id),
                    sku = line.product.sku,
                    productName = line.product.name,
                    locationId = line.locationId,
                    locationCode = locationRepository
                        .findById(line.locationId)
                        .orElseThrow {
                            ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Ячейка хранения не найдена"
                            )
                        }
                        .code,
                    quantity = line.quantity
                )
            }

        return StockReturnResponse(
            id = documentId,
            tenantId = document.tenantId,
            warehouseId = requireNotNull(document.warehouse.id),
            documentNumber = document.documentNumber,
            status = document.status,
            notes = document.notes,
            createdAt = document.createdAt,
            postedAt = document.postedAt,
            lines = lines
        )
    }
}
