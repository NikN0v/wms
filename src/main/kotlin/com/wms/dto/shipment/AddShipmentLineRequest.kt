package com.wms.dto.shipment

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class AddShipmentLineRequest(
    @field:Positive
    val productId: Long,

    @field:Positive
    val locationId: Long,

    @field:DecimalMin("0.001")
    @field:Digits(integer = 15, fraction = 3)
    val quantity: BigDecimal,

    @field:Positive
    val reservationId: Long? = null
)