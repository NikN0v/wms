package com.wms.repository.tenant

import com.wms.entity.tenant.Tenant
import org.springframework.data.jpa.repository.JpaRepository

interface TenantRepository : JpaRepository<Tenant, Long> {

    fun existsByCode(code: String): Boolean
}