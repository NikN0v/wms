package com.wms.controller.shipment

import com.wms.dto.shipment.AddShipmentLineRequest
import com.wms.dto.shipment.CreateShipmentRequest
import com.wms.dto.shipment.ShipmentResponse
import com.wms.service.shipment.ShipmentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    "/api/tenants/{tenantId}/warehouses/{warehouseId}/shipments"
)
class ShipmentController(
    private val shipmentService: ShipmentService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createDocument(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long,
        @Valid @RequestBody request: CreateShipmentRequest
    ): ShipmentResponse =
        shipmentService.createDocument(tenantId, warehouseId, request)

    @PostMapping("/{documentId}/lines")
    fun addLine(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long,
        @PathVariable documentId: Long,
        @Valid @RequestBody request: AddShipmentLineRequest
    ): ShipmentResponse =
        shipmentService.addLine(
            tenantId, warehouseId, documentId, request
        )

    @GetMapping
    fun getAll(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long
    ): List<ShipmentResponse> =
        shipmentService.getAll(tenantId, warehouseId)

    @GetMapping("/{documentId}")
    fun getById(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long,
        @PathVariable documentId: Long
    ): ShipmentResponse =
        shipmentService.getById(tenantId, warehouseId, documentId)

    @PostMapping("/{documentId}/post")
    fun post(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long,
        @PathVariable documentId: Long
    ): ShipmentResponse =
        shipmentService.post(tenantId, warehouseId, documentId)
}