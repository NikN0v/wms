package com.wms.entity.shipment

import com.wms.entity.warehouse.Warehouse
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(
    name = "shipments",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_shipment_document_number",
            columnNames = ["tenant_id", "document_number"]
        )
    ]
)
class Shipment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "tenant_id", nullable = false)
    val tenantId: Long,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    val warehouse: Warehouse,

    @Column(name = "document_number", nullable = false, length = 100)
    val documentNumber: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ShipmentStatus = ShipmentStatus.DRAFT,

    @Column(columnDefinition = "TEXT")
    val notes: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "posted_at")
    var postedAt: OffsetDateTime? = null
)