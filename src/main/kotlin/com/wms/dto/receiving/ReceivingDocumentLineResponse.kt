package com.wms.dto.receiving

import java.math.BigDecimal

data class ReceivingDocumentLineResponse(
    val id: Long,
    val productId: Long,
    val sku: String,
    val productName: String,
    val locationId: Long,
    val locationCode: String,
    val quantity: BigDecimal
)