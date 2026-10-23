package com.wms.repository.movement

import com.wms.entity.movement.InventoryMovement
import com.wms.entity.movement.InventoryMovementType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime

interface InventoryMovementRepository : JpaRepository<InventoryMovement, Long> {

    fun findAllByTenantIdOrderByCreatedAtDesc(tenantId: Long): List<InventoryMovement>

    fun findAllByTenantIdAndProduct_IdOrderByCreatedAtDesc(tenantId: Long, productId: Long): List<InventoryMovement>

    fun findAllByTenantIdAndLocationIdOrderByCreatedAtDesc(tenantId: Long, locationId: Long): List<InventoryMovement>

    @Query(
        """
    SELECT m FROM InventoryMovement m
    JOIN FETCH m.product p
    WHERE m.tenantId = :tenantId
      AND (:productId IS NULL OR p.id = :productId)
      AND (:movementType IS NULL OR m.movementType = :movementType)
      AND m.createdAt >= :from
      AND m.createdAt < :to
    ORDER BY m.createdAt DESC, m.id DESC
    """
    )
    fun findMovementReport(
        @Param("tenantId") tenantId: Long,
        @Param("productId") productId: Long?,
        @Param("movementType") movementType: InventoryMovementType?,
        @Param("from") from: OffsetDateTime,
        @Param("to") to: OffsetDateTime
    ): List<InventoryMovement>
}