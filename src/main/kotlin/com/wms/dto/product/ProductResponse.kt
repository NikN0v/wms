package com.wms.dto.product

import java.time.OffsetDateTime

data class ProductResponse(
    val id: Long,
    val tenantId: Long,
    val sku: String,
    val name: String,
    val description: String?,
    val unit: String,
    val isActive: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)