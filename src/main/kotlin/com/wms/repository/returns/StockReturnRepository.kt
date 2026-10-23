package com.wms.repository.returns

import com.wms.entity.returns.StockReturn
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface StockReturnRepository : JpaRepository<StockReturn, Long> {

    fun existsByTenantIdAndDocumentNumber(
        tenantId: Long,
        documentNumber: String
    ): Boolean

    fun findAllByTenantIdAndWarehouse_IdOrderByCreatedAtDesc(
        tenantId: Long,
        warehouseId: Long
    ): List<StockReturn>

    fun findByIdAndTenantIdAndWarehouse_Id(
        id: Long,
        tenantId: Long,
        warehouseId: Long
    ): Optional<StockReturn>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        SELECT r FROM StockReturn r
        WHERE r.id = :id
          AND r.tenantId = :tenantId
          AND r.warehouse.id = :warehouseId
        """
    )
    fun findForUpdate(
        @Param("id") id: Long,
        @Param("tenantId") tenantId: Long,
        @Param("warehouseId") warehouseId: Long
    ): Optional<StockReturn>
}