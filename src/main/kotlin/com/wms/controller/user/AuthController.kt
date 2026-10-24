package com.wms.controller.user

import com.wms.dto.user.LoginRequest
import com.wms.dto.user.LoginResponse
import com.wms.dto.user.BootstrapAdminRequest
import com.wms.dto.user.UserResponse
import com.wms.service.user.AuthService
import com.wms.service.user.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val userService: UserService
) {

    @PostMapping("/bootstrap-admin")
    @ResponseStatus(HttpStatus.CREATED)
    fun bootstrapAdmin(
        @Valid @RequestBody request: BootstrapAdminRequest,
        @RequestHeader("X-Bootstrap-Secret", required = false) bootstrapSecret: String?
    ): UserResponse = userService.bootstrapAdmin(request, bootstrapSecret)

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): LoginResponse {
        return authService.login(request)
    }
}
