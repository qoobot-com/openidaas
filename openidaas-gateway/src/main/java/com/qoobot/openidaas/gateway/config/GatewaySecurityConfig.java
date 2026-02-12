package com.qoobot.openidaas.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * 网关安全配置
 *
 * @author QooBot
 */
@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    /**
     * 安全过滤器链配置
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                // 禁用CSRF（因为是API网关）
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                
                // 禁用表单登录
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                
                // 禁用HTTP Basic认证
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                
                // 配置授权规则
                .authorizeExchange(exchanges -> exchanges
                        // 公开的健康检查端点
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        
                        // 公开的认证相关接口
                        .pathMatchers("/api/auth/login", "/api/auth/register", "/api/auth/token").permitAll()
                        
                        // 管理界面静态资源
                        .pathMatchers("/admin/**").permitAll()
                        
                        // 其他所有请求都需要认证
                        .anyExchange().authenticated()
                )
                
                // 构建安全配置
                .build();
    }
}