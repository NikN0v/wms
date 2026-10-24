package authorizationprobe

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthorizationProbeController {

    @GetMapping("/api/tenants/{tenantId}/warehouses")
    fun getWarehouses(@PathVariable tenantId: Long) = "tenant-$tenantId"

    @GetMapping("/api/tenants/{tenantId}/products")
    fun getProducts(@PathVariable tenantId: Long) = "products-$tenantId"

    @PostMapping("/api/tenants/{tenantId}/warehouses/{warehouseId}/shipments/{shipmentId}/post")
    fun postShipment(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long,
        @PathVariable shipmentId: Long
    ) = "posted-$tenantId-$warehouseId-$shipmentId"

    @PostMapping("/api/tenants/{tenantId}/products")
    fun createProduct(@PathVariable tenantId: Long) = "created-product-$tenantId"

    @GetMapping("/api/tenants/{tenantId}/audit-logs")
    fun getAudit(@PathVariable tenantId: Long) = "audit-$tenantId"

    @GetMapping("/api/tenants/{tenantId}/warehouses/{warehouseId}/reports/stock-balances")
    fun getReport(
        @PathVariable tenantId: Long,
        @PathVariable warehouseId: Long
    ) = "report-$tenantId-$warehouseId"

    @PostMapping("/api/tenants/{tenantId}/users")
    fun createUser(@PathVariable tenantId: Long) = "created-user-$tenantId"

    @GetMapping("/api/tenants/{tenantId}/users")
    fun listUsers(@PathVariable tenantId: Long) = "users-$tenantId"

    @PatchMapping("/api/tenants/{tenantId}/users/{userId}/active")
    fun setUserActive(@PathVariable tenantId: Long, @PathVariable userId: Long) = "active-$userId"

    @PatchMapping("/api/tenants/{tenantId}/users/{userId}/role")
    fun setUserRole(@PathVariable tenantId: Long, @PathVariable userId: Long) = "role-$userId"

    @PostMapping("/api/tenants")
    fun createTenant() = "created-tenant"
}
