package com.wms.security

import com.wms.entity.user.UserRole
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.server.ResponseStatusException

data class JwtUserContext(
    val tenantId: Long,
    val userId: Long,
    val role: UserRole
)

fun JwtAuthenticationToken.wmsUserContext(): JwtUserContext {
    val tenantId = token.claims["tenantId"]?.toString()?.toLongOrNull()
        ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "В токене отсутствует корректный ID компании")
    val userId = token.claims["userId"]?.toString()?.toLongOrNull()
        ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "В токене отсутствует корректный ID пользователя")
    val role = token.claims["role"]?.toString()?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
        ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "В токене отсутствует корректная роль")
    return JwtUserContext(tenantId, userId, role)
}
