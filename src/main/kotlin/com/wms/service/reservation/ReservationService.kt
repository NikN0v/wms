
package com.wms.service.reservation

import com.wms.dto.reservation.CreateReservationRequest
import com.wms.dto.reservation.ReservationResponse
import com.wms.entity.reservation.ReservationStatus
import com.wms.entity.reservation.StockReservation
import com.wms.repository.inventory.InventoryRepository
import com.wms.repository.location.StorageLocationRepository
import com.wms.repository.product.ProductRepository
import com.wms.repository.reservation.StockReservationRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime

@Service
class ReservationService(
    private val reservationRepository: StockReservationRepository,
    private val inventoryRepository: InventoryRepository,
    private val productRepository: ProductRepository,
    private val locationRepository: StorageLocationRepository
) {

    @Transactional
    fun create(
        tenantId: Long,
        request: CreateReservationRequest
    ): ReservationResponse {
        val product = productRepository.findById(request.productId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Товар не найден"
                )
            }

        if (product.tenant.id != tenantId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Товар не принадлежит компании"
            )
        }

        if (!product.isActive) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Нельзя резервировать неактивный товар"
            )
        }

        val location = locationRepository.findById(request.locationId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Ячейка не найдена"
                )
            }

        if (location.tenantId != tenantId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Ячейка не принадлежит компании"
            )
        }

        if (!location.isActive) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Нельзя резервировать товар в неактивной ячейке"
            )
        }

        val inventory = inventoryRepository.findForUpdate(
            tenantId,
            request.productId,
            request.locationId
        ).orElseThrow {
            ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "В этой ячейке нет остатка товара"
            )
        }

        if (inventory.availableQuantity.compareTo(request.quantity) < 0) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Недостаточно доступного товара. " +
                        "Доступно: ${inventory.availableQuantity}, " +
                        "требуется: ${request.quantity}"
            )
        }

        inventory.reservedQuantity =
            inventory.reservedQuantity.add(request.quantity)

        inventory.updatedAt = OffsetDateTime.now()
        inventoryRepository.save(inventory)

        val reservation = reservationRepository.save(
            StockReservation(
                tenantId = tenantId,
                product = product,
                locationId = request.locationId,
                quantity = request.quantity,
                referenceType = request.referenceType,
                referenceId = request.referenceId
            )
        )

        return toResponse(reservation)
    }

    @Transactional
    fun release(
        tenantId: Long,
        reservationId: Long
    ): ReservationResponse {
        val reservation = reservationRepository
            .findForUpdate(reservationId, tenantId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Резерв не найден"
                )
            }

        if (reservation.status != ReservationStatus.ACTIVE) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Освободить можно только активный резерв"
            )
        }

        val inventory = inventoryRepository.findForUpdate(
            tenantId,
            reservation.product.id!!,
            reservation.locationId
        ).orElseThrow {
            ResponseStatusException(
                HttpStatus.CONFLICT,
                "Не найден остаток, связанный с резервом"
            )
        }

        if (inventory.reservedQuantity < reservation.quantity) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Зарезервированное количество не соответствует остатку"
            )
        }

        inventory.reservedQuantity =
            inventory.reservedQuantity.subtract(reservation.quantity)

        inventory.updatedAt = OffsetDateTime.now()
        inventoryRepository.save(inventory)

        reservation.status = ReservationStatus.RELEASED
        reservation.releasedAt = OffsetDateTime.now()

        reservationRepository.save(reservation)

        return toResponse(reservation)
    }

    @Transactional(readOnly = true)
    fun getAll(tenantId: Long): List<ReservationResponse> =
        reservationRepository
            .findAllByTenantIdOrderByCreatedAtDesc(tenantId)
            .map(::toResponse)

    @Transactional(readOnly = true)
    fun getById(
        tenantId: Long,
        reservationId: Long
    ): ReservationResponse {
        val reservation = reservationRepository
            .findByIdAndTenantId(reservationId, tenantId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Резерв не найден"
                )
            }

        return toResponse(reservation)
    }

    private fun toResponse(
        reservation: StockReservation
    ): ReservationResponse =
        ReservationResponse(
            id = requireNotNull(reservation.id),
            tenantId = reservation.tenantId,
            productId = requireNotNull(reservation.product.id),
            sku = reservation.product.sku,
            productName = reservation.product.name,
            locationId = reservation.locationId,
            quantity = reservation.quantity,
            status = reservation.status,
            referenceType = reservation.referenceType,
            referenceId = reservation.referenceId,
            createdAt = reservation.createdAt,
            releasedAt = reservation.releasedAt
        )
}
