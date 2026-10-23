package com.wms.dto.tenant

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateTenantRequest(

    @field:NotBlank(message = "Название компании обязательно")
    @field:Size(max = 150, message = "Название не должно превышать 150 символов")
    val name: String,

    @field:NotBlank(message = "Код компании обязателен")
    @field:Size(max = 50, message = "Код не должен превышать 50 символов")
    val code: String
)