package com.wms.entity.warehouse

import com.wms.entity.tenant.Tenant
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.OffsetDateTime

@Entity
@Table(
    name = "warehouses",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_warehouses_tenant_code",
            columnNames = ["tenant_id", "code"]
        )
    ]
)
class Warehouse(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    val tenant: Tenant,

    @Column(nullable = false, length = 150)
    var name: String,

    @Column(nullable = false, length = 50)
    var code: String,

    @Column(nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
)