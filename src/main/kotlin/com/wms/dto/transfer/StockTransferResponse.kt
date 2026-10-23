package com.wms.dto.transfer

import com.wms.entity.transfers.StockTransferStatus
import java.time.OffsetDateTime

data class StockTransferResponse(
    val id: Long,
    val tenantId: Long,
    val warehouseId: Long,
    val documentNumber: String,
    val status: StockTransferStatus,
    val notes: String?,
    val createdAt: OffsetDateTime,
    val postedAt: OffsetDateTime?,
    val lines: List<StockTransferLineResponse>
)