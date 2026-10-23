package com.wms.repository.inventory

import com.wms.entity.inventory.Inventory
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface InventoryRepository : JpaRepository<Inventory, Long> {

    fun existsByTenantIdAndProduct_IdAndLocation_Id(
        tenantId: Long,
        productId: Long,
        locationId: Long
    ): Boolean

    fun findAllByTenantId(tenantId: Long): List<Inventory>

    fun findByTenantIdAndProduct_IdAndLocation_Id(
        tenantId: Long,
        productId: Long,
        locationId: Long
    ): Optional<Inventory>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
    SELECT i FROM Inventory i
    WHERE i.tenantId = :tenantId
      AND i.product.id = :productId
      AND i.location.id = :locationId
    """
    )
    fun findForUpdate(
        @Param("tenantId") tenantId: Long,
        @Param("productId") productId: Long,
        @Param("locationId") locationId: Long
    ): Optional<Inventory>
}