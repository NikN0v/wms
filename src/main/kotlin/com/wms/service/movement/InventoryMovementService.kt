package com.wms.service.movement

import com.wms.dto.movement.InventoryMovementResponse
import com.wms.entity.movement.InventoryMovement
import com.wms.repository.location.StorageLocationRepository
import com.wms.repository.movement.InventoryMovementRepository
import com.wms.repository.product.ProductRepository
import com.wms.repository.tenant.TenantRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class InventoryMovementService(
    private val movementRepository: InventoryMovementRepository,
    private val tenantRepository: TenantRepository,
    private val productRepository: ProductRepository,
    private val locationRepository: StorageLocationRepository
) {

    @Transactional(readOnly = true)
    fun getAll(tenantId: Long, warehouseIds: Set<Long>? = null): List<InventoryMovementResponse> {
        validateTenant(tenantId)

        if (warehouseIds != null && warehouseIds.isEmpty()) return emptyList()
        val result = movementRepository
            .findAllByTenantIdOrderByCreatedAtDesc(tenantId)
            .map { it.toResponse() }
        return if (warehouseIds == null) result else result.filter { it.warehouseId in warehouseIds }
    }

    @Transactional(readOnly = true)
    fun getByProduct(
        tenantId: Long,
        productId: Long,
        warehouseIds: Set<Long>? = null
    ): List<InventoryMovementResponse> {
        validateTenant(tenantId)

        val product = productRepository.findById(productId)
            .orElseThrow { notFound("Товар с ID: $productId не найден") }

        if (product.tenant.id != tenantId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Товар не принадлежит указанной компании"
            )
        }

        if (warehouseIds != null && warehouseIds.isEmpty()) return emptyList()
        val result = movementRepository
            .findAllByTenantIdAndProduct_IdOrderByCreatedAtDesc(tenantId, productId)
            .map { it.toResponse() }
        return if (warehouseIds == null) result else result.filter { it.warehouseId in warehouseIds }
    }

    @Transactional(readOnly = true)
    fun getByLocation(
        tenantId: Long,
        locationId: Long,
        warehouseIds: Set<Long>? = null
    ): List<InventoryMovementResponse> {
        validateTenant(tenantId)

        val location = locationRepository.findById(locationId)
            .orElseThrow { notFound("Ячейка с ID: $locationId не найдена") }

        if (location.tenantId != tenantId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Ячейка не принадлежит указанной компании"
            )
        }

        if (warehouseIds != null && location.zone.warehouse.id !in warehouseIds) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Нет доступа к этому складу")
        }

        return movementRepository
            .findAllByTenantIdAndLocationIdOrderByCreatedAtDesc(tenantId, locationId)
            .map { it.toResponse() }
    }

    private fun validateTenant(tenantId: Long) {
        if (!tenantRepository.existsById(tenantId)) {
            throw notFound("Компания с ID: $tenantId не найдена")
        }
    }

    private fun notFound(message: String) =
        ResponseStatusException(HttpStatus.NOT_FOUND, message)

    private fun InventoryMovement.toResponse(): InventoryMovementResponse {
        val location = locationRepository.findById(locationId)
            .orElseThrow { notFound("Ячейка с ID: $locationId не найдена") }

        return InventoryMovementResponse(
            id = requireNotNull(id),
            tenantId = tenantId,
            productId = requireNotNull(product.id),
            sku = product.sku,
            productName =  product.name,
            locationId = locationId,
            locationCode = location.code,
            warehouseId = requireNotNull(location.zone.warehouse.id),
            movementType = movementType,
            quantity = quantity,
            receivingDocumentId = receivingDocumentId,
            receivingLineId = receivingLineId,
            createdAt = createdAt
        )
    }
}
