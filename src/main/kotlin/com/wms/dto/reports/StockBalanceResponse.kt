package com.wms.dto.reports

import java.math.BigDecimal

data class StockBalanceResponse(
    val productId: Long,
    val sku: String,
    val productName: String,
    val locationId: Long,
    val locationCode: String,
    val quantity: BigDecimal,
    val reservedQuantity: BigDecimal,
    val availableQuantity: BigDecimal
)