package com.wms.controller.reports

import com.wms.dto.reports.StockBalanceResponse
import com.wms.service.reports.StockBalanceReportService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    "/api/tenants/{tenantId}/warehouses/{warehouseId}/reports"
)
class StockBalanceReportController(
    private val stockBalanceReportService: StockBalanceReportService
) {

    @GetMapping("/stock-balances")
    fun getStockBalances(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long,
        @RequestParam(required = false) productId: Long?,
        @RequestParam(required = false) locationId: Long?
    ): List<StockBalanceResponse> {
        return stockBalanceReportService.getStockBalances(
            tenantId = tenantId,
            warehouseId = warehouseId,
            productId = productId,
            locationId = locationId
        )
    }
}