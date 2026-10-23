package com.wms.dto.zone

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class CreateStorageZoneRequest(

    @field:Positive(message = "ID компании должен быть положительным")
    val tenantId: Long,

    @field:NotBlank(message = "Название зоны обязательно")
    @field:Size(max = 150, message = "Название не должно превышать 150 символов")
    val name: String,

    @field:NotBlank(message = "Код зоны обязателен")
    @field:Size(max = 50, message = "Код не должно превышать 50 символов")
    val code: String
)