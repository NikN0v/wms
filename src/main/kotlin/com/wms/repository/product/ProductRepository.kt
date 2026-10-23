package com.wms.repository.product

import com.wms.entity.product.Product
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ProductRepository : JpaRepository<Product, Long> {

    fun existsByTenantIdAndSku(tenantId: Long, sku: String): Boolean

    fun findAllByTenantId(tenantId: Long): List<Product>

    fun findByIdAndTenant_Id(
        id: Long,
        tenantId: Long
    ): Optional<Product>
}