package com.wms.dto.audit

import java.time.OffsetDateTime

data class AuditLogResponse(
    val id: Long,
    val tenantId: Long,
    val userId: Long?,
    val action: String,
    val entityType: String,
    val entityId: Long?,
    val details: Map<String, Any?>?,
    val createdAt: OffsetDateTime
)