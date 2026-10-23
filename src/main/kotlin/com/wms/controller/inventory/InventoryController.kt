package com.wms.controller.inventory

import com.wms.dto.inventory.CreateInventoryRequest
import com.wms.dto.inventory.InventoryResponse
import com.wms.service.inventory.InventoryService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tenants/{tenantId}/inventory")
class InventoryController(
    private val inventoryService: InventoryService
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
        @PathVariable tenantId: Long
    ): List<InventoryResponse> {
        return inventoryService.getAllByTenantId(tenantId)
    }
}