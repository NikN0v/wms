package com.wms.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val authoritiesConverter = JwtGrantedAuthoritiesConverter().apply {
            setAuthoritiesClaimName("role")
            setAuthorityPrefix("ROLE_")
        }

        return JwtAuthenticationConverter().apply {
            setJwtGrantedAuthoritiesConverter(authoritiesConverter)
        }
    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationConverter: JwtAuthenticationConverter
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers(
                        HttpMethod.POST,
                        "/api/auth/login",
                        "/api/auth/bootstrap-admin"
                    ).permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/tenants/*/users")
                    .hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.GET, "/api/tenants/*/users")
                    .hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.PATCH, "/api/tenants/*/users/*/active")
                    .hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers(HttpMethod.PATCH, "/api/tenants/*/users/*/role")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/tenants", "/api/tenants/*")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/tenants")
                    .hasRole("ADMIN")

                    // Reports and audit history contain management-level information.
                    .requestMatchers("/api/tenants/*/audit-logs/**")
                    .hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers("/api/tenants/*/warehouses/*/reports/**")
                    .hasAnyRole("ADMIN", "MANAGER")

                    // Operators can execute warehouse workflows, including their state transitions.
                    .requestMatchers(
                        "/api/tenants/*/warehouses/*/receiving/**",
                        "/api/tenants/*/warehouses/*/transfers/**",
                        "/api/tenants/*/warehouses/*/shipments/**",
                        "/api/tenants/*/warehouses/*/returns/**",
                        "/api/tenants/*/warehouses/*/inventory-counts/**",
                        "/api/tenants/*/reservations/**"
                    ).hasAnyRole("ADMIN", "MANAGER", "OPERATOR")

                    // Only managers and admins may create or change master data and opening balances.
                    .requestMatchers(
                        HttpMethod.POST,
                        "/api/tenants/*/warehouses",
                        "/api/tenants/*/warehouses/*/zones",
                        "/api/tenants/*/warehouses/*/zones/*/locations",
                        "/api/tenants/*/products",
                        "/api/tenants/*/inventory"
                    ).hasAnyRole("ADMIN", "MANAGER")

                    // Operators may read the warehouse structure, catalog, balances and movement history.
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/tenants/*/warehouses",
                        "/api/tenants/*/warehouses/**",
                        "/api/tenants/*/products",
                        "/api/tenants/*/inventory",
                        "/api/tenants/*/inventory-movements/**"
                    ).hasAnyRole("ADMIN", "MANAGER", "OPERATOR")

                    // Any other tenant-scoped operation is reserved for managers and admins.
                    .requestMatchers("/api/tenants/*/**")
                    .hasAnyRole("ADMIN", "MANAGER")
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)
                }
            }

        return http.build()
    }
}
