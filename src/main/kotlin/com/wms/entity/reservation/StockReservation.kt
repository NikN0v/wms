package com.wms.entity.reservation

import com.wms.entity.product.Product
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(name = "stock_reservations")
class StockReservation(
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

    @Column(nullable = false, precision = 18, scale = 3)
    val quantity: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ReservationStatus = ReservationStatus.ACTIVE,

    @Column(name = "reference_type", length = 50)
    val referenceType: String? = null,

    @Column(name = "reference_id", length = 100)
    val referenceId: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "released_at")
    var releasedAt: OffsetDateTime? = null
)