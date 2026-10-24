package com.wms.config

import authorizationprobe.AuthorizationProbeController
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@WebMvcTest(controllers = [AuthorizationProbeController::class])
@Import(
    AuthorizationProbeController::class,
    SecurityConfig::class,
    TenantAccessInterceptor::class,
    TenantAccessWebConfiguration::class
)
@EnableWebSecurity
class TenantAuthorizationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var jwtDecoder: JwtDecoder

    @Test
    fun `operator can read warehouse and run warehouse workflows`() {
        mockMvc.perform(
            get("/api/tenants/1/warehouses")
                .with(jwtAs("OPERATOR", tenantId = 1L))
        ).andExpect(status().isOk)

        mockMvc.perform(
            get("/api/tenants/1/products")
                .with(jwtAs("OPERATOR", tenantId = 1L))
        ).andExpect(status().isOk)

        mockMvc.perform(
            post("/api/tenants/1/warehouses/7/shipments/22/post")
                .with(jwtAs("OPERATOR", tenantId = 1L))
        ).andExpect(status().isOk)
    }

    @Test
    fun `operator cannot edit master data or read management data`() {
        mockMvc.perform(
            post("/api/tenants/1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtAs("OPERATOR", tenantId = 1L))
        ).andExpect(status().isForbidden)

        mockMvc.perform(
            get("/api/tenants/1/audit-logs")
                .with(jwtAs("OPERATOR", tenantId = 1L))
        ).andExpect(status().isForbidden)

        mockMvc.perform(
            get("/api/tenants/1/warehouses/7/reports/stock-balances")
                .with(jwtAs("OPERATOR", tenantId = 1L))
        ).andExpect(status().isForbidden)

        mockMvc.perform(
            post("/api/tenants/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtAs("OPERATOR", tenantId = 1L))
        ).andExpect(status().isForbidden)

        mockMvc.perform(
            post("/api/tenants/1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtAs("OPERATOR", tenantId = 1L))
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `manager can manage tenant data but cannot manage companies globally`() {
        mockMvc.perform(
            post("/api/tenants/1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtAs("MANAGER", tenantId = 1L))
        ).andExpect(status().isOk)

        mockMvc.perform(
            get("/api/tenants/1/audit-logs")
                .with(jwtAs("MANAGER", tenantId = 1L))
        ).andExpect(status().isOk)

        mockMvc.perform(
            get("/api/tenants/1/warehouses/7/reports/stock-balances")
                .with(jwtAs("MANAGER", tenantId = 1L))
        ).andExpect(status().isOk)

        mockMvc.perform(
            post("/api/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtAs("MANAGER", tenantId = 1L))
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `admin can manage company data`() {
        mockMvc.perform(
            post("/api/tenants/1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtAs("ADMIN", tenantId = 1L))
        ).andExpect(status().isOk)

        mockMvc.perform(
            post("/api/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwtAs("ADMIN", tenantId = 1L))
        ).andExpect(status().isOk)
    }

    @Test
    fun `manager can list users and change operator activation but cannot change roles`() {
        mockMvc.perform(get("/api/tenants/1/users").with(jwtAs("MANAGER", tenantId = 1L)))
            .andExpect(status().isOk)
        mockMvc.perform(patch("/api/tenants/1/users/5/active").with(jwtAs("MANAGER", tenantId = 1L)))
            .andExpect(status().isOk)
        mockMvc.perform(patch("/api/tenants/1/users/5/role").with(jwtAs("MANAGER", tenantId = 1L)))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `operator cannot access user management`() {
        mockMvc.perform(get("/api/tenants/1/users").with(jwtAs("OPERATOR", tenantId = 1L)))
            .andExpect(status().isForbidden)
        mockMvc.perform(patch("/api/tenants/1/users/5/active").with(jwtAs("OPERATOR", tenantId = 1L)))
            .andExpect(status().isForbidden)
        mockMvc.perform(patch("/api/tenants/1/users/5/role").with(jwtAs("OPERATOR", tenantId = 1L)))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `admin can change user roles`() {
        mockMvc.perform(patch("/api/tenants/1/users/5/role").with(jwtAs("ADMIN", tenantId = 1L)))
            .andExpect(status().isOk)
    }

    @Test
    fun `tenant mismatch in JWT is forbidden`() {
        val result = mockMvc.perform(
            get("/api/tenants/1/warehouses")
                .with(jwtAs("OPERATOR", tenantId = 2L))
        ).andExpect(status().isForbidden)
            .andReturn()

        assertEquals(403, result.response.status)
    }

    private fun jwtAs(role: String, tenantId: Long) = jwt()
        .jwt { token -> token.claim("tenantId", tenantId) }
        .authorities(SimpleGrantedAuthority("ROLE_$role"))
}
