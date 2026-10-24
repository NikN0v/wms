package com.wms.service.shipment

import com.wms.dto.shipment.AddShipmentLineRequest
import com.wms.dto.shipment.CreateShipmentRequest
import com.wms.dto.shipment.ShipmentLineResponse
import com.wms.dto.shipment.ShipmentResponse
import com.wms.entity.location.StorageLocation
import com.wms.entity.movement.InventoryMovement
import com.wms.entity.movement.InventoryMovementType
import com.wms.entity.reservation.ReservationStatus
import com.wms.entity.shipment.Shipment
import com.wms.entity.shipment.ShipmentLine
import com.wms.entity.shipment.ShipmentStatus
import com.wms.repository.inventory.InventoryRepository
import com.wms.repository.location.StorageLocationRepository
import com.wms.repository.movement.InventoryMovementRepository
import com.wms.repository.product.ProductRepository
import com.wms.repository.reservation.StockReservationRepository
import com.wms.repository.shipment.ShipmentLineRepository
import com.wms.repository.shipment.ShipmentRepository
import com.wms.repository.warehouse.WarehouseRepository
import com.wms.service.audit.AuditLogService
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime

@Service
class ShipmentService(
    private val shipmentRepository: ShipmentRepository,
    private val shipmentLineRepository: ShipmentLineRepository,
    private val warehouseRepository: WarehouseRepository,
    private val productRepository: ProductRepository,
    private val locationRepository: StorageLocationRepository,
    private val inventoryRepository: InventoryRepository,
    private val movementRepository: InventoryMovementRepository,
    private val reservationRepository: StockReservationRepository,
    private val auditLogService: AuditLogService
) {

    @Transactional
    fun createDocument(
        tenantId: Long,
        warehouseId: Long,
        request: CreateShipmentRequest
    ): ShipmentResponse {
        val warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Склад не найден"
                )
            }

        if (warehouse.tenant.id != tenantId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Склад не принадлежит компании"
            )
        }

        if (
            shipmentRepository.existsByTenantIdAndDocumentNumber(
                tenantId,
                request.documentNumber
            )
        ) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Документ с таким номером уже существует"
            )
        }

        val shipment = shipmentRepository.save(
            Shipment(
                tenantId = tenantId,
                warehouse = warehouse,
                documentNumber = request.documentNumber,
                notes = request.notes
            )
        )

        return toResponse(shipment)
    }

    @Transactional
    fun addLine(
        tenantId: Long,
        warehouseId: Long,
        documentId: Long,
        request: AddShipmentLineRequest
    ): ShipmentResponse {
        val document = getDocument(
            documentId,
            tenantId,
            warehouseId
        )

        ensureDraft(document)

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
                "Нельзя отгрузить неактивный товар"
            )
        }

        val location = locationRepository.findById(request.locationId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Ячейка не найдена"
                )
            }

        validateLocation(
            location,
            tenantId,
            warehouseId
        )

        val reservation = request.reservationId?.let { reservationId ->
            val found = reservationRepository
                .findForUpdate(reservationId, tenantId)
                .orElseThrow {
                    ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Резерв не найден"
                    )
                }

            if (found.status != ReservationStatus.ACTIVE) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Резерв не активен"
                )
            }

            if (
                found.product.id != request.productId ||
                found.locationId != request.locationId
            ) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Резерв не соответствует товару или ячейке"
                )
            }

            if (found.quantity.compareTo(request.quantity) != 0) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Количество отгрузки должно совпадать с количеством резерва"
                )
            }

            if (shipmentLineRepository.existsByReservationId(reservationId)) {
                throw ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Резерв уже используется в другой строке отгрузки"
                )
            }

            found
        }

        shipmentLineRepository.save(
            ShipmentLine(
                document = document,
                tenantId = tenantId,
                product = product,
                locationId = request.locationId,
                quantity = request.quantity,
                reservationId = reservation?.id
            )
        )

        return toResponse(document)
    }

    @Transactional
    fun getAll(
        tenantId: Long,
        warehouseId: Long
    ): List<ShipmentResponse> {
        validateWarehouse(tenantId, warehouseId)

        return shipmentRepository
            .findAllByTenantIdAndWarehouse_IdOrderByCreatedAtDesc(
                tenantId,
                warehouseId
            )
            .map(::toResponse)
    }

    @Transactional
    fun getById(
        tenantId: Long,
        warehouseId: Long,
        documentId: Long
    ): ShipmentResponse {
        return toResponse(
            getDocument(documentId, tenantId, warehouseId)
        )
    }

    @Transactional
    fun post(
        tenantId: Long,
        warehouseId: Long,
        documentId: Long
    ): ShipmentResponse {
        val document = shipmentRepository
            .findForUpdate(documentId, tenantId, warehouseId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Документ отгрузки не найден"
                )
            }

        ensureDraft(document)

        val lines = shipmentLineRepository
            .findAllByDocument_IdOrderByIdAsc(documentId)

        if (lines.isEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Нельзя провести пустой документ"
            )
        }

        for (line in lines) {
            val location = locationRepository
                .findById(line.locationId)
                .orElseThrow {
                    ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Ячейка ${line.locationId} не найдена"
                    )
                }

            validateLocation(
                location,
                tenantId,
                warehouseId
            )

            val inventory = inventoryRepository.findForUpdate(
                tenantId,
                line.product.id!!,
                line.locationId
            ).orElseThrow {
                ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "В ячейке нет остатка этого товара"
                )
            }

            val reservation = line.reservationId?.let { reservationId ->
                reservationRepository
                    .findForUpdate(reservationId, tenantId)
                    .orElseThrow {
                        ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Резерв не найден"
                        )
                    }
            }

            if (reservation != null) {
                if (
                    reservation.status != ReservationStatus.ACTIVE ||
                    reservation.product.id != line.product.id ||
                    reservation.locationId != line.locationId ||
                    reservation.quantity.compareTo(line.quantity) != 0
                ) {
                    throw ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Резерв недействителен или не соответствует строке отгрузки"
                    )
                }

                if (
                    inventory.quantity.compareTo(line.quantity) < 0 ||
                    inventory.reservedQuantity.compareTo(line.quantity) < 0
                ) {
                    throw ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Физический остаток или зарезервированное количество недостаточны"
                    )
                }

                inventory.reservedQuantity =
                    inventory.reservedQuantity.subtract(line.quantity)

                reservation.status = ReservationStatus.CONSUMED
                reservationRepository.save(reservation)
            } else {
                if (inventory.availableQuantity.compareTo(line.quantity) < 0) {
                    throw ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Недостаточно доступного товара для отгрузки"
                    )
                }
            }

            inventory.quantity = inventory.quantity.subtract(line.quantity)
            inventory.updatedAt = OffsetDateTime.now()
            inventoryRepository.save(inventory)

            movementRepository.save(
                InventoryMovement(
                    tenantId = tenantId,
                    product = line.product,
                    locationId = line.locationId,
                    movementType = InventoryMovementType.SHIPMENT,
                    quantity = line.quantity
                )
            )
        }

        document.status = ShipmentStatus.POSTED
        document.postedAt = OffsetDateTime.now()

        shipmentRepository.save(document)

        auditLogService.log(
            tenantId = tenantId,
            action = "SHIPMENT_POSTED",
            entityType = "SHIPMENT",
            entityId = document.id,
            details = mapOf(
                "documentNumber" to document.documentNumber,
                "linesCount" to lines.size,
                "status" to document.status.name,
                "reservationsUsed" to lines.count { it.reservationId != null }
            )
        )

        return toResponse(document)
    }

    private fun getDocument(
        documentId: Long,
        tenantId: Long,
        warehouseId: Long
    ): Shipment {
        return shipmentRepository
            .findByIdAndTenantIdAndWarehouse_Id(
                documentId,
                tenantId,
                warehouseId
            )
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Документ отгрузки не найден"
                )
            }
    }

    private fun validateWarehouse(
        tenantId: Long,
        warehouseId: Long
    ) {
        val warehouse = warehouseRepository.findById(warehouseId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Склад не найден"
                )
            }

        if (warehouse.tenant.id != tenantId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Склад не принадлежит компании"
            )
        }
    }

    private fun validateLocation(
        location: StorageLocation,
        tenantId: Long,
        warehouseId: Long
    ) {
        if (
            location.tenantId != tenantId ||
            location.zone.warehouse.id != warehouseId
        ) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Ячейка не принадлежит указанному складу"
            )
        }

        if (!location.isActive) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Ячейка неактивна"
            )
        }
    }

    private fun ensureDraft(document: Shipment) {
        if (document.status != ShipmentStatus.DRAFT) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Операция доступна только для документа DRAFT"
            )
        }
    }

    private fun toResponse(document: Shipment): ShipmentResponse {
        val documentId = requireNotNull(document.id) {
            "ID документа отгрузки не должен быть null"
        }

        val lines = shipmentLineRepository
            .findAllByDocument_IdOrderByIdAsc(documentId)

        return ShipmentResponse(
            id = documentId,
            tenantId = document.tenantId,
            warehouseId = requireNotNull(document.warehouse.id),
            documentNumber = document.documentNumber,
            status = document.status,
            notes = document.notes,
            createdAt = document.createdAt,
            postedAt = document.postedAt,
            lines = lines.map { line ->
                val location = locationRepository
                    .findById(line.locationId)
                    .orElseThrow {
                        ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Ячейка ${line.locationId} не найдена"
                        )
                    }

                ShipmentLineResponse(
                    id = requireNotNull(line.id),
                    productId = requireNotNull(line.product.id),
                    sku = line.product.sku,
                    productName = line.product.name,
                    locationId = requireNotNull(location.id),
                    reservationId = line.reservationId,
                    locationCode = location.code,
                    quantity = line.quantity
                )
            }
        )
    }
}
