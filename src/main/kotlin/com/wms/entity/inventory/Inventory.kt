package com.wms.entity.inventory

import com.wms.entity.location.StorageLocation
import com.wms.entity.product.Product
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
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(
    name = "inventory",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_inventory_product_location",
            columnNames = ["tenant_id", "product_id", "location_id"]
        )
    ]
)
class Inventory(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "tenant_id", nullable = false)
    val tenantId: Long,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    val location: StorageLocation,

    @Column(nullable = false, precision = 18, scale = 3)
    var quantity: BigDecimal = BigDecimal.ZERO,

    @Column(name = "reserved_quantity", nullable = false, precision = 18, scale = 3)
    var reservedQuantity: BigDecimal = BigDecimal.ZERO,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
) {
    val availableQuantity: BigDecimal
        get() = quantity.subtract(reservedQuantity)
}