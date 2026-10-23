package com.wms.entity.shipment

import com.wms.entity.product.Product
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "shipment_lines")
class ShipmentLine(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    val document: Shipment,

    @Column(name = "tenant_id", nullable = false)
    val tenantId: Long,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(name = "location_id", nullable = false)
    val locationId: Long,

    @Column(name = "reservation_id")
    val reservationId: Long? = null,

    @Column(nullable = false, precision = 18, scale = 3)
    val quantity: BigDecimal
)