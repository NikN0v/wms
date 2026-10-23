package com.wms.entity.receiving

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
import java.time.OffsetDateTime

@Entity
@Table(name = "receiving_documents")
class ReceivingDocument(

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
    var status: ReceivingDocumentStatus = ReceivingDocumentStatus.DRAFT,

    @Column(name = "document_number", length = 100)
    val documentNumber: String? = null,

    @Column(columnDefinition = "TEXT")
    val notes: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "posted_at")
    var postedAt: OffsetDateTime? = null
)