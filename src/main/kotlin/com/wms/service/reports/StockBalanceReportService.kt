package com.wms.service.reports

import com.wms.dto.reports.StockBalanceResponse
import com.wms.repository.inventory.InventoryRepository
import com.wms.repository.warehouse.WarehouseRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class StockBalanceReportService(
    private val inventoryRepository: InventoryRepository,
    private val warehouseRepository: WarehouseRepository
) {

    @Transactional(readOnly = true)
    fun getStockBalances(
        tenantId: Long,
        warehouseId: Long,
        productId: Long?,
        locationId: Long?
    ): List<StockBalanceResponse> {
        if (!warehouseRepository.existsByIdAndTenant_Id(warehouseId, tenantId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Склад не найден"
            )
        }

        return inventoryRepository.findStockBalances(
            tenantId = tenantId,
            warehouseId = warehouseId,
            productId = productId,
            locationId = locationId
        ).map { inventory ->
            StockBalanceResponse(
                productId = requireNotNull(inventory.product.id),
                sku = inventory.product.sku,
                productName = inventory.product.name,
                locationId = requireNotNull(inventory.location.id),
                locationCode = inventory.location.code,
                quantity = inventory.quantity,
                reservedQuantity = inventory.reservedQuantity,
                availableQuantity = inventory.availableQuantity
            )
        }
    }
}