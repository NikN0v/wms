package com.wms.dto.movement

import com.wms.entity.movement.InventoryMovementType
import java.math.BigDecimal
import java.time.OffsetDateTime

data class InventoryMovementResponse(
    val id: Long,
    val tenantId: Long,
    val productId: Long,
    val sku: String,
    val productName: String,
    val locationId: Long,
    val locationCode: String,
    val movementType: InventoryMovementType,
    val quantity: BigDecimal,
    val receivingDocumentId: Long?,
    val receivingLineId: Long?,
    val createdAt: OffsetDateTime
)