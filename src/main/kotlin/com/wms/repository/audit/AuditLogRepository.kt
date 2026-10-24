package com.wms.repository.audit

import com.wms.entity.audit.AuditLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime

interface AuditLogRepository : JpaRepository<AuditLog, Long> {

    fun findAllByTenantIdOrderByCreatedAtDesc(
        tenantId: Long
    ): List<AuditLog>

    fun findAllByTenantIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
        tenantId: Long,
        entityType: String,
        entityId: Long
    ): List<AuditLog>

    @Query(
        value = """
        SELECT a.*
        FROM audit_logs a
        WHERE a.tenant_id = :tenantId
          AND (CAST(:action AS text) IS NULL OR a.action = CAST(:action AS text))
          AND (CAST(:entityType AS text) IS NULL OR a.entity_type = CAST(:entityType AS text))
          AND (CAST(:entityId AS bigint) IS NULL OR a.entity_id = CAST(:entityId AS bigint))
          AND (CAST(:fromDate AS timestamptz) IS NULL OR a.created_at >= CAST(:fromDate AS timestamptz))
          AND (CAST(:toDate AS timestamptz) IS NULL OR a.created_at <= CAST(:toDate AS timestamptz))
        ORDER BY a.created_at DESC, a.id DESC
    """,
        countQuery = """
        SELECT COUNT(*)
        FROM audit_logs a
        WHERE a.tenant_id = :tenantId
          AND (CAST(:action AS text) IS NULL OR a.action = CAST(:action AS text))
          AND (CAST(:entityType AS text) IS NULL OR a.entity_type = CAST(:entityType AS text))
          AND (CAST(:entityId AS bigint) IS NULL OR a.entity_id = CAST(:entityId AS bigint))
          AND (CAST(:fromDate AS timestamptz) IS NULL OR a.created_at >= CAST(:fromDate AS timestamptz))
          AND (CAST(:toDate AS timestamptz) IS NULL OR a.created_at <= CAST(:toDate AS timestamptz))
    """,
        nativeQuery = true
    )
    fun findFiltered(
        @Param("tenantId") tenantId: Long,
        @Param("action") action: String?,
        @Param("entityType") entityType: String?,
        @Param("entityId") entityId: Long?,
        @Param("fromDate") fromDate: OffsetDateTime?,
        @Param("toDate") toDate: OffsetDateTime?,
        pageable: Pageable
    ): Page<AuditLog>
}