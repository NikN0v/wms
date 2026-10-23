package com.wms.dto.returns

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateStockReturnRequest(

    @field:NotBlank
    @field:Size(max = 100)
    val documentNumber: String,

    val notes: String? = null
)