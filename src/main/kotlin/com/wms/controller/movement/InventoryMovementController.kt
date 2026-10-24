package com.wms.controller.movement

import com.wms.dto.movement.InventoryMovementResponse
import com.wms.service.movement.InventoryMovementService
import com.wms.service.warehouse.WarehouseAccessService
import com.wms.security.wmsUserContext
import com.wms.entity.user.UserRole
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tenants/{tenantId}/inventory-movements")
class InventoryMovementController(
    private val movementService: InventoryMovementService,
    private val warehouseAccessService: WarehouseAccessService
) {

    @GetMapping
    fun getAll(@PathVariable tenantId: Long, authentication: JwtAuthenticationToken): List<InventoryMovementResponse> {
        val allowed = allowedWarehouses(tenantId, authentication)
        return movementService.getAll(tenantId, allowed)
    }

    @GetMapping("/products/{productId}")
    fun getByProduct(
        @PathVariable tenantId: Long,
        @PathVariable productId: Long,
        authentication: JwtAuthenticationToken
    ): List<InventoryMovementResponse> =
        movementService.getByProduct(tenantId, productId, allowedWarehouses(tenantId, authentication))

    @GetMapping("/locations/{locationId}")
    fun getByLocation(
        @PathVariable tenantId: Long,
        @PathVariable locationId: Long,
        authentication: JwtAuthenticationToken
    ): List<InventoryMovementResponse> =
        movementService.getByLocation(tenantId, locationId, allowedWarehouses(tenantId, authentication))

    private fun allowedWarehouses(tenantId: Long, authentication: JwtAuthenticationToken): Set<Long>? {
        val identity = authentication.wmsUserContext()
        return if (identity.role == UserRole.OPERATOR) {
            warehouseAccessService.getAssignedWarehouseIds(tenantId, identity.userId).toSet()
        } else null
    }
}
