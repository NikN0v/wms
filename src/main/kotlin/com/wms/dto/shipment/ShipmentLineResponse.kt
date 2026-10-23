package com.wms.dto.shipment

import java.math.BigDecimal

data class ShipmentLineResponse(
    val id: Long,
    val productId: Long,
    val sku: String,
    val productName: String,
    val locationId: Long,
    val locationCode: String,
    val reservationId: Long? = null,
    val quantity: BigDecimal
)