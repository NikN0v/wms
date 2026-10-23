package com.wms.dto.returns

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class AddStockReturnLineRequest(

    @field:NotNull
    @field:Positive
    val productId: Long,

    @field:NotNull
    @field:Positive
    val locationId: Long,

    @field:NotNull
    @field:Positive
    val quantity: BigDecimal
)