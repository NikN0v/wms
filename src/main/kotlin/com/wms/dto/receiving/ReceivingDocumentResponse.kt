package com.wms.dto.receiving

import com.wms.entity.receiving.ReceivingDocumentStatus
import java.time.OffsetDateTime

data class ReceivingDocumentResponse(
    val id: Long,
    val tenantId: Long,
    val warehouseId: Long,
    val status: ReceivingDocumentStatus,
    val documentNumber: String?,
    val notes: String?,
    val createdAt: OffsetDateTime,
    val postedAt: OffsetDateTime?,
    val lines: List<ReceivingDocumentLineResponse>
)