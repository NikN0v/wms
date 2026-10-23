package com.wms.dto.inventorycount

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

data class AddInventoryCountLineRequest(
    @field:NotNull
    val productId: Long,

    @field:NotNull
    val locationId: Long,

    @field:NotNull
    @field:PositiveOrZero
    val actualQuantity: BigDecimal
)