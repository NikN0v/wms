package com.wms.entity.transfers

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
import java.math.BigDecimal

@Entity
@Table(name = "stock_transfer_lines")
class StockTransferLine(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    val document: StockTransfer,

    @Column(name = "tenant_id", nullable = false)
    val tenantId: Long,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(name = "source_location_id", nullable = false)
    val sourceLocationId: Long,

    @Column(name = "target_location_id", nullable = false)
    val targetLocationId: Long,

    @Column(nullable = false, precision = 18, scale = 3)
    val quantity: BigDecimal
)