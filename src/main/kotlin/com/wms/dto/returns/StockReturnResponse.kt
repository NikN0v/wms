package com.wms.dto.returns

import com.wms.entity.returns.StockReturnStatus
import java.time.OffsetDateTime

data class StockReturnResponse(
    val id: Long,
    val tenantId: Long,
    val warehouseId: Long,
    val documentNumber: String,
    val status: StockReturnStatus,
    val notes: String?,
    val createdAt: OffsetDateTime,
    val postedAt: OffsetDateTime?,
    val lines: List<StockReturnLineResponse>
)