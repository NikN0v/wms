package com.wms.controller.reservation

import com.wms.dto.reservation.CreateReservationRequest
import com.wms.dto.reservation.ReservationResponse
import com.wms.service.reservation.ReservationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tenants/{tenantId}/reservations")
class ReservationController(
    private val reservationService: ReservationService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable tenantId: Long,
        @Valid @RequestBody request: CreateReservationRequest
    ): ReservationResponse =
        reservationService.create(tenantId, request)

    @GetMapping
    fun getAll(
        @PathVariable tenantId: Long
    ): List<ReservationResponse> =
        reservationService.getAll(tenantId)

    @GetMapping("/{reservationId}")
    fun getById(
        @PathVariable tenantId: Long,
        @PathVariable reservationId: Long
    ): ReservationResponse =
        reservationService.getById(tenantId, reservationId)

    @PostMapping("/{reservationId}/release")
    fun release(
        @PathVariable tenantId: Long,
        @PathVariable reservationId: Long
    ): ReservationResponse =
        reservationService.release(tenantId, reservationId)
}