package com.wms.repository.receiving

import com.wms.entity.receiving.ReceivingDocument
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ReceivingDocumentRepository : JpaRepository<ReceivingDocument, Long> {

    fun findAllByTenantIdOrderByCreatedAtDesc(tenantId: Long): List<ReceivingDocument>

    fun existsByTenantIdAndDocumentNumber(tenantId: Long, documentNumber: String): Boolean

    fun findByIdAndTenantIdAndWarehouse_Id(
        id: Long,
        tenantId: Long,
        warehouseId: Long
    ): Optional<ReceivingDocument>
}