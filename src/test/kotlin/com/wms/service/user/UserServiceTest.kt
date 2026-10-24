package com.wms.service.user

import com.wms.dto.user.BootstrapAdminRequest
import com.wms.entity.tenant.Tenant
import com.wms.entity.user.User
import com.wms.entity.user.UserRole
import com.wms.repository.tenant.TenantRepository
import com.wms.repository.user.UserRepository
import jakarta.persistence.EntityManager
import org.hibernate.Session
import org.hibernate.jdbc.Work
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.server.ResponseStatusException
import java.sql.Connection
import java.sql.PreparedStatement
import java.util.Optional

class UserServiceTest {

    @Test
    fun `bootstrap creates first admin and tenant under database lock`() {
        val users = Mockito.mock(UserRepository::class.java)
        val tenants = Mockito.mock(TenantRepository::class.java)
        val encoder = Mockito.mock(PasswordEncoder::class.java)
        val entityManager = Mockito.mock(EntityManager::class.java)
        val lockStatement = mockAdvisoryLock(entityManager)
        val savedTenant = Tenant(id = 9L, name = "Acme", code = "ACME")
        val savedUser = User(
            id = 12L,
            tenantId = 9L,
            username = "root.admin",
            passwordHash = "bcrypt-hash",
            role = UserRole.ADMIN
        )

        Mockito.`when`(users.count()).thenReturn(0L)
        Mockito.`when`(tenants.existsByCode("ACME")).thenReturn(false)
        Mockito.`when`(tenants.save(Mockito.any(Tenant::class.java)))
            .thenReturn(savedTenant)
        Mockito.`when`(users.existsByTenantIdAndUsername(9L, "root.admin"))
            .thenReturn(false)
        Mockito.`when`(encoder.encode("a-strong-password-123"))
            .thenReturn("bcrypt-hash")
        Mockito.`when`(users.save(Mockito.any(User::class.java)))
            .thenReturn(savedUser)

        val response = service(users, tenants, encoder, entityManager)
            .bootstrapAdmin(
                request(tenantId = null, tenantName = " Acme ", tenantCode = "acme"),
                suppliedSecret = "bootstrap-secret"
            )

        assertEquals(12L, response.id)
        assertEquals(9L, response.tenantId)
        assertEquals(UserRole.ADMIN, response.role)
        Mockito.verify(lockStatement).setLong(1, 0x574D53424F4F54L)
        Mockito.verify(lockStatement).execute()

        val savedUserCaptor = ArgumentCaptor.forClass(User::class.java)
        Mockito.verify(users).save(savedUserCaptor.capture())
        assertEquals(UserRole.ADMIN, savedUserCaptor.value.role)
        assertEquals("root.admin", savedUserCaptor.value.username)
        assertEquals("bcrypt-hash", savedUserCaptor.value.passwordHash)
    }

    @Test
    fun `bootstrap rejects invalid secret before accessing the database`() {
        val users = Mockito.mock(UserRepository::class.java)
        val tenants = Mockito.mock(TenantRepository::class.java)
        val encoder = Mockito.mock(PasswordEncoder::class.java)
        val entityManager = Mockito.mock(EntityManager::class.java)

        val error = assertThrows(ResponseStatusException::class.java) {
            service(users, tenants, encoder, entityManager)
                .bootstrapAdmin(request(1L), suppliedSecret = "wrong")
        }

        assertEquals(HttpStatus.FORBIDDEN, error.statusCode)
        Mockito.verifyNoInteractions(users, tenants, entityManager)
    }

    @Test
    fun `bootstrap is disabled when no secret is configured`() {
        val users = Mockito.mock(UserRepository::class.java)
        val tenants = Mockito.mock(TenantRepository::class.java)
        val encoder = Mockito.mock(PasswordEncoder::class.java)
        val entityManager = Mockito.mock(EntityManager::class.java)

        val error = assertThrows(ResponseStatusException::class.java) {
            UserService(users, encoder, tenants, entityManager, bootstrapSecret = "")
                .bootstrapAdmin(request(1L), suppliedSecret = "any-value")
        }

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.statusCode)
        Mockito.verifyNoInteractions(users, tenants, entityManager)
    }

    @Test
    fun `bootstrap rejects repeated setup`() {
        val users = Mockito.mock(UserRepository::class.java)
        val tenants = Mockito.mock(TenantRepository::class.java)
        val encoder = Mockito.mock(PasswordEncoder::class.java)
        val entityManager = Mockito.mock(EntityManager::class.java)
        mockAdvisoryLock(entityManager)
        Mockito.`when`(users.count()).thenReturn(1L)

        val error = assertThrows(ResponseStatusException::class.java) {
            service(users, tenants, encoder, entityManager)
                .bootstrapAdmin(request(1L), suppliedSecret = "bootstrap-secret")
        }

        assertEquals(HttpStatus.CONFLICT, error.statusCode)
        Mockito.verifyNoInteractions(tenants, encoder)
        Mockito.verify(users, Mockito.never()).save(Mockito.any(User::class.java))
    }

    @Test
    fun `cannot deactivate the last active admin`() {
        val users = Mockito.mock(UserRepository::class.java)
        val tenants = Mockito.mock(TenantRepository::class.java)
        Mockito.`when`(tenants.findByIdForUpdate(9L)).thenReturn(Optional.of(Tenant(id = 9L, name = "Acme", code = "ACME")))
        Mockito.`when`(users.findForUpdateByIdAndTenantId(9L, 12L)).thenReturn(
            Optional.of(User(id = 12L, tenantId = 9L, username = "admin", passwordHash = "hash", role = UserRole.ADMIN))
        )
        Mockito.`when`(users.countByTenantIdAndRoleAndIsActiveTrue(9L, UserRole.ADMIN)).thenReturn(1L)

        val error = assertThrows(ResponseStatusException::class.java) {
            service(users, tenants, Mockito.mock(PasswordEncoder::class.java), Mockito.mock(EntityManager::class.java))
                .setActive(9L, 12L, 13L, UserRole.ADMIN, false)
        }

        assertEquals(HttpStatus.CONFLICT, error.statusCode)
    }

    @Test
    fun `API cannot assign admin role`() {
        val users = Mockito.mock(UserRepository::class.java)
        val error = assertThrows(ResponseStatusException::class.java) {
            service(users, Mockito.mock(TenantRepository::class.java), Mockito.mock(PasswordEncoder::class.java), Mockito.mock(EntityManager::class.java))
                .changeRole(9L, 12L, 13L, UserRole.ADMIN, UserRole.ADMIN)
        }

        assertEquals(HttpStatus.BAD_REQUEST, error.statusCode)
        Mockito.verifyNoInteractions(users)
    }

    @Test
    fun `manager cannot change role of operator`() {
        val users = Mockito.mock(UserRepository::class.java)
        val tenants = Mockito.mock(TenantRepository::class.java)
        Mockito.`when`(tenants.findByIdForUpdate(9L)).thenReturn(Optional.of(Tenant(id = 9L, name = "Acme", code = "ACME")))
        Mockito.`when`(users.findForUpdateByIdAndTenantId(9L, 12L)).thenReturn(
            Optional.of(User(id = 12L, tenantId = 9L, username = "operator", passwordHash = "hash", role = UserRole.OPERATOR))
        )

        val error = assertThrows(ResponseStatusException::class.java) {
            service(users, tenants, Mockito.mock(PasswordEncoder::class.java), Mockito.mock(EntityManager::class.java))
                .changeRole(9L, 12L, 13L, UserRole.MANAGER, UserRole.MANAGER)
        }

        assertEquals(HttpStatus.FORBIDDEN, error.statusCode)
    }

    private fun service(
        users: UserRepository,
        tenants: TenantRepository,
        encoder: PasswordEncoder,
        entityManager: EntityManager
    ) = UserService(
        userRepository = users,
        passwordEncoder = encoder,
        tenantRepository = tenants,
        entityManager = entityManager,
        bootstrapSecret = "bootstrap-secret"
    )

    private fun request(
        tenantId: Long?,
        tenantName: String? = null,
        tenantCode: String? = null
    ) = BootstrapAdminRequest(
        tenantId = tenantId,
        tenantName = tenantName,
        tenantCode = tenantCode,
        username = "Root.Admin",
        password = "a-strong-password-123"
    )

    private fun mockAdvisoryLock(entityManager: EntityManager): PreparedStatement {
        val session = Mockito.mock(Session::class.java)
        val connection = Mockito.mock(Connection::class.java)
        val statement = Mockito.mock(PreparedStatement::class.java)

        Mockito.`when`(entityManager.unwrap(Session::class.java)).thenReturn(session)
        Mockito.`when`(connection.prepareStatement("SELECT pg_advisory_xact_lock(?)"))
            .thenReturn(statement)
        Mockito.`when`(statement.execute()).thenReturn(true)
        Mockito.doAnswer { invocation ->
            invocation.getArgument<Work>(0).execute(connection)
            null
        }.`when`(session).doWork(Mockito.any(Work::class.java))

        return statement
    }
}
