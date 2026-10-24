package com.wms.controller.reservation

import com.wms.dto.reservation.CreateReservationRequest
import com.wms.dto.reservation.ReservationResponse
import com.wms.service.reservation.ReservationService
import com.wms.service.warehouse.WarehouseAccessService
import com.wms.security.wmsUserContext
import com.wms.entity.user.UserRole
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tenants/{tenantId}/reservations")
class ReservationController(
    private val reservationService: ReservationService,
    private val warehouseAccessService: WarehouseAccessService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable tenantId: Long,
        @Valid @RequestBody request: CreateReservationRequest,
        authentication: JwtAuthenticationToken
    ): ReservationResponse =
        reservationService.create(tenantId, request, allowedWarehouses(tenantId, authentication))

    @GetMapping
    fun getAll(
        @PathVariable tenantId: Long,
        authentication: JwtAuthenticationToken
    ): List<ReservationResponse> =
        reservationService.getAll(tenantId, allowedWarehouses(tenantId, authentication))

    @GetMapping("/{reservationId}")
    fun getById(
        @PathVariable tenantId: Long,
        @PathVariable reservationId: Long,
        authentication: JwtAuthenticationToken
    ): ReservationResponse =
        reservationService.getById(tenantId, reservationId, allowedWarehouses(tenantId, authentication))

    @PostMapping("/{reservationId}/release")
    fun release(
        @PathVariable tenantId: Long,
        @PathVariable reservationId: Long,
        authentication: JwtAuthenticationToken
    ): ReservationResponse =
        reservationService.release(tenantId, reservationId, allowedWarehouses(tenantId, authentication))

    private fun allowedWarehouses(tenantId: Long, authentication: JwtAuthenticationToken): Set<Long>? {
        val identity = authentication.wmsUserContext()
        return if (identity.role == UserRole.OPERATOR) {
            warehouseAccessService.getAssignedWarehouseIds(tenantId, identity.userId).toSet()
        } else null
    }
}
