package com.wms.controller.product

import com.wms.dto.product.CreateProductRequest
import com.wms.dto.product.ProductResponse
import com.wms.service.product.ProductService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tenants/{tenantId}/products")
class ProductController(
    private val productService: ProductService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable tenantId: Long,
        @Valid @RequestBody request: CreateProductRequest
    ): ProductResponse {
        if (tenantId != request.tenantId) {
            throw org.springframework.web.server.ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "ID компании в URL и теле запроса не совпадают"
            )
        }

        return productService.create(request)
    }

    @GetMapping
    fun getAll(
        @PathVariable tenantId: Long
    ): List<ProductResponse> {
        return productService.getAllByTenantId(tenantId)
    }
}