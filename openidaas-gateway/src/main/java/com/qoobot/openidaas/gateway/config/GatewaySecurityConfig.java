package com.qoobot.openidaas.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * 网关安全配置
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    /**
     * 安全过滤器链
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                // 公开端点
                .pathMatchers(
                    "/actuator/health",
                    "/actuator/info",
                    "/api/public/**",
                    "/auth/**"
                ).permitAll()
                
                // 其他请求需要认证
                .anyExchange().authenticated()
            );
        
        return http.build();
    }
}
