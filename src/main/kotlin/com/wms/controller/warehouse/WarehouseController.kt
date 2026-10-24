package com.wms.controller.warehouse

import com.wms.dto.warehouse.CreateWarehouseRequest
import com.wms.dto.warehouse.WarehouseResponse
import com.wms.service.warehouse.WarehouseService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.*
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
        @Valid @RequestBody request: CreateWarehouseRequest,
        authentication: JwtAuthenticationToken
    ): WarehouseResponse {
        requireTenantAccess(tenantId, authentication)

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
        @PathVariable tenantId: Long,
        authentication: JwtAuthenticationToken
    ): List<WarehouseResponse> {
        requireTenantAccess(tenantId, authentication)

        return warehouseService.getAllByTenantId(tenantId)
    }

    private fun requireTenantAccess(
        tenantId: Long,
        authentication: JwtAuthenticationToken
    ) {
        val tokenTenantId = authentication.token.claims["tenantId"]
            ?.toString()
            ?.toLongOrNull()
            ?: throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "В токене отсутствует корректный ID компании"
            )

        if (tenantId != tokenTenantId) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Нет доступа к данным этой компании"
            )
        }
    }
}