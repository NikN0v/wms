package com.wms.repository.shipment

import com.wms.entity.shipment.ShipmentLine
import org.springframework.data.jpa.repository.JpaRepository

interface ShipmentLineRepository : JpaRepository<ShipmentLine, Long> {

    fun findAllByDocument_IdOrderByIdAsc(documentId: Long): List<ShipmentLine>

    fun existsByDocument_Id(documentId: Long): Boolean

    fun existsByReservationId(reservationId: Long): Boolean
}