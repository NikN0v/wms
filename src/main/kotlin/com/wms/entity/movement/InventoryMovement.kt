package com.wms.entity.movement

import com.wms.entity.product.Product
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(
    name = "inventory_movements",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_movement_receiving_line",
            columnNames = ["receiving_line_id"]
        )
    ]
)
class InventoryMovement(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "tenant_id", nullable = false)
    val tenantId: Long,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(name = "location_id", nullable = false)
    val locationId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 30)
    val movementType: InventoryMovementType,

    @Column(nullable = false, precision = 18, scale = 3)
    val quantity: BigDecimal,

    @Column(name = "receiving_document_id")
    val receivingDocumentId: Long? = null,

    @Column(name = "receiving_line_id")
    val receivingLineId: Long? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)