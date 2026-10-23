package com.wms.service.zone

import com.wms.dto.zone.CreateStorageZoneRequest
import com.wms.dto.zone.StorageZoneResponse
import com.wms.entity.zone.StorageZone
import com.wms.repository.warehouse.WarehouseRepository
import com.wms.repository.zone.StorageZoneRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class StorageZoneService(
    private val zoneRepository: StorageZoneRepository,
    private val warehouseRepository: WarehouseRepository
) {

    @Transactional
    fun create(
        warehouseId: Long,
        request: CreateStorageZoneRequest
    ): StorageZoneResponse {
        val warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Склад с ID $warehouseId не найден"
                )
            }

        if (warehouse.tenant.id != request.tenantId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Склад не принадлежит указанной компании"
            )
        }

        val normalizedCode = request.code.trim().uppercase()
        val normalizedName = request.name.trim()

        if (zoneRepository.existsByWarehouseIdAndCode(
                warehouseId,
                normalizedCode
            )
        ) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Зона с кодом $normalizedCode уже существует на этом складе"
            )
        }

        val zone = StorageZone(
            warehouse = warehouse,
            tenantId = request.tenantId,
            name = normalizedName,
            code = normalizedCode
        )

        return zoneRepository.save(zone).toResponse()
    }

    @Transactional(readOnly = true)
    fun getAllByWarehouseId(
        warehouseId: Long
    ): List<StorageZoneResponse> {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Склад с ID $warehouseId не найден"
            )
        }

        return zoneRepository.findAllByWarehouseId(warehouseId)
            .map { it.toResponse() }
    }

    private fun StorageZone.toResponse() = StorageZoneResponse(
        id = requireNotNull(id),
        tenantId = tenantId,
        warehouseId = requireNotNull(warehouse.id),
        name = name,
        code = code,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}