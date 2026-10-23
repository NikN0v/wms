package com.wms.entity.transfers

import com.wms.entity.warehouse.Warehouse
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
import java.time.OffsetDateTime

@Entity
@Table(
    name = "stock_transfers",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_stock_transfer_document_number",
            columnNames = ["tenant_id", "document_number"]
        )
    ]
)
class StockTransfer(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "tenant_id", nullable = false)
    val tenantId: Long,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    val warehouse: Warehouse,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: StockTransferStatus = StockTransferStatus.DRAFT,

    @Column(name = "document_number", nullable = false, length = 100)
    val documentNumber: String,

    @Column(columnDefinition = "TEXT")
    val notes: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "posted_at")
    var postedAt: OffsetDateTime? = null
)