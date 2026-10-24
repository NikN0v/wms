package com.wms.dto.user

import jakarta.validation.constraints.NotNull

data class SetUserActiveRequest(
    @field:NotNull
    val active: Boolean? = null
)
