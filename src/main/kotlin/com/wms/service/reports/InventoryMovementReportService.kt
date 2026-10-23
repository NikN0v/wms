package com.wms.service.reports

import com.wms.dto.reports.InventoryMovementReportResponse
import com.wms.entity.movement.InventoryMovementType
import com.wms.repository.location.StorageLocationRepository
import com.wms.repository.movement.InventoryMovementRepository
import com.wms.repository.warehouse.WarehouseRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime

@Service
class InventoryMovementReportService(
    private val inventoryMovementRepository: InventoryMovementRepository,
    private val warehouseRepository: WarehouseRepository,
    private val storageLocationRepository: StorageLocationRepository
) {

    @Transactional(readOnly = true)
    fun getReport(
        tenantId: Long,
        warehouseId: Long,
        productId: Long?,
        movementType: InventoryMovementType?,
        from: OffsetDateTime,
        to: OffsetDateTime
    ): List<InventoryMovementReportResponse> {
        if (!warehouseRepository.existsByIdAndTenant_Id(warehouseId, tenantId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Склад не найден"
            )
        }

        if (!from.isBefore(to)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Начало периода должно быть раньше окончания"
            )
        }

        return inventoryMovementRepository.findMovementReport(
            tenantId = tenantId,
            productId = productId,
            movementType = movementType,
            from = from,
            to = to
        ).mapNotNull { movement ->

            val location = storageLocationRepository
                .findById(movement.locationId)
                .orElse(null)
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Ячейка движения не найдена"
                )

            // Оставляем движения только выбранного склада.
            if (location.tenantId != tenantId ||
                location.zone.warehouse.id != warehouseId
            ) {
                return@mapNotNull null
            }

            InventoryMovementReportResponse(
                id = requireNotNull(movement.id),
                productId = requireNotNull(movement.product.id),
                sku = movement.product.sku,
                productName = movement.product.name,
                locationId = movement.locationId,
                locationCode = location.code,
                movementType = movement.movementType,
                quantity = movement.quantity,
                createdAt = movement.createdAt
            )
        }
    }
}