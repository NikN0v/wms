package com.wms.controller.warehouse

import com.wms.dto.warehouse.CreateWarehouseRequest
import com.wms.dto.warehouse.WarehouseResponse
import com.wms.service.warehouse.WarehouseService
import com.wms.service.warehouse.WarehouseAccessService
import com.wms.entity.user.UserRole
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/tenants/{tenantId}/warehouses")
class WarehouseController(
    private val warehouseService: WarehouseService,
    private val warehouseAccessService: WarehouseAccessService
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

        val warehouses = warehouseService.getAllByTenantId(tenantId)
        val userId = authentication.token.claims["userId"]?.toString()?.toLongOrNull()
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "В токене отсутствует корректный ID пользователя")
        val role = authentication.token.claims["role"]?.toString()?.let {
            runCatching { UserRole.valueOf(it) }.getOrNull()
        } ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "В токене отсутствует корректная роль")
        return if (role == UserRole.OPERATOR) {
            val assignedIds = warehouseAccessService.getAssignedWarehouseIds(tenantId, userId).toSet()
            warehouses.filter { it.id in assignedIds }
        } else warehouses
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
