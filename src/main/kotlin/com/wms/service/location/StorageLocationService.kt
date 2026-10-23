package com.wms.service.location

import com.wms.dto.location.CreateStorageLocationRequest
import com.wms.dto.location.StorageLocationResponse
import com.wms.entity.location.StorageLocation
import com.wms.repository.location.StorageLocationRepository
import com.wms.repository.zone.StorageZoneRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class StorageLocationService(
    private val locationRepository: StorageLocationRepository,
    private val zoneRepository: StorageZoneRepository
) {

    @Transactional
    fun create(
        warehouseId: Long,
        zoneId: Long,
        request: CreateStorageLocationRequest
    ): StorageLocationResponse {
        val zone = zoneRepository.findById(zoneId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Зона с ID: $zoneId не найдена"
                )
            }

        if (zone.warehouse.id != warehouseId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Зона не принадлежит указанному складу"
            )
        }

        if (zone.tenantId != request.tenantId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Зона не принадлежит указанной компании"
            )
        }

        val normalizedCode = request.code.trim().uppercase()
        val normalizedType = request.type.trim().uppercase()

        val allowedTypes = setOf("STANDARD", "RECEIVING", "SHIPPING", "PICKING")
        if (normalizedType !in allowedTypes) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Недопустимый тип ячейки"
            )
        }

        if (locationRepository.existsByZoneIdAndCode(zoneId, normalizedCode)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ячейка с кодом: $normalizedCode уже существует в этой зоне"
            )
        }

        val location = StorageLocation(
            zone = zone,
            tenantId = request.tenantId,
            code = normalizedCode,
            type = normalizedType
        )

        return locationRepository.save(location).toResponse()
    }

    @Transactional(readOnly = true)
    fun getAllByZoneId(
        warehouseId: Long,
        zoneId: Long,
        tenantId: Long
    ): List<StorageLocationResponse> {
        val zone = zoneRepository.findById(zoneId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Зона с ID: $zoneId не найдена"
                )
            }

        if (zone.warehouse.id != warehouseId || zone.tenantId != tenantId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Зона не принадлежит указанному складу или компании"
            )
        }

        return locationRepository.findAllByZoneId(zoneId)
            .map { it.toResponse() }
    }

    private fun StorageLocation.toResponse() = StorageLocationResponse(
        id = requireNotNull(id),
        tenantId = tenantId,
        zoneId = requireNotNull(zone.id),
        code = code,
        type = type,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}