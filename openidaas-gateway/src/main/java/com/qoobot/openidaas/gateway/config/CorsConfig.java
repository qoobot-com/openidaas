package com.qoobot.openidaas.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Arrays;
import java.util.Collections;

/**
 * 跨域配置类
 *
 * @author QooBot
 */
@Configuration
public class CorsConfig {

    /**
     * 跨域过滤器配置
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的源站
        config.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "https://*.yourdomain.com",
            "http://*.yourdomain.com"
        ));
        
        // 允许的请求方法
        config.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
        ));
        
        // 允许的请求头
        config.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "X-Service-Name",
            "X-User-ID",
            "X-Username",
            "X-Tenant-ID"
        ));
        
        // 暴露的响应头
        config.setExposedHeaders(Arrays.asList(
            "Authorization",
            "X-Total-Count",
            "X-Pagination-Page",
            "X-Pagination-Limit"
        ));
        
        // 是否允许携带凭证
        config.setAllowCredentials(true);
        
        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", config);
        
        return new CorsWebFilter(source);
    }
}