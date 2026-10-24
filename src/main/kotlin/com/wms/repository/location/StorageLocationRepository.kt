package com.wms.repository.location

import com.wms.entity.location.StorageLocation
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface StorageLocationRepository : JpaRepository<StorageLocation, Long> {

    fun existsByZoneIdAndCode(zoneId: Long, code: String): Boolean

    fun findAllByZoneId(zoneId: Long): List<StorageLocation>

    fun findByIdAndTenantId(
        id: Long,
        tenantId: Long
    ): Optional<StorageLocation>

    @Query("select z.warehouse.id from StorageLocation l join l.zone z where l.id = :locationId and l.tenantId = :tenantId")
    fun findWarehouseIdByIdAndTenantId(
        @Param("locationId") locationId: Long,
        @Param("tenantId") tenantId: Long
    ): Long?
}
