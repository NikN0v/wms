package com.wms.dto.audit

import java.time.OffsetDateTime

data class AuditLogFilterRequest(
    val action: String? = null,
    val entityType: String? = null,
    val entityId: Long? = null,
    val from: OffsetDateTime? = null,
    val to: OffsetDateTime? = null,
    val page: Int = 0,
    val size: Int = 20
)