package com.wms.dto.product

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class CreateProductRequest(

    @field:Positive(message = "ID компании должен быть положительным")
    val tenantId: Long,

    @field:NotBlank(message = "SKU обязателен")
    @field:Size(max = 100)
    val sku: String,

    @field:NotBlank(message = "Название товара обязательно")
    @field:Size(max = 200)
    val name: String,

    val description: String? = null,

    @field:NotBlank(message = "Единица измерения обязательна")
    val unit: String = "PCS"
)