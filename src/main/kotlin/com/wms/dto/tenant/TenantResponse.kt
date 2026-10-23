package com.wms.dto.tenant

import java.time.OffsetDateTime

data class TenantResponse(
    val id: Long,
    val name: String,
    val code: String,
    val isActive: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)