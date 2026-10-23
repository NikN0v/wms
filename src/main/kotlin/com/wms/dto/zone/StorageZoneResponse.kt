package com.wms.dto.zone

import java.time.OffsetDateTime

data class StorageZoneResponse(
    val id: Long,
    val tenantId: Long,
    val warehouseId: Long,
    val name: String,
    val code: String,
    val isActive: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)