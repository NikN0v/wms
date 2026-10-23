
package com.wms.entity.returns

import com.wms.entity.product.Product
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "stock_return_lines")
class StockReturnLine(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    val document: StockReturn,

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

    @Column(nullable = false, precision = 14, scale = 3)
    val quantity: BigDecimal
)
