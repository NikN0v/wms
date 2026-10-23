package com.wms.repository.returns

import com.wms.entity.returns.StockReturnLine
import org.springframework.data.jpa.repository.JpaRepository

interface StockReturnLineRepository :
    JpaRepository<StockReturnLine, Long> {

    fun findAllByDocument_IdOrderByIdAsc(
        documentId: Long
    ): List<StockReturnLine>

    fun existsByDocument_Id(documentId: Long): Boolean
}