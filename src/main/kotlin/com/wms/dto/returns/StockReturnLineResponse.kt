package com.wms.dto.returns

import java.math.BigDecimal

data class StockReturnLineResponse(
    val id: Long,
    val productId: Long,
    val sku: String,
    val productName: String,
    val locationId: Long,
    val locationCode: String,
    val quantity: BigDecimal
)