package com.wms.controller.movement

import com.wms.dto.movement.InventoryMovementResponse
import com.wms.service.movement.InventoryMovementService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tenants/{tenantId}/inventory-movements")
class InventoryMovementController(
    private val movementService: InventoryMovementService
) {

    @GetMapping
    fun getAll(@PathVariable tenantId: Long): List<InventoryMovementResponse> = movementService.getAll(tenantId)

    @GetMapping("/products/{productId}")
    fun getByProduct(
        @PathVariable tenantId: Long,
        @PathVariable productId: Long
    ): List<InventoryMovementResponse> =
        movementService.getByProduct(tenantId, productId)

    @GetMapping("/locations/{locationId}")
    fun getByLocation(
        @PathVariable tenantId: Long,
        @PathVariable locationId: Long
    ): List<InventoryMovementResponse> =
        movementService.getByLocation(tenantId, locationId)
}