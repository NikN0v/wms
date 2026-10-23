package com.wms.dto.transfer

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class AddStockTransferLineRequest(
    @field:Positive(message = "ID товара должен быть положительным")
    val productId: Long,

    @field:Positive(message = "ID исходной ячейки должен быть положительным")
    val sourceLocationId: Long,

    @field:Positive(message = "ID целевой ячейки должен быть положительным")
    val targetLocationId: Long,

    @field:DecimalMin(value = "0.001", message = "Количество должно быть больше нуля")
    @field:Digits(integer = 15, fraction = 3)
    val quantity: BigDecimal
)