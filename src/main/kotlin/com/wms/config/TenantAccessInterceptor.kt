package com.wms.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import com.wms.security.wmsUserContext
import com.wms.service.warehouse.WarehouseAccessService
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Component
class TenantAccessInterceptor(
    private val warehouseAccessService: WarehouseAccessService
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val pathVariables = request.getAttribute(
            HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE
        ) as? Map<*, *>

        val tenantIdValue = pathVariables?.get("tenantId") ?: return true
        val tenantId = tenantIdValue
            .toString()
            .toLongOrNull()
            ?: throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "В запросе отсутствует корректный ID компании"
            )

        val authentication = SecurityContextHolder.getContext().authentication
            as? JwtAuthenticationToken
            ?: throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Не удалось определить компанию пользователя"
            )

        val identity = authentication.wmsUserContext()

        if (tenantId != identity.tenantId) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Нет доступа к данным этой компании"
            )
        }

        warehouseAccessService.validateActor(tenantId, identity.userId, identity.role)

        val warehouseIdValue = pathVariables["warehouseId"]
        if (warehouseIdValue != null) {
            val warehouseId = warehouseIdValue.toString().toLongOrNull()
                ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Некорректный ID склада")
            warehouseAccessService.requireAccess(
                tenantId,
                identity.userId,
                identity.role,
                warehouseId
            )
        }

        return true
    }
}

@Configuration
class TenantAccessWebConfiguration(
    private val tenantAccessInterceptor: TenantAccessInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(
        registry: InterceptorRegistry
    ) {
        registry.addInterceptor(tenantAccessInterceptor)
            .addPathPatterns("/api/tenants/{tenantId}/**")
    }
}
