package com.wms.entity.inventorycount

import com.wms.entity.product.Product
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(
    name = "inventory_count_lines",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_inventory_count_line",
            columnNames = ["count_id", "product_id", "location_id"]
        )
    ]
)
class InventoryCountLine(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "count_id", nullable = false)
    val document: InventoryCount,

    @Column(name = "tenant_id", nullable = false)
    val tenantId: Long,

    @Column(name = "product_id", nullable = false)
    val productId: Long,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "product_id",
        referencedColumnName = "id",
        insertable = false,
        updatable = false
    )
    val product: Product,

    @Column(name = "location_id", nullable = false)
    val locationId: Long,

    @Column(name = "system_quantity", nullable = false, precision = 14, scale = 3)
    val systemQuantity: BigDecimal,

    @Column(name = "actual_quantity", nullable = false, precision = 14, scale = 3)
    val actualQuantity: BigDecimal
)