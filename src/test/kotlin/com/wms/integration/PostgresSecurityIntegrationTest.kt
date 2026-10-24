package com.wms.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.time.Instant
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "WMS_TEST_DB_URL", matches = ".+")
class PostgresSecurityIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var jwtEncoder: JwtEncoder

    private var tenantId: Long = 0

    @BeforeEach
    fun prepareTestTenant() {
        jdbcTemplate.update("DELETE FROM users")
        jdbcTemplate.update(
            """
            INSERT INTO tenants (name, code)
            VALUES ('Integration Test Tenant', 'CODEX_INTEGRATION_TEST')
            ON CONFLICT (code) DO NOTHING
            """.trimIndent()
        )
        tenantId = requireNotNull(
            jdbcTemplate.queryForObject(
                "SELECT id FROM tenants WHERE code = ?",
                Long::class.java,
                "CODEX_INTEGRATION_TEST"
            )
        )
    }

    @AfterEach
    fun removeTestUsers() {
        jdbcTemplate.update("DELETE FROM users")
    }

    @Test
    fun `bootstrap creates one admin and refuses a second bootstrap`() {
        val first = bootstrapRequest()
        mockMvc.perform(first)
            .andExpect(status().isCreated)

        mockMvc.perform(bootstrapRequest())
            .andExpect(status().isConflict)

        assertEquals(1, userCount())
        assertEquals(
            "ADMIN",
            jdbcTemplate.queryForObject(
                "SELECT role FROM users WHERE tenant_id = ?",
                String::class.java,
                tenantId
            )
        )
    }

    @Test
    fun `parallel bootstrap requests create exactly one admin`() {
        val requestCount = 8
        val executor = Executors.newFixedThreadPool(requestCount)
        val ready = CountDownLatch(requestCount)
        val start = CountDownLatch(1)

        try {
            val requests = (1..requestCount).map {
                executor.submit<Int> {
                    ready.countDown()
                    check(start.await(10, TimeUnit.SECONDS))
                    mockMvc.perform(bootstrapRequest()).andReturn().response.status
                }
            }

            assertTrue(ready.await(10, TimeUnit.SECONDS), "Requests did not reach the start barrier")
            start.countDown()
            val statuses = requests.map { it.get(30, TimeUnit.SECONDS) }

            assertEquals(1, statuses.count { it == 201 })
            assertEquals(requestCount - 1, statuses.count { it == 409 })
            assertEquals(1, userCount())
        } finally {
            start.countDown()
            executor.shutdownNow()
        }
    }

    @Test
    fun `invalid bootstrap secret is rejected`() {
        mockMvc.perform(bootstrapRequest(secret = "wrong-secret"))
            .andExpect(status().isForbidden)

        assertEquals(0, userCount())
    }

    @Test
    fun `expired signed JWT is rejected by resource server`() {
        val now = Instant.now()
        val claims = JwtClaimsSet.builder()
            .issuer("wms-api")
            .issuedAt(now.minusSeconds(120))
            .expiresAt(now.minusSeconds(60))
            .subject("1")
            .claim("tenantId", tenantId)
            .claim("role", "ADMIN")
            .build()
        val token = jwtEncoder.encode(
            JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims
            )
        ).tokenValue

        mockMvc.perform(
            get("/api/tenants/$tenantId/warehouses")
                .header("Authorization", "Bearer $token")
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `manager lists users and changes only operator activation`() {
        val managerId = insertUser(tenantId, "integration.manager", "MANAGER")
        val operatorId = insertUser(tenantId, "integration.operator", "OPERATOR")
        val adminId = insertUser(tenantId, "integration.admin", "ADMIN")
        val token = token(managerId, tenantId, "MANAGER")

        mockMvc.perform(
            get("/api/tenants/$tenantId/users").header("Authorization", "Bearer $token")
        ).andExpect(status().isOk)

        mockMvc.perform(
            patch("/api/tenants/$tenantId/users/$operatorId/active")
                .header("Authorization", "Bearer $token")
                .contentType("application/json")
                .content("{\"active\":false}")
        ).andExpect(status().isOk)

        mockMvc.perform(
            patch("/api/tenants/$tenantId/users/$adminId/active")
                .header("Authorization", "Bearer $token")
                .contentType("application/json")
                .content("{\"active\":false}")
        ).andExpect(status().isForbidden)

        mockMvc.perform(
            patch("/api/tenants/$tenantId/users/$operatorId/role")
                .header("Authorization", "Bearer $token")
                .contentType("application/json")
                .content("{\"role\":\"MANAGER\"}")
        ).andExpect(status().isForbidden)

        assertEquals(false, jdbcTemplate.queryForObject("SELECT is_active FROM users WHERE id = ?", Boolean::class.java, operatorId))
        assertEquals(true, jdbcTemplate.queryForObject("SELECT is_active FROM users WHERE id = ?", Boolean::class.java, adminId))
    }

    @Test
    fun `admin promotes operator but cannot assign admin role`() {
        val adminId = insertUser(tenantId, "integration.admin", "ADMIN")
        val operatorId = insertUser(tenantId, "integration.operator", "OPERATOR")
        val token = token(adminId, tenantId, "ADMIN")

        mockMvc.perform(
            patch("/api/tenants/$tenantId/users/$operatorId/role")
                .header("Authorization", "Bearer $token")
                .contentType("application/json")
                .content("{\"role\":\"MANAGER\"}")
        ).andExpect(status().isOk)
        assertEquals("MANAGER", jdbcTemplate.queryForObject("SELECT role FROM users WHERE id = ?", String::class.java, operatorId))

        mockMvc.perform(
            patch("/api/tenants/$tenantId/users/$operatorId/role")
                .header("Authorization", "Bearer $token")
                .contentType("application/json")
                .content("{\"role\":\"ADMIN\"}")
        ).andExpect(status().isBadRequest)
        assertEquals("MANAGER", jdbcTemplate.queryForObject("SELECT role FROM users WHERE id = ?", String::class.java, operatorId))
    }

    @Test
    fun `users endpoint rejects access to another tenant`() {
        val managerId = insertUser(tenantId, "integration.manager", "MANAGER")
        jdbcTemplate.update(
            "INSERT INTO tenants (name, code) VALUES ('Other Integration Tenant', 'CODEX_OTHER_TEST') ON CONFLICT (code) DO NOTHING"
        )
        val otherTenantId = jdbcTemplate.queryForObject(
            "SELECT id FROM tenants WHERE code = 'CODEX_OTHER_TEST'", Long::class.java
        )!!
        val token = token(managerId, tenantId, "MANAGER")

        mockMvc.perform(
            get("/api/tenants/$otherTenantId/users").header("Authorization", "Bearer $token")
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `manager cannot change activation of another manager`() {
        val actorId = insertUser(tenantId, "integration.manager.actor", "MANAGER")
        val targetId = insertUser(tenantId, "integration.manager.target", "MANAGER")
        val token = token(actorId, tenantId, "MANAGER")

        mockMvc.perform(
            patch("/api/tenants/$tenantId/users/$targetId/active")
                .header("Authorization", "Bearer $token")
                .contentType("application/json")
                .content("{\"active\":false}")
        ).andExpect(status().isForbidden)

        assertEquals(true, jdbcTemplate.queryForObject("SELECT is_active FROM users WHERE id = ?", Boolean::class.java, targetId))
    }

    @Test
    fun `user cannot change own activation or role and last admin remains active`() {
        val adminId = insertUser(tenantId, "integration.admin", "ADMIN")
        val token = token(adminId, tenantId, "ADMIN")

        mockMvc.perform(
            patch("/api/tenants/$tenantId/users/$adminId/active")
                .header("Authorization", "Bearer $token")
                .contentType("application/json")
                .content("{\"active\":false}")
        ).andExpect(status().isConflict)

        mockMvc.perform(
            patch("/api/tenants/$tenantId/users/$adminId/role")
                .header("Authorization", "Bearer $token")
                .contentType("application/json")
                .content("{\"role\":\"MANAGER\"}")
        ).andExpect(status().isConflict)

        assertEquals("ADMIN", jdbcTemplate.queryForObject("SELECT role FROM users WHERE id = ?", String::class.java, adminId))
        assertEquals(true, jdbcTemplate.queryForObject("SELECT is_active FROM users WHERE id = ?", Boolean::class.java, adminId))
    }

    @Test
    fun `PATCH user operations reject another tenant`() {
        val adminId = insertUser(tenantId, "integration.admin", "ADMIN")
        jdbcTemplate.update(
            "INSERT INTO tenants (name, code) VALUES ('Other Integration Tenant', 'CODEX_OTHER_TEST') ON CONFLICT (code) DO NOTHING"
        )
        val otherTenantId = jdbcTemplate.queryForObject(
            "SELECT id FROM tenants WHERE code = 'CODEX_OTHER_TEST'", Long::class.java
        )!!
        val otherUserId = insertUser(otherTenantId, "other.tenant.user", "OPERATOR")
        val token = token(adminId, tenantId, "ADMIN")

        mockMvc.perform(
            patch("/api/tenants/$otherTenantId/users/$otherUserId/active")
                .header("Authorization", "Bearer $token")
                .contentType("application/json")
                .content("{\"active\":false}")
        ).andExpect(status().isForbidden)

        mockMvc.perform(
            patch("/api/tenants/$otherTenantId/users/$otherUserId/role")
                .header("Authorization", "Bearer $token")
                .contentType("application/json")
                .content("{\"role\":\"MANAGER\"}")
        ).andExpect(status().isForbidden)

        assertEquals("OPERATOR", jdbcTemplate.queryForObject("SELECT role FROM users WHERE id = ?", String::class.java, otherUserId))
        assertEquals(true, jdbcTemplate.queryForObject("SELECT is_active FROM users WHERE id = ?", Boolean::class.java, otherUserId))
    }

    @Test
    fun `operator sees and accesses only assigned warehouses`() {
        val adminId = insertUser(tenantId, "integration.admin", "ADMIN")
        val operatorId = insertUser(tenantId, "integration.operator", "OPERATOR")
        val assignedWarehouseId = insertWarehouse(tenantId, "ASSIGNED")
        val otherWarehouseId = insertWarehouse(tenantId, "UNASSIGNED")
        val adminToken = token(adminId, tenantId, "ADMIN")
        val operatorToken = token(operatorId, tenantId, "OPERATOR")

        mockMvc.perform(
            put("/api/tenants/$tenantId/users/$operatorId/warehouses")
                .header("Authorization", "Bearer $adminToken")
                .contentType("application/json")
                .content("{\"warehouseIds\":[$assignedWarehouseId]}")
        ).andExpect(status().isOk)

        mockMvc.perform(
            get("/api/tenants/$tenantId/users/$operatorId/warehouses")
                .header("Authorization", "Bearer $adminToken")
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$[0]").value(assignedWarehouseId))

        mockMvc.perform(
            get("/api/tenants/$tenantId/warehouses")
                .header("Authorization", "Bearer $operatorToken")
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(assignedWarehouseId))

        mockMvc.perform(
            get("/api/tenants/$tenantId/warehouses/$assignedWarehouseId/zones")
                .header("Authorization", "Bearer $operatorToken")
        ).andExpect(status().isOk)

        mockMvc.perform(
            get("/api/tenants/$tenantId/warehouses/$otherWarehouseId/zones")
                .header("Authorization", "Bearer $operatorToken")
        ).andExpect(status().isForbidden)

        mockMvc.perform(
            patch("/api/tenants/$tenantId/users/$operatorId/role")
                .header("Authorization", "Bearer $adminToken")
                .contentType("application/json")
                .content("{\"role\":\"MANAGER\"}")
        ).andExpect(status().isOk)

        mockMvc.perform(
            get("/api/tenants/$tenantId/warehouses/$assignedWarehouseId/zones")
                .header("Authorization", "Bearer $operatorToken")
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `cannot assign a warehouse from another tenant`() {
        val adminId = insertUser(tenantId, "integration.admin", "ADMIN")
        val operatorId = insertUser(tenantId, "integration.operator", "OPERATOR")
        jdbcTemplate.update(
            "INSERT INTO tenants (name, code) VALUES ('Other Integration Tenant', 'CODEX_OTHER_TEST') ON CONFLICT (code) DO NOTHING"
        )
        val otherTenantId = jdbcTemplate.queryForObject(
            "SELECT id FROM tenants WHERE code = 'CODEX_OTHER_TEST'", Long::class.java
        )!!
        val foreignWarehouseId = insertWarehouse(otherTenantId, "FOREIGN")
        val adminToken = token(adminId, tenantId, "ADMIN")

        mockMvc.perform(
            put("/api/tenants/$tenantId/users/$operatorId/warehouses")
                .header("Authorization", "Bearer $adminToken")
                .contentType("application/json")
                .content("{\"warehouseIds\":[$foreignWarehouseId]}")
        ).andExpect(status().isBadRequest)

        assertEquals(0, jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM user_warehouse_assignments WHERE tenant_id = ? AND user_id = ?",
            Int::class.java,
            tenantId,
            operatorId
        ))
    }

    @Test
    fun `operator tenant-level stock APIs return only assigned warehouse data`() {
        val adminId = insertUser(tenantId, "integration.admin", "ADMIN")
        val operatorId = insertUser(tenantId, "integration.operator", "OPERATOR")
        val assignedWarehouseId = insertWarehouse(tenantId, "STOCK_ASSIGNED")
        val otherWarehouseId = insertWarehouse(tenantId, "STOCK_OTHER")
        val assignedLocationId = insertLocation(tenantId, assignedWarehouseId, "A-01")
        val otherLocationId = insertLocation(tenantId, otherWarehouseId, "B-01")
        val productId = jdbcTemplate.queryForObject(
            "INSERT INTO products (tenant_id, sku, name) VALUES (?, ?, 'Test Product') RETURNING id",
            Long::class.java,
            tenantId,
            "T${System.nanoTime()}"
        )!!
        listOf(assignedLocationId, otherLocationId).forEach { locationId ->
            jdbcTemplate.update(
                "INSERT INTO inventory (tenant_id, product_id, location_id, quantity) VALUES (?, ?, ?, 10)",
                tenantId,
                productId,
                locationId
            )
            jdbcTemplate.update(
                "INSERT INTO inventory_movements (tenant_id, product_id, location_id, movement_type, quantity) VALUES (?, ?, ?, 'ADJUSTMENT', 1)",
                tenantId,
                productId,
                locationId
            )
            jdbcTemplate.update(
                "INSERT INTO stock_reservations (tenant_id, product_id, location_id, quantity) VALUES (?, ?, ?, 1)",
                tenantId,
                productId,
                locationId
            )
        }
        mockMvc.perform(
            put("/api/tenants/$tenantId/users/$operatorId/warehouses")
                .header("Authorization", "Bearer ${token(adminId, tenantId, "ADMIN")}")
                .contentType("application/json")
                .content("{\"warehouseIds\":[$assignedWarehouseId]}")
        ).andExpect(status().isOk)
        val operatorToken = token(operatorId, tenantId, "OPERATOR")

        mockMvc.perform(get("/api/tenants/$tenantId/inventory").header("Authorization", "Bearer $operatorToken"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].locationId").value(assignedLocationId))
        mockMvc.perform(get("/api/tenants/$tenantId/inventory-movements").header("Authorization", "Bearer $operatorToken"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].warehouseId").value(assignedWarehouseId))
        mockMvc.perform(get("/api/tenants/$tenantId/reservations").header("Authorization", "Bearer $operatorToken"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].warehouseId").value(assignedWarehouseId))
    }

    private fun insertUser(tenantId: Long, username: String, role: String): Long =
        jdbcTemplate.queryForObject(
            "INSERT INTO users (tenant_id, username, password_hash, role, is_active) VALUES (?, ?, 'test-hash', ?, true) RETURNING id",
            Long::class.java,
            tenantId,
            username,
            role
        )!!

    private fun insertWarehouse(tenantId: Long, suffix: String): Long =
        jdbcTemplate.queryForObject(
            "INSERT INTO warehouses (tenant_id, name, code) VALUES (?, ?, ?) RETURNING id",
            Long::class.java,
            tenantId,
            "Integration $suffix",
            "T${System.nanoTime()}"
        )!!

    private fun insertLocation(tenantId: Long, warehouseId: Long, code: String): Long {
        val zoneId = jdbcTemplate.queryForObject(
            "INSERT INTO storage_zones (tenant_id, warehouse_id, name, code) VALUES (?, ?, 'Test Zone', ?) RETURNING id",
            Long::class.java,
            tenantId,
            warehouseId,
            "Z${System.nanoTime()}"
        )!!
        return jdbcTemplate.queryForObject(
            "INSERT INTO storage_locations (tenant_id, zone_id, code) VALUES (?, ?, ?) RETURNING id",
            Long::class.java,
            tenantId,
            zoneId,
            code
        )!!
    }

    private fun token(userId: Long, tenantId: Long, role: String): String {
        val now = Instant.now()
        val claims = JwtClaimsSet.builder()
            .issuer("wms-api")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(300))
            .subject(userId.toString())
            .claim("userId", userId)
            .claim("tenantId", tenantId)
            .claim("role", role)
            .build()
        return jwtEncoder.encode(
            JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)
        ).tokenValue
    }

    private fun bootstrapRequest(secret: String = TEST_BOOTSTRAP_SECRET) =
        post("/api/auth/bootstrap-admin")
            .header("X-Bootstrap-Secret", secret)
            .contentType("application/json")
            .content(
                """
                {
                  "tenantId": $tenantId,
                  "username": "integration.admin",
                  "password": "a-strong-password-123"
                }
                """.trimIndent()
            )

    private fun userCount(): Int =
        requireNotNull(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Int::class.java))

    companion object {
        private const val TEST_BOOTSTRAP_SECRET = "codex-integration-bootstrap-secret"
        private const val TEST_JWT_SECRET_BASE64 =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="

        @JvmStatic
        @DynamicPropertySource
        fun testProperties(registry: DynamicPropertyRegistry) {
            val url = requireNotNull(System.getenv("WMS_TEST_DB_URL"))
            val databaseName = url.substringBefore('?').substringAfterLast('/')
            require(databaseName.endsWith("_test", ignoreCase = true)) {
                "WMS_TEST_DB_URL must point to a database whose name ends with _test"
            }

            val username = requireNotNull(System.getenv("WMS_TEST_DB_USERNAME"))
            val password = requireNotNull(System.getenv("WMS_TEST_DB_PASSWORD"))

            registry.add("spring.datasource.url") { url }
            registry.add("spring.datasource.username") { username }
            registry.add("spring.datasource.password") { password }
            registry.add("security.jwt.secret") { TEST_JWT_SECRET_BASE64 }
            registry.add("security.bootstrap.secret") { TEST_BOOTSTRAP_SECRET }
        }
    }
}
