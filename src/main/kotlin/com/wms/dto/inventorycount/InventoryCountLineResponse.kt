package com.wms.dto.inventorycount

import java.math.BigDecimal

data class InventoryCountLineResponse(
    val id: Long,
    val productId: Long,
    val sku: String,
    val productName: String,
    val locationId: Long,
    val locationCode: String,
    val systemQuantity: BigDecimal,
    val actualQuantity: BigDecimal,
    val difference: BigDecimal
)