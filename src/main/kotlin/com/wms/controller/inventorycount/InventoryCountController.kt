package com.wms.controller.inventorycount

import com.wms.dto.inventorycount.AddInventoryCountLineRequest
import com.wms.dto.inventorycount.CreateInventoryCountRequest
import com.wms.dto.inventorycount.InventoryCountLineResponse
import com.wms.dto.inventorycount.InventoryCountResponse
import com.wms.service.inventorycount.InventoryCountService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    "/api/tenants/{tenantId}/warehouses/{warehouseId}/inventory-counts"
)
class InventoryCountController(
    private val inventoryCountService: InventoryCountService
) {

    @PostMapping
    fun create(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long,
        @Valid @RequestBody request: CreateInventoryCountRequest
    ): InventoryCountResponse {
        return inventoryCountService.createDocument(
            tenantId,
            warehouseId,
            request
        )
    }

    @PostMapping("/{countId}/lines")
    fun addLine(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long,
        @PathVariable countId: Long,
        @Valid @RequestBody request: AddInventoryCountLineRequest
    ): InventoryCountLineResponse {
        return inventoryCountService.addLine(
            tenantId,
            warehouseId,
            countId,
            request
        )
    }

    @GetMapping
    fun getAll(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long
    ): List<InventoryCountResponse> {
        return inventoryCountService.getAll(tenantId, warehouseId)
    }

    @GetMapping("/{countId}")
    fun getById(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long,
        @PathVariable countId: Long
    ): InventoryCountResponse {
        return inventoryCountService.getById(
            tenantId,
            warehouseId,
            countId
        )
    }

    @PostMapping("/{countId}/post")
    fun post(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long,
        @PathVariable countId: Long
    ): InventoryCountResponse {
        return inventoryCountService.post(
            tenantId,
            warehouseId,
            countId
        )
    }
}