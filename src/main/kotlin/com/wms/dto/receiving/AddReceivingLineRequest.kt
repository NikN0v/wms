package com.wms.dto.receiving

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class AddReceivingLineRequest(

    @field:Positive(message = "ID товара должен быть положительным")
    val productId: Long,

    @field:Positive(message = "ID ячейки должен быть положительным")
    val locationId: Long,

    @field:DecimalMin(value = "0.001", message = "Количество должно быть больше нуля")
    @field:Digits(integer = 15, fraction = 3)
    val quantity: BigDecimal
)