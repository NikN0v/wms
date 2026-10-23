package com.wms.entity.location

import com.wms.entity.zone.StorageZone
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
    name = "storage_locations",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_storage_locations_zone_code",
            columnNames = ["zone_id", "code"]
        )
    ]
)
class StorageLocation(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    val zone: StorageZone,

    @Column(name = "tenant_id", nullable = false)
    val tenantId: Long,

    @Column(nullable = false, length = 50)
    var code: String,

    @Column(nullable = false, length = 30)
    var type: String = "STANDARD",

    @Column(nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
)