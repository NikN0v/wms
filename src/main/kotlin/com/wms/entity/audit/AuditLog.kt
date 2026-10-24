package com.wms.entity.audit

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime

@Entity
@Table(name = "audit_logs")
class AuditLog(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "tenant_id", nullable = false)
    val tenantId: Long,

    @Column(name = "user_id")
    val userId: Long? = null,

    @Column(name = "action", nullable = false, length = 100)
    val action: String,

    @Column(name = "entity_type", nullable = false, length = 100)
    val entityType: String,

    @Column(name = "entity_id")
    val entityId: Long? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = "jsonb")
    val details: Map<String, Any?>? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)