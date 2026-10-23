package com.wms.service.warehouse

import com.wms.dto.warehouse.CreateWarehouseRequest
import com.wms.dto.warehouse.WarehouseResponse
import com.wms.entity.warehouse.Warehouse
import com.wms.repository.tenant.TenantRepository
import com.wms.repository.warehouse.WarehouseRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class WarehouseService(
    private val warehouseRepository: WarehouseRepository,
    private val tenantRepository: TenantRepository
) {

    @Transactional
    fun create(request: CreateWarehouseRequest): WarehouseResponse {
        val tenantId = request.tenantId

        val tenant = tenantRepository.findById(tenantId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Компания с ID: $tenantId не найдена"
                )
            }

        val normalizedCode = request.code.trim().uppercase()
        val normalizedName = request.name.trim()

        if (warehouseRepository.existsByTenantIdAndCode(tenantId, normalizedCode)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Склад с кодом: $normalizedCode уже существует в этой компании"
            )
        }

        val warehouse = Warehouse(
            tenant = tenant,
            name = normalizedName,
            code = normalizedCode
        )

        return warehouseRepository.save(warehouse).toResponse()
    }

    @Transactional(readOnly = true)
    fun getAllByTenantId(tenantId: Long): List<WarehouseResponse> {
        if (!tenantRepository.existsById(tenantId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Компания с ID $tenantId не найдена"
            )
        }

        return warehouseRepository.findAllByTenantId(tenantId)
            .map { it.toResponse() }
    }

    private fun Warehouse.toResponse() = WarehouseResponse(
        id = requireNotNull(id),
        tenantId = tenant.id ?: error("У склада отсутствует ID компании"),
        name = name,
        code = code,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}