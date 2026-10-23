package com.wms.dto.warehouse

import java.time.OffsetDateTime

data class WarehouseResponse(
    val id: Long,
    val tenantId: Long,
    val name: String,
    val code: String,
    val isActive: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)