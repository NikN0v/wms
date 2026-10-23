package com.wms.repository.zone

import com.wms.entity.zone.StorageZone
import org.springframework.data.jpa.repository.JpaRepository

interface StorageZoneRepository : JpaRepository<StorageZone, Long> {

    fun existsByWarehouseIdAndCode(warehouseId: Long, code: String): Boolean

    fun findAllByWarehouseId(warehouseId: Long): List<StorageZone>
}