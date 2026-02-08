package com.qoobot.openidaas.gateway.config;

import com.qoobot.openidaas.gateway.filter.AuthenticationGatewayFilter;
import com.qoobot.openidaas.gateway.filter.RateLimitGatewayFilter;
import com.qoobot.openidaas.gateway.filter.ApiLoggingFilter;
import com.qoobot.openidaas.gateway.property.GatewayProperties;
import com.qoobot.openidaas.gateway.service.JwtTokenService;
import com.qoobot.openidaas.gateway.service.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import java.time.Duration;

/**
 * 网关路由配置
 * 
 * 提供动态路由配置，支持负载均衡、限流、熔断等功能
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class GatewayRoutesConfiguration {

    private final GatewayProperties gatewayProperties;
    private final JwtTokenService jwtTokenService;
    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;
    private final ApiLoggingFilter apiLoggingFilter;

    /**
     * 路由定位器
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        // 创建带参数的过滤器实例
        AuthenticationGatewayFilter authFilter = new AuthenticationGatewayFilter(jwtTokenService, objectMapper);
        RateLimitGatewayFilter rateLimitFilter = new RateLimitGatewayFilter(rateLimitService, objectMapper);
        
        return builder.routes()
                // 用户服务路由
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .and()
                        .method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                        .filters(f -> f
                                .filter(authFilter.apply(null))
                                .filter(rateLimitFilter.apply(null))
                                .filter(apiLoggingFilter.apply(null))
                                .stripPrefix(1)
                                .circuitBreaker(c -> c
                                        .setName("user-service-cb")
                                        .setFallbackUri("forward:/fallback/user")))
                        .uri(gatewayProperties.getUserServiceUri()))

                // 认证服务路由
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .filter(rateLimitFilter.apply(null))
                                .filter(apiLoggingFilter.apply(null))
                                .stripPrefix(1)
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setBackoff(Duration.ofMillis(100), Duration.ofSeconds(2), 2, false)))
                        .uri(gatewayProperties.getAuthServiceUri()))

                // 租户服务路由
                .route("tenant-service", r -> r
                        .path("/api/tenants/**")
                        .and()
                        .method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                        .filters(f -> f
                                .filter(authFilter.apply(null))
                                .filter(rateLimitFilter.apply(null))
                                .filter(apiLoggingFilter.apply(null))
                                .stripPrefix(1)
                                .circuitBreaker(c -> c
                                        .setName("tenant-service-cb")
                                        .setFallbackUri("forward:/fallback/tenant")))
                        .uri(gatewayProperties.getTenantServiceUri()))

                // 安全服务路由
                .route("security-service", r -> r
                        .path("/api/security/**")
                        .and()
                        .method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                        .filters(f -> f
                                .filter(authFilter.apply(null))
                                .filter(rateLimitFilter.apply(null))
                                .filter(apiLoggingFilter.apply(null))
                                .stripPrefix(1)
                                .circuitBreaker(c -> c
                                        .setName("security-service-cb")
                                        .setFallbackUri("forward:/fallback/security")))
                        .uri(gatewayProperties.getSecurityServiceUri()))

                // 内部服务路由（无需认证）
                .route("internal-service", r -> r
                        .path("/internal/**")
                        .filters(f -> f
                                .filter(rateLimitFilter.apply(null))
                                .filter(apiLoggingFilter.apply(null))
                                .stripPrefix(1))
                        .uri(gatewayProperties.getInternalServiceUri()))

                .build();
    }
}
