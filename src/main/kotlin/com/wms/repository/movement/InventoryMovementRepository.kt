package com.wms.repository.movement

import com.wms.entity.movement.InventoryMovement
import org.springframework.data.jpa.repository.JpaRepository

interface InventoryMovementRepository : JpaRepository<InventoryMovement, Long> {

    fun findAllByTenantIdOrderByCreatedAtDesc(tenantId: Long): List<InventoryMovement>

    fun findAllByTenantIdAndProduct_IdOrderByCreatedAtDesc(tenantId: Long, productId: Long): List<InventoryMovement>

    fun findAllByTenantIdAndLocationIdOrderByCreatedAtDesc(tenantId: Long, locationId: Long): List<InventoryMovement>
}