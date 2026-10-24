package com.wms.service.user

import com.wms.dto.user.LoginRequest
import com.wms.entity.user.User
import com.wms.entity.user.UserRole
import com.wms.repository.user.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import java.time.Duration
import java.time.OffsetDateTime
import java.util.Optional

class AuthServiceTest {

    @Test
    fun `login uses configured JWT expiration`() {
        val userRepository = Mockito.mock(UserRepository::class.java)
        val passwordEncoder = Mockito.mock(PasswordEncoder::class.java)
        val jwtEncoder = Mockito.mock(JwtEncoder::class.java)
        val user = User(
            id = 15L,
            tenantId = 3L,
            username = "admin",
            passwordHash = "encoded-password",
            role = UserRole.ADMIN
        )

        Mockito.`when`(userRepository.findByTenantIdAndUsername(3L, "admin"))
            .thenReturn(Optional.of(user))
        Mockito.`when`(passwordEncoder.matches("password", "encoded-password"))
            .thenReturn(true)
        Mockito.`when`(jwtEncoder.encode(Mockito.any(JwtEncoderParameters::class.java)))
            .thenReturn(
                Jwt.withTokenValue("token")
                    .header("alg", "HS256")
                    .claim("sub", "15")
                    .build()
            )

        val response = AuthService(
            userRepository,
            passwordEncoder,
            jwtEncoder,
            jwtExpirationSeconds = 900L
        ).login(LoginRequest(tenantId = 3L, username = "admin", password = "password"))

        val parameters = ArgumentCaptor.forClass(JwtEncoderParameters::class.java)
        Mockito.verify(jwtEncoder).encode(parameters.capture())
        val claims = parameters.value.claims

        assertEquals("token", response.accessToken)
        assertEquals(900L, response.expiresIn)
        assertEquals(
            900L,
            Duration.between(
                requireNotNull(claims.issuedAt),
                requireNotNull(claims.expiresAt)
            ).seconds
        )
    }

    @Test
    fun `non-positive JWT expiration is rejected`() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            AuthService(
                Mockito.mock(UserRepository::class.java),
                Mockito.mock(PasswordEncoder::class.java),
                Mockito.mock(JwtEncoder::class.java),
                jwtExpirationSeconds = 0L
            )
        }

        assertEquals("Срок действия JWT должен быть положительным", error.message)
    }
}
