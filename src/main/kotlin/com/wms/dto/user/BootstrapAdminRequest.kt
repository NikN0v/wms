package com.wms.dto.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class BootstrapAdminRequest(

    @field:Positive
    val tenantId: Long? = null,

    @field:Size(max = 150)
    val tenantName: String? = null,

    @field:Size(max = 50)
    val tenantCode: String? = null,

    @field:NotBlank
    @field:Size(min = 3, max = 100)
    @field:Pattern(
        regexp = "^[a-zA-Z0-9._-]+$",
        message = "Имя пользователя может содержать только буквы, цифры, точки, подчеркивания и дефисы"
    )
    val username: String,

    @field:NotBlank
    @field:Size(min = 12, max = 72)
    val password: String
)
