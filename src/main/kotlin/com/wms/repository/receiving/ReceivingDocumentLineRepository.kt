package com.wms.repository.receiving

import com.wms.entity.receiving.ReceivingDocumentLine
import org.springframework.data.jpa.repository.JpaRepository

interface ReceivingDocumentLineRepository : JpaRepository<ReceivingDocumentLine, Long> {

    fun findAllByDocument_IdOrderByIdAsc(documentId: Long): List<ReceivingDocumentLine>

    fun existsByDocument_Id(documentId: Long): Boolean
}