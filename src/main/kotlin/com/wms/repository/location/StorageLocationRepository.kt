package com.wms.repository.location

import com.wms.entity.location.StorageLocation
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface StorageLocationRepository : JpaRepository<StorageLocation, Long> {

    fun existsByZoneIdAndCode(zoneId: Long, code: String): Boolean

    fun findAllByZoneId(zoneId: Long): List<StorageLocation>

    fun findByIdAndTenantId(
        id: Long,
        tenantId: Long
    ): Optional<StorageLocation>
}