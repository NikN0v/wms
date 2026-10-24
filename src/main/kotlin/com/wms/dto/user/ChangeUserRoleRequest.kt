package com.wms.dto.user

import com.wms.entity.user.UserRole
import jakarta.validation.constraints.NotNull

data class ChangeUserRoleRequest(
    @field:NotNull
    val role: UserRole? = null
)
