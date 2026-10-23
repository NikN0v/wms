package com.wms.controller.warehouse

import com.wms.dto.warehouse.CreateWarehouseRequest
import com.wms.dto.warehouse.WarehouseResponse
import com.wms.service.warehouse.WarehouseService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/tenants/{tenantId}/warehouses")
class WarehouseController(
    private val warehouseService: WarehouseService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable tenantId: Long,
        @Valid @RequestBody request: CreateWarehouseRequest
    ): WarehouseResponse {
        if (tenantId != request.tenantId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "ID компании в URL и теле запроса не совпадают"
            )
        }

        return warehouseService.create(request)
    }

    @GetMapping
    fun getAll(
        @PathVariable tenantId: Long
    ): List<WarehouseResponse> {
        return warehouseService.getAllByTenantId(tenantId)
    }
}