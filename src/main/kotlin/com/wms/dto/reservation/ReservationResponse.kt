package com.wms.dto.reservation

import com.wms.entity.reservation.ReservationStatus
import java.math.BigDecimal
import java.time.OffsetDateTime

data class ReservationResponse(
    val id: Long,
    val tenantId: Long,
    val productId: Long,
    val sku: String,
    val productName: String,
    val locationId: Long,
    val quantity: BigDecimal,
    val status: ReservationStatus,
    val referenceType: String?,
    val referenceId: String?,
    val createdAt: OffsetDateTime,
    val releasedAt: OffsetDateTime?
)