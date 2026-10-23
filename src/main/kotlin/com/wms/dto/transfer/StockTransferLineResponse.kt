package com.wms.dto.transfer

import java.math.BigDecimal

data class StockTransferLineResponse(
    val id: Long,
    val productId: Long,
    val sku: String,
    val productName: String,
    val sourceLocationId: Long,
    val sourceLocationCode: String,
    val targetLocationId: Long,
    val targetLocationCode: String,
    val quantity: BigDecimal
)