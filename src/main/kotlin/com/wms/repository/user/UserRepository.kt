package com.wms.repository.user

import com.wms.entity.user.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {

    fun findByTenantIdAndUsername(
        tenantId: Long,
        username: String
    ): Optional<User>

    fun existsByTenantIdAndUsername(
        tenantId: Long,
        username: String
    ): Boolean
}