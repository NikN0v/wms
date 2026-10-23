package com.wms.dto.location

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class CreateStorageLocationRequest(

    @field:Positive(message = "ID компании должно быть положительным")
    val tenantId: Long,

    @field:NotBlank(message = "Код ячейки обязателен")
    @field:Size(max = 50, message = "Код не должно превышать 50 символов")
    val code: String,

    @field:NotNull(message = "Тип ячейки обязателен")
    val type: String = "STANDARD"
)