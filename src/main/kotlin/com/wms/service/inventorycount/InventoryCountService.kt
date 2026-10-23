
package com.wms.service.inventorycount

import com.wms.dto.inventorycount.*
import com.wms.entity.inventorycount.*
import com.wms.entity.movement.InventoryMovement
import com.wms.entity.movement.InventoryMovementType
import com.wms.repository.inventorycount.InventoryCountLineRepository
import com.wms.repository.inventorycount.InventoryCountRepository
import com.wms.repository.inventory.InventoryRepository
import com.wms.repository.location.StorageLocationRepository
import com.wms.repository.movement.InventoryMovementRepository
import com.wms.repository.product.ProductRepository
import com.wms.repository.warehouse.WarehouseRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.OffsetDateTime

@Service
class InventoryCountService(
    private val countRepository: InventoryCountRepository,
    private val lineRepository: InventoryCountLineRepository,
    private val warehouseRepository: WarehouseRepository,
    private val productRepository: ProductRepository,
    private val locationRepository: StorageLocationRepository,
    private val inventoryRepository: InventoryRepository,
    private val inventoryMovementRepository: InventoryMovementRepository
) {

    @Transactional
    fun createDocument(
        tenantId: Long,
        warehouseId: Long,
        request: CreateInventoryCountRequest
    ): InventoryCountResponse {
        val warehouse = warehouseRepository
            .findByIdAndTenantId(warehouseId, tenantId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Склад не найден"
                )
            }

        if (countRepository.existsByTenantIdAndDocumentNumber(
                tenantId,
                request.documentNumber
            )
        ) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Документ с таким номером уже существует"
            )
        }

        val document = countRepository.save(
            InventoryCount(
                tenantId = tenantId,
                warehouse = warehouse,
                documentNumber = request.documentNumber,
                notes = request.notes
            )
        )

        return toResponse(document)
    }


    @Transactional
    fun post(
        tenantId: Long,
        warehouseId: Long,
        countId: Long
    ): InventoryCountResponse {
        val document = countRepository
            .findForUpdate(countId, tenantId, warehouseId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Документ инвентаризации не найден"
                )
            }

        if (document.status != InventoryCountStatus.DRAFT) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Инвентаризация уже проведена или её статус недопустим"
            )
        }

        val documentId = requireNotNull(document.id)
        val lines = lineRepository.findAllByDocument_IdOrderByIdAsc(documentId)

        if (lines.isEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Нельзя провести инвентаризацию без строк"
            )
        }

        // Сначала блокируем и проверяем все остатки.
        val inventories = lines.map { line ->
            val location = locationRepository
                .findById(line.locationId)
                .orElseThrow {
                    ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Ячейка хранения не найдена"
                    )
                }

            if (
                location.tenantId != tenantId ||
                location.zone.warehouse.id != warehouseId ||
                !location.isActive
            ) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ячейка неактивна или не принадлежит указанному складу"
                )
            }

            val inventory = inventoryRepository
                .findForUpdate(
                    tenantId,
                    line.productId,
                    line.locationId
                )
                .orElseThrow {
                    ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Остаток товара ${line.productId} в ячейке ${line.locationId} отсутствует"
                    )
                }

            if (inventory.quantity.compareTo(line.systemQuantity) != 0) {
                throw ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Остаток товара ${line.productId} изменился после создания строки инвентаризации. Обновите документ и повторите попытку"
                )
            }

            Triple(line, location, inventory)
        }

        val now = OffsetDateTime.now()

        // Все проверки пройдены — применяем корректировки.
        for ((line, _, inventory) in inventories) {
            val difference = line.actualQuantity.subtract(inventory.quantity)

            if (difference.compareTo(BigDecimal.ZERO) != 0) {
                inventory.quantity = line.actualQuantity
                inventory.updatedAt = now
                inventoryRepository.save(inventory)

                inventoryMovementRepository.save(
                    InventoryMovement(
                        tenantId = tenantId,
                        product = line.product,
                        locationId = line.locationId,
                        movementType = InventoryMovementType.ADJUSTMENT,
                        quantity = difference.abs()
                    )
                )
            }
        }

        document.status = InventoryCountStatus.POSTED
        document.postedAt = now
        countRepository.save(document)

        return toResponse(document)
    }


    @Transactional
    fun addLine(
        tenantId: Long,
        warehouseId: Long,
        countId: Long,
        request: AddInventoryCountLineRequest
    ): InventoryCountLineResponse {
        val document = getDocument(tenantId, warehouseId, countId)

        if (document.status != InventoryCountStatus.DRAFT) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Нельзя изменять проведённую инвентаризацию"
            )
        }

        val product = productRepository
            .findById(request.productId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Товар не найден"
                )
            }

        if (product.tenant.id != tenantId || !product.isActive) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Товар неактивен или принадлежит другой компании"
            )
        }

        val location = locationRepository
            .findById(request.locationId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Ячейка хранения не найдена"
                )
            }

        if (
            location.tenantId != tenantId ||
            location.zone.warehouse.id != warehouseId ||
            !location.isActive
        ) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Ячейка неактивна или не принадлежит указанному складу"
            )
        }

        if (lineRepository.existsByDocument_IdAndProductIdAndLocationId(
                countId,
                request.productId,
                request.locationId
            )
        ) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Этот товар в данной ячейке уже добавлен в инвентаризацию"
            )
        }

        val systemQuantity = inventoryRepository
            .findByTenantIdAndProduct_IdAndLocation_Id(
                tenantId,
                request.productId,
                request.locationId
            )
            .map { it.quantity }
            .orElse(BigDecimal.ZERO)

        val line = lineRepository.save(
            InventoryCountLine(
                document = document,
                tenantId = tenantId,
                productId = requireNotNull(product.id),
                product = product,
                locationId = request.locationId,
                systemQuantity = systemQuantity,
                actualQuantity = request.actualQuantity
            )
        )

        return toLineResponse(line)
    }

    @Transactional(readOnly = true)
    fun getAll(
        tenantId: Long,
        warehouseId: Long
    ): List<InventoryCountResponse> {
        validateWarehouse(tenantId, warehouseId)

        return countRepository
            .findAllByTenantIdAndWarehouse_IdOrderByCreatedAtDesc(
                tenantId,
                warehouseId
            )
            .map(::toResponse)
    }

    @Transactional(readOnly = true)
    fun getById(
        tenantId: Long,
        warehouseId: Long,
        countId: Long
    ): InventoryCountResponse {
        return toResponse(getDocument(tenantId, warehouseId, countId))
    }

    private fun getDocument(
        tenantId: Long,
        warehouseId: Long,
        countId: Long
    ): InventoryCount {
        return countRepository
            .findByIdAndTenantIdAndWarehouse_Id(
                countId,
                tenantId,
                warehouseId
            )
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Документ инвентаризации не найден"
                )
            }
    }

    private fun validateWarehouse(
        tenantId: Long,
        warehouseId: Long
    ) {
        if (!warehouseRepository.existsByIdAndTenant_Id(warehouseId, tenantId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Склад не найден"
            )
        }
    }

    private fun toResponse(
        document: InventoryCount
    ): InventoryCountResponse {
        val id = requireNotNull(document.id)

        return InventoryCountResponse(
            id = id,
            tenantId = document.tenantId,
            warehouseId = document.warehouse.id!!,
            documentNumber = document.documentNumber,
            status = document.status,
            notes = document.notes,
            createdAt = document.createdAt,
            postedAt = document.postedAt,
            lines = lineRepository
                .findAllByDocument_IdOrderByIdAsc(id)
                .map(::toLineResponse)
        )
    }

    private fun toLineResponse(
        line: InventoryCountLine
    ): InventoryCountLineResponse {
        val location = locationRepository
            .findById(line.locationId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Ячейка хранения не найдена"
                )
            }

        return InventoryCountLineResponse(
            id = requireNotNull(line.id),
            productId = line.productId,
            sku = line.product.sku,
            productName = line.product.name,
            locationId = line.locationId,
            locationCode = location.code,
            systemQuantity = line.systemQuantity,
            actualQuantity = line.actualQuantity,
            difference = line.actualQuantity.subtract(line.systemQuantity)
        )
    }
}
