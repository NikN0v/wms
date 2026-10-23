package com.wms.controller.reports

import com.wms.dto.reports.InventoryMovementReportResponse
import com.wms.entity.movement.InventoryMovementType
import com.wms.service.reports.InventoryMovementReportService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime

@RestController
@RequestMapping(
    "/api/tenants/{tenantId}/warehouses/{warehouseId}/reports"
)
class InventoryMovementReportController(
    private val inventoryMovementReportService: InventoryMovementReportService
) {

    @GetMapping("/inventory-movements")
    fun getReport(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long,
        @RequestParam(required = false) productId: Long?,
        @RequestParam(required = false) movementType: InventoryMovementType?,
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        from: OffsetDateTime,
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        to: OffsetDateTime
    ): List<InventoryMovementReportResponse> {
        return inventoryMovementReportService.getReport(
            tenantId = tenantId,
            warehouseId = warehouseId,
            productId = productId,
            movementType = movementType,
            from = from,
            to = to
        )
    }
}