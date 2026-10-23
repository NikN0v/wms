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

    @Query(
        """
    SELECT i FROM Inventory i
    JOIN FETCH i.product p
    JOIN FETCH i.location l
    JOIN FETCH l.zone z
    WHERE i.tenantId = :tenantId
      AND z.warehouse.id = :warehouseId
      AND (:productId IS NULL OR p.id = :productId)
      AND (:locationId IS NULL OR l.id = :locationId)
    ORDER BY p.sku, l.code
    """
    )
    fun findStockBalances(
        @Param("tenantId") tenantId: Long,
        @Param("warehouseId") warehouseId: Long,
        @Param("productId") productId: Long?,
        @Param("locationId") locationId: Long?
    ): List<Inventory>
}