package com.wms.dto.shipment

import com.wms.entity.shipment.ShipmentStatus
import java.time.OffsetDateTime

data class ShipmentResponse(
    val id: Long,
    val tenantId: Long,
    val warehouseId: Long,
    val documentNumber: String,
    val status: ShipmentStatus,
    val notes: String?,
    val createdAt: OffsetDateTime,
    val postedAt: OffsetDateTime?,
    val lines: List<ShipmentLineResponse>
)