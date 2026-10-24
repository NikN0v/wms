
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
        request: CreateReservationRequest,
        allowedWarehouseIds: Set<Long>? = null
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

        if (allowedWarehouseIds != null && location.zone.warehouse.id !in allowedWarehouseIds) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа к этому складу")
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
        reservationId: Long,
        allowedWarehouseIds: Set<Long>? = null
    ): ReservationResponse {
        val reservation = reservationRepository
            .findForUpdate(reservationId, tenantId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Резерв не найден"
                )
            }

        requireReservationWarehouseAccess(tenantId, reservation.locationId, allowedWarehouseIds)

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
    fun getAll(tenantId: Long, allowedWarehouseIds: Set<Long>? = null): List<ReservationResponse> {
        if (allowedWarehouseIds != null && allowedWarehouseIds.isEmpty()) return emptyList()
        val result = reservationRepository
            .findAllByTenantIdOrderByCreatedAtDesc(tenantId)
            .map(::toResponse)
        return if (allowedWarehouseIds == null) result else result.filter { it.warehouseId in allowedWarehouseIds }
    }

    @Transactional(readOnly = true)
    fun getById(
        tenantId: Long,
        reservationId: Long,
        allowedWarehouseIds: Set<Long>? = null
    ): ReservationResponse {
        val reservation = reservationRepository
            .findByIdAndTenantId(reservationId, tenantId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Резерв не найден"
                )
            }

        val response = toResponse(reservation)
        if (allowedWarehouseIds != null && response.warehouseId !in allowedWarehouseIds) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа к этому складу")
        }
        return response
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
            warehouseId = locationRepository.findWarehouseIdByIdAndTenantId(reservation.locationId, reservation.tenantId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Ячейка не найдена"),
            quantity = reservation.quantity,
            status = reservation.status,
            referenceType = reservation.referenceType,
            referenceId = reservation.referenceId,
            createdAt = reservation.createdAt,
            releasedAt = reservation.releasedAt
        )

    private fun requireReservationWarehouseAccess(
        tenantId: Long,
        locationId: Long,
        allowedWarehouseIds: Set<Long>?
    ) {
        if (allowedWarehouseIds == null) return
        val warehouseId = locationRepository.findWarehouseIdByIdAndTenantId(locationId, tenantId)
            ?: throw ResponseStatusException(HttpStatus.CONFLICT, "Не найдена ячейка резерва")
        if (warehouseId !in allowedWarehouseIds) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа к этому складу")
        }
    }
}
