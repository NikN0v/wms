package com.wms.dto.reservation

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class CreateReservationRequest(
    @field:Positive
    val productId: Long,

    @field:Positive
    val locationId: Long,

    @field:DecimalMin("0.001")
    @field:Digits(integer = 15, fraction = 3)
    val quantity: BigDecimal,

    @field:Size(max = 50)
    val referenceType: String? = null,

    @field:Size(max = 100)
    val referenceId: String? = null
)