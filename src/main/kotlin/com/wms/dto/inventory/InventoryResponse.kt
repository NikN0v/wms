package com.wms.dto.inventory

import java.math.BigDecimal
import java.time.OffsetDateTime

data class InventoryResponse(
    val id: Long,
    val tenantId: Long,
    val productId: Long,
    val sku: String,
    val productName: String,
    val locationId: Long,
    val locationCode: String,
    val quantity: BigDecimal,
    val reservedQuantity: BigDecimal,
    val availableQuantity: BigDecimal,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)