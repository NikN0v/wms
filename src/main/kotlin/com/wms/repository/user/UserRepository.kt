package com.wms.repository.user

import com.wms.entity.user.User
import com.wms.entity.user.UserRole
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
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

    fun findAllByTenantIdOrderByCreatedAtDesc(tenantId: Long): List<User>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :userId and u.tenantId = :tenantId")
    fun findForUpdateByIdAndTenantId(
        @Param("tenantId") tenantId: Long,
        @Param("userId") userId: Long
    ): Optional<User>

    fun countByTenantIdAndRoleAndIsActiveTrue(tenantId: Long, role: UserRole): Long
}
