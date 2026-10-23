package com.wms.controller.tenant

import com.wms.dto.tenant.CreateTenantRequest
import com.wms.dto.tenant.TenantResponse
import com.wms.service.tenant.TenantService
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
@RequestMapping("/api/tenants")
class TenantController(
    private val tenantService: TenantService
) {

    @GetMapping
    fun getAll(): List<TenantResponse> {
        return tenantService.getAll()
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): TenantResponse {
        return tenantService.getById(id)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateTenantRequest): TenantResponse {
        return tenantService.create(request)
    }
}