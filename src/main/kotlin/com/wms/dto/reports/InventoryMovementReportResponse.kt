package com.wms.dto.reports

import com.wms.entity.movement.InventoryMovementType
import java.math.BigDecimal
import java.time.OffsetDateTime

data class InventoryMovementReportResponse(
    val id: Long,
    val productId: Long,
    val sku: String,
    val productName: String,
    val locationId: Long,
    val locationCode: String,
    val movementType: InventoryMovementType,
    val quantity: BigDecimal,
    val createdAt: OffsetDateTime
)