package com.wms.repository.reservation

import com.wms.entity.reservation.StockReservation
import com.wms.entity.reservation.ReservationStatus
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface StockReservationRepository :
    JpaRepository<StockReservation, Long> {

    fun findAllByTenantIdOrderByCreatedAtDesc(
        tenantId: Long
    ): List<StockReservation>

    fun findAllByTenantIdAndStatusOrderByCreatedAtDesc(
        tenantId: Long,
        status: ReservationStatus
    ): List<StockReservation>

    fun findByIdAndTenantId(
        id: Long,
        tenantId: Long
    ): Optional<StockReservation>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        SELECT r FROM StockReservation r
        WHERE r.id = :id AND r.tenantId = :tenantId
        """
    )
    fun findForUpdate(
        @Param("id") id: Long,
        @Param("tenantId") tenantId: Long
    ): Optional<StockReservation>
}