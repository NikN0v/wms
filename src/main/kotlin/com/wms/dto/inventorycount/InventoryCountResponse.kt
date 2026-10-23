package com.wms.dto.inventorycount

import com.wms.entity.inventorycount.InventoryCountStatus
import java.time.OffsetDateTime

data class InventoryCountResponse(
    val id: Long,
    val tenantId: Long,
    val warehouseId: Long,
    val documentNumber: String,
    val status: InventoryCountStatus,
    val notes: String?,
    val createdAt: OffsetDateTime,
    val postedAt: OffsetDateTime?,
    val lines: List<InventoryCountLineResponse>
)