package com.wms.dto.receiving

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateReceivingDocumentRequest(

    @field:NotBlank(message = "Номер документа обязателен")
    @field:Size(max = 100)
    val documentNumber: String,

    @field:Size(max = 2000)
    val notes: String? = null
)