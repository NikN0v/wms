package com.wms.dto.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class LoginRequest(

    @field:Positive
    val tenantId: Long,

    @field:NotBlank
    @field:Size(max = 100)
    val username: String,

    @field:NotBlank
    @field:Size(max = 72)
    val password: String
)