package com.wms.dto.user

import com.wms.entity.user.UserRole
import java.time.OffsetDateTime

data class UserResponse(
    val id: Long,
    val tenantId: Long,
    val username: String,
    val role: UserRole,
    val isActive: Boolean,
    val createdAt: OffsetDateTime
)