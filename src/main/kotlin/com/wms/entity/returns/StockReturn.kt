package com.wms.entity.returns

import com.wms.entity.warehouse.Warehouse
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "stock_returns")
class StockReturn(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "tenant_id", nullable = false)
    val tenantId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    val warehouse: Warehouse,

    @Column(name = "document_number", nullable = false, length = 100)
    val documentNumber: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: StockReturnStatus = StockReturnStatus.DRAFT,

    @Column(columnDefinition = "TEXT")
    val notes: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "posted_at")
    var postedAt: OffsetDateTime? = null
)