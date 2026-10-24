package com.wms.dto.user

import jakarta.validation.constraints.Positive

data class SetUserWarehousesRequest(
    val warehouseIds: List<@Positive Long>
)
