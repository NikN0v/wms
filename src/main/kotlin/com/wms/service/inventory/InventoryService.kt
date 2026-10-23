package com.wms.service.inventory

import com.wms.dto.inventory.CreateInventoryRequest
import com.wms.dto.inventory.InventoryResponse
import com.wms.entity.inventory.Inventory
import com.wms.repository.inventory.InventoryRepository
import com.wms.repository.location.StorageLocationRepository
import com.wms.repository.product.ProductRepository
import com.wms.repository.tenant.TenantRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class InventoryService(
    private val inventoryRepository: InventoryRepository,
    private val productRepository: ProductRepository,
    private val locationRepository: StorageLocationRepository,
    private val tenantRepository: TenantRepository
) {

    @Transactional
    fun create(
        tenantId: Long,
        request: CreateInventoryRequest
    ): InventoryResponse {
        if (!tenantRepository.existsById(tenantId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Компания с ID $tenantId не найдена"
            )
        }

        val product = productRepository.findById(request.productId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Товар с ID: ${request.productId} не найден"
                )
            }

        val location = locationRepository.findById(request.locationId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Ячейка с ID: ${request.locationId} не найдена"
                )
            }

        if (product.tenant.id != tenantId || location.tenantId != tenantId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Товар и ячейка должны принадлежать указанной компании"
            )
        }

        if (!product.isActive || !location.isActive) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Нельзя добавить остаток для неактивного товара или ячейки"
            )
        }

        if (inventoryRepository.existsByTenantIdAndProduct_IdAndLocation_Id(
                tenantId,
                request.productId,
                request.locationId
            )
        ) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Остаток для этого товара в данной ячейке уже существует"
            )
        }

        val inventory = Inventory(
            tenantId = tenantId,
            product = product,
            location = location,
            quantity = request.quantity
        )

        return inventoryRepository.save(inventory).toResponse()
    }

    @Transactional(readOnly = true)
    fun getAllByTenantId(tenantId: Long): List<InventoryResponse> {
        if (!tenantRepository.existsById(tenantId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Компания с ID: $tenantId не найдена"
            )
        }

        return inventoryRepository.findAllByTenantId(tenantId)
            .map { it.toResponse() }
    }

    private fun Inventory.toResponse() = InventoryResponse(
        id = requireNotNull(id),
        tenantId = tenantId,
        productId = requireNotNull(product.id),
        sku = product.sku,
        productName = product.name,
        locationId = requireNotNull(location.id),
        locationCode = location.code,
        quantity = quantity,
        reservedQuantity = reservedQuantity,
        availableQuantity = availableQuantity,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}