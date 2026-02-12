package com.qoobot.openidaas.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关路由配置
 *
 * @author QooBot
 */
@Configuration
public class GatewayRouteConfig {

    @Value("${gateway.timeout.connect:5000}")
    private int connectTimeout;
    
    @Value("${gateway.timeout.response:10000}")
    private int responseTimeout;

    /**
     * 自定义路由配置
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 认证服务路由
                .route("auth-service", r -> r.path("/api/auth/**")
                        .filters(f -> f.stripPrefix(2)
                                .addRequestHeader("X-Service-Name", "auth-service")
                                .addRequestHeader("X-Gateway-Version", "1.0")
                                .retry(3))
                        .uri("lb://openidaas-auth-service"))
                
                // 用户服务路由
                .route("user-service", r -> r.path("/api/users/**")
                        .filters(f -> f.stripPrefix(2)
                                .addRequestHeader("X-Service-Name", "user-service")
                                .addRequestHeader("X-Gateway-Version", "1.0")
                                .retry(3))
                        .uri("lb://openidaas-user-service"))
                
                // 角色服务路由
                .route("role-service", r -> r.path("/api/roles/**")
                        .filters(f -> f.stripPrefix(2)
                                .addRequestHeader("X-Service-Name", "role-service")
                                .addRequestHeader("X-Gateway-Version", "1.0")
                                .retry(3))
                        .uri("lb://openidaas-role-service"))
                
                // 组织服务路由
                .route("organization-service", r -> r.path("/api/organizations/**")
                        .filters(f -> f.stripPrefix(2)
                                .addRequestHeader("X-Service-Name", "organization-service")
                                .addRequestHeader("X-Gateway-Version", "1.0")
                                .retry(3))
                        .uri("lb://openidaas-organization-service"))
                
                // 应用服务路由
                .route("application-service", r -> r.path("/api/applications/**")
                        .filters(f -> f.stripPrefix(2)
                                .addRequestHeader("X-Service-Name", "application-service")
                                .addRequestHeader("X-Gateway-Version", "1.0")
                                .retry(3))
                        .uri("lb://openidaas-application-service"))
                
                // 授权服务路由
                .route("authorization-service", r -> r.path("/api/authorization/**")
                        .filters(f -> f.stripPrefix(2)
                                .addRequestHeader("X-Service-Name", "authorization-service")
                                .addRequestHeader("X-Gateway-Version", "1.0")
                                .retry(3))
                        .uri("lb://openidaas-authorization-service"))
                
                // 审计服务路由
                .route("audit-service", r -> r.path("/api/audit/**")
                        .filters(f -> f.stripPrefix(2)
                                .addRequestHeader("X-Service-Name", "audit-service")
                                .addRequestHeader("X-Gateway-Version", "1.0")
                                .retry(3))
                        .uri("lb://openidaas-audit-service"))
                
                // 文件服务路由
                .route("file-service", r -> r.path("/api/files/**")
                        .filters(f -> f.stripPrefix(2)
                                .addRequestHeader("X-Service-Name", "file-service")
                                .addRequestHeader("X-Gateway-Version", "1.0")
                                .retry(3))
                        .uri("lb://openidaas-file-service"))
                
                // 通知服务路由
                .route("notification-service", r -> r.path("/api/notifications/**")
                        .filters(f -> f.stripPrefix(2)
                                .addRequestHeader("X-Service-Name", "notification-service")
                                .addRequestHeader("X-Gateway-Version", "1.0")
                                .retry(3))
                        .uri("lb://openidaas-notification-service"))
                
                // 管理界面静态资源路由
                .route("admin-ui-static", r -> r.path("/admin/static/**")
                        .filters(f -> f.stripPrefix(1)
                                .retry(2))
                        .uri("lb://openidaas-admin-ui"))
                
                // 管理界面首页路由
                .route("admin-ui-index", r -> r.path("/admin/**")
                        .filters(f -> f.stripPrefix(1)
                                .retry(2))
                        .uri("lb://openidaas-admin-ui"))
                
                // WebSocket路由
                .route("websocket-service", r -> r.path("/ws/**")
                        .filters(f -> f.stripPrefix(1)
                                .addRequestHeader("X-Service-Name", "websocket-service"))
                        .uri("lb:ws://openidaas-websocket-service"))
                
                // 健康检查路由
                .route("health-check", r -> r.path("/health/**")
                        .filters(f -> f.stripPrefix(1)
                                .addRequestHeader("X-Service-Name", "health-check"))
                        .uri("lb://openidaas-health-service"))
                
                .build();
    }
}