package com.wms.dto.inventorycount

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateInventoryCountRequest(
    @field:NotBlank
    @field:Size(max = 100)
    val documentNumber: String,

    val notes: String? = null
)