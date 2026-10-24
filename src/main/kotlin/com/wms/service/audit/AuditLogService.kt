package com.wms.service.audit

import com.wms.dto.audit.AuditLogFilterRequest
import com.wms.dto.audit.AuditLogResponse
import com.wms.entity.audit.AuditLog
import com.wms.repository.audit.AuditLogRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class AuditLogService(
    private val auditLogRepository: AuditLogRepository
) {

    @Transactional
    fun log(
        tenantId: Long,
        action: String,
        entityType: String,
        entityId: Long? = null,
        userId: Long? = null,
        details: Map<String, Any?>? = null
    ): AuditLog {
        require(tenantId > 0) { "tenantId должен быть положительным" }
        require(action.isNotBlank()) { "action не должен быть пустым" }
        require(entityType.isNotBlank()) { "entityType не должен быть пустым" }

        val auditLog = AuditLog(
            tenantId = tenantId,
            userId = userId,
            action = action,
            entityType = entityType,
            entityId = entityId,
            details = details
        )

        return auditLogRepository.save(auditLog)
    }

    @Transactional(readOnly = true)
    fun getTenantHistory(tenantId: Long): List<AuditLog> {
        require(tenantId > 0) { "tenantId должен быть положительным" }

        return auditLogRepository
            .findAllByTenantIdOrderByCreatedAtDesc(tenantId)
    }

    @Transactional(readOnly = true)
    fun getEntityHistory(
        tenantId: Long,
        entityType: String,
        entityId: Long
    ): List<AuditLog> {
        require(tenantId > 0) { "tenantId должен быть положительным" }
        require(entityId > 0) { "entityId должен быть положительным" }
        require(entityType.isNotBlank()) { "entityType не должен быть пустым" }

        return auditLogRepository
            .findAllByTenantIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
                tenantId = tenantId,
                entityType = entityType,
                entityId = entityId
            )
    }

    @Transactional(readOnly = true)
    fun search(
        tenantId: Long,
        filter: AuditLogFilterRequest
    ): Page<AuditLogResponse> {
        if (tenantId <= 0) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "tenantId должен быть положительным"
            )
        }

        if (filter.page < 0) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Номер страницы не может быть отрицательным"
            )
        }

        if (filter.size !in 1..100) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Размер страницы должен быть от 1 до 100"
            )
        }

        if (filter.entityId != null && filter.entityId <= 0) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "entityId должен быть положительным"
            )
        }

        if (
            filter.from != null &&
            filter.to != null &&
            filter.from.isAfter(filter.to)
        ) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Дата начала периода не может быть позже даты окончания"
            )
        }

        val pageable = PageRequest.of(
            filter.page,
            filter.size
        )

        return auditLogRepository.findFiltered(
            tenantId = tenantId,
            action = filter.action?.trim()?.takeIf { it.isNotEmpty() },
            entityType = filter.entityType?.trim()?.takeIf { it.isNotEmpty() },
            entityId = filter.entityId,
            fromDate = filter.from,
            toDate = filter.to,
            pageable = pageable
        ).map { log ->
            AuditLogResponse(
                id = requireNotNull(log.id),
                tenantId = log.tenantId,
                userId = log.userId,
                action = log.action,
                entityType = log.entityType,
                entityId = log.entityId,
                details = log.details,
                createdAt = log.createdAt
            )
        }
    }
}