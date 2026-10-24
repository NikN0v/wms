package com.wms.controller.user

import com.wms.dto.user.RegisterUserRequest
import com.wms.dto.user.UserResponse
import com.wms.service.user.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

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
}