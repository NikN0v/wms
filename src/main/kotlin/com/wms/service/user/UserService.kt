package com.wms.service.user

import com.wms.dto.user.BootstrapAdminRequest
import com.wms.dto.user.RegisterUserRequest
import com.wms.dto.user.UserResponse
import com.wms.entity.tenant.Tenant
import com.wms.entity.user.User
import com.wms.entity.user.UserRole
import jakarta.persistence.EntityManager
import org.hibernate.Session
import com.wms.repository.tenant.TenantRepository
import com.wms.repository.user.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.security.MessageDigest

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tenantRepository: TenantRepository,
    private val entityManager: EntityManager,
    @Value("\${security.bootstrap.secret:}")
    private val bootstrapSecret: String
) {

    @Transactional(readOnly = true)
    fun getAllByTenantId(tenantId: Long): List<UserResponse> {
        requireTenant(tenantId)
        return userRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId)
            .map { it.toResponse() }
    }

    @Transactional
    fun setActive(
        tenantId: Long,
        userId: Long,
        actorUserId: Long,
        actorRole: UserRole,
        isActive: Boolean
    ): UserResponse {
        val user = getTargetForUpdate(tenantId, userId, actorUserId)
        if (actorRole !in setOf(UserRole.ADMIN, UserRole.MANAGER)) forbidden()
        if (actorRole == UserRole.MANAGER && user.role != UserRole.OPERATOR) forbidden()

        if (!isActive && user.isActive && user.role == UserRole.ADMIN &&
            userRepository.countByTenantIdAndRoleAndIsActiveTrue(tenantId, UserRole.ADMIN) <= 1
        ) lastActiveAdminConflict()

        user.isActive = isActive
        return user.toResponse()
    }

    @Transactional
    fun changeRole(
        tenantId: Long,
        userId: Long,
        actorUserId: Long,
        actorRole: UserRole,
        role: UserRole
    ): UserResponse {
        if (actorRole != UserRole.ADMIN) forbidden()
        if (role == UserRole.ADMIN) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Назначение роли ADMIN через API запрещено")
        }
        val user = getTargetForUpdate(tenantId, userId, actorUserId)
        if (user.role == UserRole.ADMIN && user.isActive &&
            userRepository.countByTenantIdAndRoleAndIsActiveTrue(tenantId, UserRole.ADMIN) <= 1
        ) lastActiveAdminConflict()

        user.role = role
        return user.toResponse()
    }

    private fun getTargetForUpdate(tenantId: Long, userId: Long, actorUserId: Long): User {
        if (tenantId <= 0 || userId <= 0 || actorUserId <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный ID")
        }
        tenantRepository.findByIdForUpdate(tenantId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Компания не найдена")
        }
        if (userId == actorUserId) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Нельзя изменять собственную учетную запись")
        }
        return userRepository.findForUpdateByIdAndTenantId(tenantId, userId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден")
        }
    }

    private fun requireTenant(tenantId: Long) {
        if (tenantId <= 0) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный ID компании")
        if (!tenantRepository.existsById(tenantId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Компания не найдена")
        }
    }

    private fun forbidden(): Nothing = throw ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав")

    private fun lastActiveAdminConflict(): Nothing = throw ResponseStatusException(
        HttpStatus.CONFLICT,
        "Нельзя отключить или понизить последнего активного администратора"
    )

    @Transactional
    fun bootstrapAdmin(
        request: BootstrapAdminRequest,
        suppliedSecret: String?
    ): UserResponse {
        verifyBootstrapSecret(suppliedSecret)
        acquireBootstrapLock()

        if (userRepository.count() > 0) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Первоначальная настройка уже выполнена"
            )
        }

        val tenant = resolveBootstrapTenant(request)
        val tenantId = requireNotNull(tenant.id)
        val username = request.username.trim().lowercase()

        if (userRepository.existsByTenantIdAndUsername(tenantId, username)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Имя пользователя уже занято"
            )
        }

        val user = User(
            tenantId = tenantId,
            username = username,
            passwordHash = requireNotNull(passwordEncoder.encode(request.password)),
            role = UserRole.ADMIN
        )

        return userRepository.save(user).toResponse()
    }

    @Transactional
    fun register(
        tenantId: Long,
        request: RegisterUserRequest
    ): UserResponse {
        if (tenantId <= 0) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Неверный ID компании"
            )
        }

        if (!tenantRepository.existsById(tenantId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Компания не найдена"
            )
        }

        val username = request.username.trim().lowercase()

        if (userRepository.existsByTenantIdAndUsername(tenantId, username)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Имя пользователя уже занято"
            )
        }

        val user = User(
            tenantId = tenantId,
            username = username,
            passwordHash = requireNotNull(
                passwordEncoder.encode(request.password)
            ),
            role = UserRole.OPERATOR
        )

        return userRepository.save(user).toResponse()
    }

    private fun User.toResponse() = UserResponse(
        id = requireNotNull(id),
        tenantId = tenantId,
        username = username,
        role = role,
        isActive = isActive,
        createdAt = createdAt
    )

    private fun verifyBootstrapSecret(suppliedSecret: String?) {
        if (bootstrapSecret.isBlank()) {
            throw ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Первоначальная настройка отключена"
            )
        }

        val suppliedBytes = suppliedSecret?.toByteArray(Charsets.UTF_8) ?: ByteArray(0)
        val expectedBytes = bootstrapSecret.toByteArray(Charsets.UTF_8)
        if (!MessageDigest.isEqual(expectedBytes, suppliedBytes)) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Неверный секрет первоначальной настройки"
            )
        }
    }

    private fun acquireBootstrapLock() {
        entityManager.unwrap(Session::class.java).doWork { connection ->
            connection.prepareStatement("SELECT pg_advisory_xact_lock(?)").use { statement ->
                statement.setLong(1, BOOTSTRAP_LOCK_KEY)
                statement.execute()
            }
        }
    }

    private fun resolveBootstrapTenant(request: BootstrapAdminRequest): Tenant {
        val tenantId = request.tenantId
        val tenantName = request.tenantName?.trim()?.takeIf(String::isNotEmpty)
        val tenantCode = request.tenantCode?.trim()?.uppercase()?.takeIf(String::isNotEmpty)

        if (tenantId != null) {
            if (tenantName != null || tenantCode != null) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Укажите либо tenantId, либо название и код новой компании"
                )
            }

            return tenantRepository.findById(tenantId)
                .orElseThrow {
                    ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Компания не найдена"
                    )
                }
        }

        if (tenantName == null || tenantCode == null) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Для создания компании укажите tenantName и tenantCode"
            )
        }

        if (tenantRepository.existsByCode(tenantCode)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Компания с таким кодом уже существует"
            )
        }

        return tenantRepository.save(
            Tenant(name = tenantName, code = tenantCode)
        )
    }

    private companion object {
        const val BOOTSTRAP_LOCK_KEY: Long = 0x574D53424F4F54L
    }
}
