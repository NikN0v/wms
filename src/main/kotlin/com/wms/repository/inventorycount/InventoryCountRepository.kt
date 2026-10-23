package com.wms.repository.inventorycount

import com.wms.entity.inventorycount.InventoryCount
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface InventoryCountRepository : JpaRepository<InventoryCount, Long> {

    fun existsByTenantIdAndDocumentNumber(
        tenantId: Long,
        documentNumber: String
    ): Boolean

    fun findAllByTenantIdAndWarehouse_IdOrderByCreatedAtDesc(
        tenantId: Long,
        warehouseId: Long
    ): List<InventoryCount>

    fun findByIdAndTenantIdAndWarehouse_Id(
        id: Long,
        tenantId: Long,
        warehouseId: Long
    ): Optional<InventoryCount>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        SELECT c FROM InventoryCount c
        WHERE c.id = :id
          AND c.tenantId = :tenantId
          AND c.warehouse.id = :warehouseId
        """
    )
    fun findForUpdate(
        @Param("id") id: Long,
        @Param("tenantId") tenantId: Long,
        @Param("warehouseId") warehouseId: Long
    ): Optional<InventoryCount>
}