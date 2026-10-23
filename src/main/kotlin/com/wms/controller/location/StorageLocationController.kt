package com.wms.controller.location

import com.wms.dto.location.CreateStorageLocationRequest
import com.wms.dto.location.StorageLocationResponse
import com.wms.service.location.StorageLocationService
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
@RequestMapping("/api/tenants/{tenantId}/warehouses/{warehouseId}/zones/{zoneId}/locations")
class StorageLocationController(
    private val locationService: StorageLocationService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long,
        @PathVariable zoneId: Long,
        @Valid @RequestBody request: CreateStorageLocationRequest
    ): StorageLocationResponse {
        if (tenantId != request.tenantId) {
            throw org.springframework.web.server.ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "ID компании в URL и теле запроса не совпадают"
            )
        }

        return locationService.create(warehouseId, zoneId, request)
    }

    @GetMapping
    fun getAll(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long,
        @PathVariable zoneId: Long
    ): List<StorageLocationResponse> {
        return locationService.getAllByZoneId(
            warehouseId = warehouseId,
            zoneId = zoneId,
            tenantId = tenantId
        )
    }
}