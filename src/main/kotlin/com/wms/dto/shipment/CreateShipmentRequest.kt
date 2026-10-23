package com.wms.dto.shipment

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateShipmentRequest(
    @field:NotBlank
    @field:Size(max = 100)
    val documentNumber: String,

    @field:Size(max = 2000)
    val notes: String? = null
)