package com.wms.dto.location

import java.time.OffsetDateTime

data class StorageLocationResponse(
    val id: Long,
    val tenantId: Long,
    val zoneId: Long,
    val code: String,
    val type: String,
    val isActive: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)