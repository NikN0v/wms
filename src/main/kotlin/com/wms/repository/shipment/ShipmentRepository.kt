package com.wms.repository.shipment

import com.wms.entity.shipment.Shipment
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface ShipmentRepository : JpaRepository<Shipment, Long> {

    fun existsByTenantIdAndDocumentNumber(
        tenantId: Long,
        documentNumber: String
    ): Boolean

    fun findAllByTenantIdAndWarehouse_IdOrderByCreatedAtDesc(
        tenantId: Long,
        warehouseId: Long
    ): List<Shipment>

    fun findByIdAndTenantIdAndWarehouse_Id(
        id: Long,
        tenantId: Long,
        warehouseId: Long
    ): Optional<Shipment>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        SELECT s FROM Shipment s
        WHERE s.id = :id
          AND s.tenantId = :tenantId
          AND s.warehouse.id = :warehouseId
        """
    )
    fun findForUpdate(
        @Param("id") id: Long,
        @Param("tenantId") tenantId: Long,
        @Param("warehouseId") warehouseId: Long
    ): Optional<Shipment>
}