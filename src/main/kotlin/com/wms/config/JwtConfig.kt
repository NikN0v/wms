package com.wms.config

import com.nimbusds.jose.jwk.source.ImmutableSecret
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

@Configuration
class JwtConfig(
    @Value("\${security.jwt.secret}")
    private val secretBase64: String
) {

    private fun secretKey(): SecretKey {
        val keyBytes = Base64.getDecoder().decode(secretBase64)

        require(keyBytes.size >= 32) {
            "Секрет JWT должен содержать не менее 32 байт"
        }

        return SecretKeySpec(keyBytes, "HmacSHA256")
    }

    @Bean
    fun jwtEncoder(): JwtEncoder =
        NimbusJwtEncoder(ImmutableSecret(secretKey()))

    @Bean
    fun jwtDecoder(): JwtDecoder =
        NimbusJwtDecoder
            .withSecretKey(secretKey())
            .macAlgorithm(MacAlgorithm.HS256)
            .build()
}