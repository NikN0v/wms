package com.wms.controller.inventory

import com.wms.dto.inventory.CreateInventoryRequest
import com.wms.dto.inventory.InventoryResponse
import com.wms.service.inventory.InventoryService
import com.wms.service.warehouse.WarehouseAccessService
import com.wms.security.wmsUserContext
import com.wms.entity.user.UserRole
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

@RestController
@RequestMapping("/api/tenants/{tenantId}/inventory")
class InventoryController(
    private val inventoryService: InventoryService,
    private val warehouseAccessService: WarehouseAccessService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable tenantId: Long,
        @Valid @RequestBody request: CreateInventoryRequest
    ): InventoryResponse {
        return inventoryService.create(tenantId, request)
    }

    @GetMapping
    fun getAll(
        @PathVariable tenantId: Long,
        authentication: JwtAuthenticationToken
    ): List<InventoryResponse> {
        val identity = authentication.wmsUserContext()
        val allowedWarehouses = if (identity.role == UserRole.OPERATOR) {
            warehouseAccessService.getAssignedWarehouseIds(tenantId, identity.userId).toSet()
        } else null
        return inventoryService.getAllByTenantId(tenantId, allowedWarehouses)
    }
}
