package com.wms.service.product

import com.wms.dto.product.CreateProductRequest
import com.wms.dto.product.ProductResponse
import com.wms.entity.product.Product
import com.wms.repository.product.ProductRepository
import com.wms.repository.tenant.TenantRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val tenantRepository: TenantRepository
) {

    @Transactional
    fun create(request: CreateProductRequest): ProductResponse {
        val tenantId = request.tenantId

        val tenant = tenantRepository.findById(tenantId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Компания с ID: $tenantId не найдена"
                )
            }

        val normalizedSku = request.sku.trim().uppercase()
        val normalizedUnit = request.unit.trim().uppercase()

        val allowedUnits = setOf("PCS", "KG", "G", "L", "ML", "BOX")
        if (normalizedUnit !in allowedUnits) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Недопустимая единица измерения"
            )
        }

        if (productRepository.existsByTenantIdAndSku(tenantId, normalizedSku)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Товар с SKU: $normalizedSku уже существует у этой компании"
            )
        }

        val product = Product(
            tenant = tenant,
            sku = normalizedSku,
            name = request.name.trim(),
            description = request.description?.trim()?.takeIf { it.isNotEmpty() },
            unit = normalizedUnit
        )

        return productRepository.save(product).toResponse()
    }

    @Transactional(readOnly = true)
    fun getAllByTenantId(tenantId: Long): List<ProductResponse> {
        if (!tenantRepository.existsById(tenantId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Компания с ID: $tenantId не найдена"
            )
        }

        return productRepository.findAllByTenantId(tenantId)
            .map { it.toResponse() }
    }

    private fun Product.toResponse() = ProductResponse(
        id = requireNotNull(id),
        tenantId = requireNotNull(tenant.id),
        sku = sku,
        name = name,
        description = description,
        unit = unit,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}