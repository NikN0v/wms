package com.wms.controller.user

import com.wms.dto.user.RegisterUserRequest
import com.wms.dto.user.ChangeUserRoleRequest
import com.wms.dto.user.SetUserActiveRequest
import com.wms.dto.user.UserResponse
import com.wms.entity.user.UserRole
import com.wms.service.user.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/tenants/{tenantId}/users")
class UserController(
    private val userService: UserService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @PathVariable tenantId: Long,
        @Valid @RequestBody request: RegisterUserRequest
    ): UserResponse {
        return userService.register(tenantId, request)
    }

    @GetMapping
    fun getAll(@PathVariable tenantId: Long): List<UserResponse> =
        userService.getAllByTenantId(tenantId)

    @PatchMapping("/{userId}/active")
    fun setActive(
        @PathVariable tenantId: Long,
        @PathVariable userId: Long,
        @Valid @RequestBody request: SetUserActiveRequest,
        authentication: JwtAuthenticationToken
    ): UserResponse {
        val (actorId, role) = actor(authentication)
        return userService.setActive(tenantId, userId, actorId, role, requireNotNull(request.active))
    }

    @PatchMapping("/{userId}/role")
    fun changeRole(
        @PathVariable tenantId: Long,
        @PathVariable userId: Long,
        @Valid @RequestBody request: ChangeUserRoleRequest,
        authentication: JwtAuthenticationToken
    ): UserResponse {
        val (actorId, role) = actor(authentication)
        return userService.changeRole(tenantId, userId, actorId, role, requireNotNull(request.role))
    }

    private fun actor(authentication: JwtAuthenticationToken): Pair<Long, UserRole> {
        val id = authentication.token.claims["userId"]?.toString()?.toLongOrNull()
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "В токене отсутствует корректный ID пользователя")
        val role = authentication.token.claims["role"]?.toString()?.let {
            runCatching { UserRole.valueOf(it) }.getOrNull()
        } ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "В токене отсутствует корректная роль")
        return id to role
    }
}
