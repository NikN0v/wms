
package com.wms.controller.returns

import com.wms.dto.returns.AddStockReturnLineRequest
import com.wms.dto.returns.CreateStockReturnRequest
import com.wms.dto.returns.StockReturnResponse
import com.wms.service.returns.StockReturnService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    "/api/tenants/{tenantId}/warehouses/{warehouseId}/returns"
)
class StockReturnController(
    private val stockReturnService: StockReturnService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createDocument(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long,
        @Valid @RequestBody request: CreateStockReturnRequest
    ): StockReturnResponse {
        return stockReturnService.createDocument(
            tenantId,
            warehouseId,
            request
        )
    }

    @PostMapping("/{returnId}/lines")
    @ResponseStatus(HttpStatus.CREATED)
    fun addLine(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long,
        @PathVariable returnId: Long,
        @Valid @RequestBody request: AddStockReturnLineRequest
    ): StockReturnResponse {
        return stockReturnService.addLine(
            tenantId,
            warehouseId,
            returnId,
            request
        )
    }

    @GetMapping
    fun getAll(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long
    ): List<StockReturnResponse> {
        return stockReturnService.getAll(
            tenantId,
            warehouseId
        )
    }

    @GetMapping("/{returnId}")
    fun getById(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long,
        @PathVariable returnId: Long
    ): StockReturnResponse {
        return stockReturnService.getById(
            tenantId,
            warehouseId,
            returnId
        )
    }

    @PostMapping("/{returnId}/post")
    fun post(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long,
        @PathVariable returnId: Long
    ): StockReturnResponse {
        return stockReturnService.post(
            tenantId,
            warehouseId,
            returnId
        )
    }
}
