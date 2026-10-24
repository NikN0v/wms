package com.wms.repository.user

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class UserWarehouseAssignmentRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    fun findWarehouseIds(tenantId: Long, userId: Long): List<Long> =
        jdbcTemplate.queryForList(
            "SELECT warehouse_id FROM user_warehouse_assignments WHERE tenant_id = ? AND user_id = ? ORDER BY warehouse_id",
            Long::class.javaObjectType,
            tenantId,
            userId
        ).filterNotNull()

    fun exists(tenantId: Long, userId: Long, warehouseId: Long): Boolean =
        jdbcTemplate.queryForObject(
            "SELECT EXISTS (SELECT 1 FROM user_warehouse_assignments WHERE tenant_id = ? AND user_id = ? AND warehouse_id = ?)",
            Boolean::class.java,
            tenantId,
            userId,
            warehouseId
        ) ?: false

    fun deleteAll(tenantId: Long, userId: Long) {
        jdbcTemplate.update(
            "DELETE FROM user_warehouse_assignments WHERE tenant_id = ? AND user_id = ?",
            tenantId,
            userId
        )
    }

    fun insert(tenantId: Long, userId: Long, warehouseId: Long) {
        jdbcTemplate.update(
            "INSERT INTO user_warehouse_assignments (tenant_id, user_id, warehouse_id) VALUES (?, ?, ?)",
            tenantId,
            userId,
            warehouseId
        )
    }
}
