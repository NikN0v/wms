package com.wms.controller.zone

import com.wms.dto.zone.CreateStorageZoneRequest
import com.wms.dto.zone.StorageZoneResponse
import com.wms.service.zone.StorageZoneService
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
@RequestMapping("/api/tenants/{tenantId}/warehouses/{warehouseId}/zones")
class StorageZoneController(
    private val zoneService: StorageZoneService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long,
        @Valid @RequestBody request: CreateStorageZoneRequest
    ): StorageZoneResponse {
        if (tenantId != request.tenantId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "ID компании в URL и теле запроса не совпадают"
            )
        }

        return zoneService.create(warehouseId, request)
    }

    @GetMapping
    fun getAll(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long
    ): List<StorageZoneResponse> {
        val zones = zoneService.getAllByWarehouseId(warehouseId)

        if (zones.any { it.tenantId != tenantId }) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Склад не принадлежит указанной компании"
            )
        }

        return zones
    }
}