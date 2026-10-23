package com.wms.repository.transfer

import com.wms.entity.transfers.StockTransfer
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface StockTransferRepository : JpaRepository<StockTransfer, Long> {

    fun existsByTenantIdAndDocumentNumber(
        tenantId: Long,
        documentNumber: String
    ): Boolean

    fun findAllByTenantIdAndWarehouse_IdOrderByCreatedAtDesc(
        tenantId: Long,
        warehouseId: Long
    ): List<StockTransfer>

    fun findByIdAndTenantIdAndWarehouse_Id(
        id: Long,
        tenantId: Long,
        warehouseId: Long
    ): Optional<StockTransfer>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        SELECT t FROM StockTransfer t
        WHERE t.id = :id
          AND t.tenantId = :tenantId
          AND t.warehouse.id = :warehouseId
        """
    )
    fun findForUpdate(
        @Param("id") id: Long,
        @Param("tenantId") tenantId: Long,
        @Param("warehouseId") warehouseId: Long
    ): Optional<StockTransfer>
}