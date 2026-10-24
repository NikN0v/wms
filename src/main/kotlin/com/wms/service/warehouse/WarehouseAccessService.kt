package com.wms.service.warehouse

import com.wms.entity.user.UserRole
import com.wms.repository.location.StorageLocationRepository
import com.wms.repository.user.UserRepository
import com.wms.repository.user.UserWarehouseAssignmentRepository
import com.wms.repository.warehouse.WarehouseRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class WarehouseAccessService(
    private val assignmentRepository: UserWarehouseAssignmentRepository,
    private val userRepository: UserRepository,
    private val warehouseRepository: WarehouseRepository,
    private val locationRepository: StorageLocationRepository
) {
    @Transactional(readOnly = true)
    fun validateActor(tenantId: Long, userId: Long, tokenRole: UserRole) {
        val user = userRepository.findByIdAndTenantId(userId, tenantId).orElseThrow {
            ResponseStatusException(HttpStatus.FORBIDDEN, "Учетная запись недоступна")
        }
        if (!user.isActive || user.role != tokenRole) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Права токена устарели или учетная запись отключена")
        }
    }

    @Transactional(readOnly = true)
    fun getAssignedWarehouseIds(tenantId: Long, userId: Long): List<Long> {
        val target = userRepository.findByIdAndTenantId(userId, tenantId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден")
        }
        if (target.role != UserRole.OPERATOR) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Назначать склады можно только оператору")
        }
        return assignmentRepository.findWarehouseIds(tenantId, userId)
    }

    @Transactional(readOnly = true)
    fun hasAccess(tenantId: Long, userId: Long, warehouseId: Long): Boolean =
        assignmentRepository.exists(tenantId, userId, warehouseId)

    @Transactional
    fun replaceAssignments(
        tenantId: Long,
        targetUserId: Long,
        warehouseIds: List<Long>,
        actorRole: UserRole
    ): List<Long> {
        if (actorRole !in setOf(UserRole.ADMIN, UserRole.MANAGER)) forbidden()
        if (warehouseIds.any { it <= 0 } || warehouseIds.distinct().size != warehouseIds.size) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Список складов содержит неверные или повторяющиеся ID")
        }

        val target = userRepository.findForUpdateByIdAndTenantId(tenantId, targetUserId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден")
        }
        if (target.role != UserRole.OPERATOR) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Назначать склады можно только оператору")
        }

        val warehouses = if (warehouseIds.isEmpty()) emptyList() else
            warehouseRepository.findAllByTenant_IdAndIdIn(tenantId, warehouseIds)
        if (warehouses.size != warehouseIds.size) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Все склады должны принадлежать указанной компании")
        }

        assignmentRepository.deleteAll(tenantId, targetUserId)
        warehouseIds.forEach { assignmentRepository.insert(tenantId, targetUserId, it) }
        return warehouseIds.sorted()
    }

    fun requireAccess(tenantId: Long, userId: Long, role: UserRole, warehouseId: Long) {
        if (role == UserRole.ADMIN || role == UserRole.MANAGER) return
        if (role != UserRole.OPERATOR || !hasAccess(tenantId, userId, warehouseId)) forbidden()
    }

    fun requireLocationAccess(tenantId: Long, userId: Long, role: UserRole, locationId: Long) {
        if (role == UserRole.ADMIN || role == UserRole.MANAGER) return
        val warehouseId = locationRepository.findWarehouseIdByIdAndTenantId(locationId, tenantId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Ячейка не найдена")
        requireAccess(tenantId, userId, role, warehouseId)
    }

    private fun forbidden(): Nothing =
        throw ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа к этому складу")
}
