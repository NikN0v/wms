package com.wms.service.user

import com.wms.dto.user.LoginRequest
import com.wms.dto.user.LoginResponse
import com.wms.repository.user.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtEncoder: JwtEncoder,
    @Value("\${security.jwt.expiration-seconds}")
    private val jwtExpirationSeconds: Long
) {

    init {
        require(jwtExpirationSeconds > 0) {
            "Срок действия JWT должен быть положительным"
        }
    }

    fun login(request: LoginRequest): LoginResponse {
        val username = request.username.trim().lowercase()

        val user = userRepository
            .findByTenantIdAndUsername(request.tenantId, username)
            .orElseThrow { BadCredentialsException("Invalid credentials") }

        if (!user.isActive ||
            !passwordEncoder.matches(request.password, user.passwordHash)
        ) {
            throw BadCredentialsException("Invalid credentials")
        }

        val now = Instant.now()
        val expiresIn = jwtExpirationSeconds

        val claims = JwtClaimsSet.builder()
            .issuer("wms-api")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expiresIn))
            .subject(user.id.toString())
            .claim("userId", user.id)
            .claim("tenantId", user.tenantId)
            .claim("username", user.username)
            .claim("role", user.role.name)
            .build()

        val token = jwtEncoder.encode(
            JwtEncoderParameters.from(
                org.springframework.security.oauth2.jwt.JwsHeader
                    .with(MacAlgorithm.HS256)
                    .build(),
                claims
            )
        ).tokenValue

        return LoginResponse(
            accessToken = token,
            expiresIn = expiresIn
        )
    }
}
