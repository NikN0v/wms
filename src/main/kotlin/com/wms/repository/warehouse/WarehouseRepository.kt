package com.wms.repository.warehouse

import com.wms.entity.warehouse.Warehouse
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface WarehouseRepository : JpaRepository<Warehouse, Long> {

    fun existsByTenantIdAndCode(tenantId: Long, code: String): Boolean

    fun findAllByTenantId(tenantId: Long): List<Warehouse>

    fun findByIdAndTenantId(
        id: Long,
        tenantId: Long
    ): Optional<Warehouse>

    fun existsByIdAndTenant_Id(
        id: Long,
        tenantId: Long
    ): Boolean
}