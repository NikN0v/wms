package com.wms.repository.transfer

import com.wms.entity.transfers.StockTransferLine
import org.springframework.data.jpa.repository.JpaRepository

interface StockTransferLineRepository : JpaRepository<StockTransferLine, Long> {

    fun findAllByDocument_IdOrderByIdAsc(
        documentId: Long
    ): List<StockTransferLine>

    fun existsByDocument_Id(documentId: Long): Boolean
}