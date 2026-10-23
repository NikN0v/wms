package com.wms.repository.inventorycount

import com.wms.entity.inventorycount.InventoryCountLine
import org.springframework.data.jpa.repository.JpaRepository

interface InventoryCountLineRepository :
    JpaRepository<InventoryCountLine, Long> {

    fun findAllByDocument_IdOrderByIdAsc(
        documentId: Long
    ): List<InventoryCountLine>

    fun existsByDocument_Id(documentId: Long): Boolean

    fun existsByDocument_IdAndProductIdAndLocationId(
        documentId: Long,
        productId: Long,
        locationId: Long
    ): Boolean
}