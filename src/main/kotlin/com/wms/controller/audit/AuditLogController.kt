package com.wms.controller.audit

import com.wms.dto.audit.AuditLogFilterRequest
import com.wms.dto.audit.AuditLogResponse
import com.wms.entity.audit.AuditLog
import com.wms.service.audit.AuditLogService
import org.springframework.data.domain.Page
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime

@RestController
@RequestMapping("/api/tenants/{tenantId}/audit-logs")
class AuditLogController(
    private val auditLogService: AuditLogService
) {

    @GetMapping
    fun getTenantHistory(
        @PathVariable tenantId: Long
    ): List<AuditLog> {
        return auditLogService.getTenantHistory(tenantId)
    }

    @GetMapping("/{entityType}/{entityId}")
    fun getEntityHistory(
        @PathVariable tenantId: Long,
        @PathVariable entityType: String,
        @PathVariable entityId: Long
    ): List<AuditLog> {
        return auditLogService.getEntityHistory(
            tenantId = tenantId,
            entityType = entityType,
            entityId = entityId
        )
    }

    @GetMapping("/search")
    fun search(
        @PathVariable tenantId: Long,
        @RequestParam(required = false) action: String?,
        @RequestParam(required = false) entityType: String?,
        @RequestParam(required = false) entityId: Long?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        from: OffsetDateTime?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        to: OffsetDateTime?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): Page<AuditLogResponse> =
        auditLogService.search(
            tenantId = tenantId,
            filter = AuditLogFilterRequest(
                action = action,
                entityType = entityType,
                entityId = entityId,
                from = from,
                to = to,
                page = page,
                size = size
            )
        )
}
