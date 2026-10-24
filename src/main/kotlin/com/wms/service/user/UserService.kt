package com.wms.service.user

import com.wms.dto.user.RegisterUserRequest
import com.wms.dto.user.UserResponse
import com.wms.entity.user.User
import com.wms.entity.user.UserRole
import com.wms.repository.tenant.TenantRepository
import com.wms.repository.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tenantRepository: TenantRepository
) {

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
}