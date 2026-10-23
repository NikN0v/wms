package com.wms.controller.receiving

import com.wms.dto.receiving.AddReceivingLineRequest
import com.wms.dto.receiving.CreateReceivingDocumentRequest
import com.wms.dto.receiving.ReceivingDocumentResponse
import com.wms.service.receiving.ReceivingService
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
@RequestMapping(
    "/api/tenants/{tenantId}/warehouses/{warehouseId}/receiving"
)
class ReceivingController(
    private val receivingService: ReceivingService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createDocument(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long,
        @Valid @RequestBody request: CreateReceivingDocumentRequest
    ): ReceivingDocumentResponse =
        receivingService.createDocument(tenantId, warehouseId, request)

    @PostMapping("/{documentId}/lines")
    fun addLine(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long,
        @PathVariable documentId: Long,
        @Valid @RequestBody request: AddReceivingLineRequest
    ): ReceivingDocumentResponse =
        receivingService.addLine(tenantId, warehouseId, documentId, request)

    @GetMapping
    fun getAll(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long
    ): List<ReceivingDocumentResponse> =
        receivingService.getAll(tenantId, warehouseId)

    @GetMapping("/{documentId}")
    fun getById(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long,
        @PathVariable documentId: Long
    ): ReceivingDocumentResponse =
        receivingService.getById(tenantId, warehouseId, documentId)

    @PostMapping("/{documentId}/post")
    fun post(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long,
        @PathVariable documentId: Long
    ): ReceivingDocumentResponse =
        receivingService.post(tenantId, warehouseId, documentId)
}