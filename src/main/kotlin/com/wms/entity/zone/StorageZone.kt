package com.wms.entity.zone

import com.wms.entity.warehouse.Warehouse
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
    name = "storage_zones",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_storage_zones_warehouse_code",
            columnNames = ["warehouse_id", "code"]
        )
    ]
)
class StorageZone(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    val warehouse: Warehouse,

    @Column(name = "tenant_id", nullable = false)
    val tenantId: Long,

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