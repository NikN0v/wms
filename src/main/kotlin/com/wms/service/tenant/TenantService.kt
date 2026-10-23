package com.wms.service.tenant

import com.wms.dto.tenant.CreateTenantRequest
import com.wms.dto.tenant.TenantResponse
import com.wms.entity.tenant.Tenant
import com.wms.repository.tenant.TenantRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class TenantService(
    private val tenantRepository: TenantRepository
) {

    @Transactional(readOnly = true)
    fun getAll(): List<TenantResponse> {
        return tenantRepository.findAll().map { tenant -> tenant.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): TenantResponse {
        val tenant = tenantRepository.findById(id)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Компания с ID: $id не найдена"
                )
            }

        return tenant.toResponse()
    }

    @Transactional
    fun create(request: CreateTenantRequest): TenantResponse {
        val normalizedCode = request.code.trim().uppercase()
        val normalizedName = request.name.trim()

        if (tenantRepository.existsByCode(normalizedCode)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Компания с таким кодом уже существует"
            )
        }

        val tenant = Tenant(
            name = normalizedName,
            code = normalizedCode
        )

        val savedTenant = tenantRepository.save(tenant)

        return savedTenant.toResponse()
    }

    private fun Tenant.toResponse() = TenantResponse(
        id = requireNotNull(id),
        name = name,
        code = code,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}