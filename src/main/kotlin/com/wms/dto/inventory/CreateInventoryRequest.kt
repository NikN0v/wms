package com.wms.dto.inventory

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class CreateInventoryRequest(

    @field:Positive(message = "ID товара должно быть положительным")
    val productId: Long,

    @field:Positive(message = "ID ячейки должно быть положительным")
    val locationId: Long,

    @field:DecimalMin(value = "0.001", message = "Количество должно быть больше нуля")
    @field:Digits(integer = 15, fraction = 3)
    val quantity: BigDecimal
)