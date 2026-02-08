package com.qoobot.openidaas.core.constants;

import java.util.Arrays;
import java.util.List;

/**
 * 安全相关常量
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public class SecurityConstants {
    
    /**
     * JWT 令牌前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";
    
    /**
     * JWT 令牌头
     */
    public static final String TOKEN_HEADER = "Authorization";
    
    /**
     * JWT 令牌有效期（小时）
     */
    public static final long TOKEN_EXPIRATION_HOURS = 24;
    
    /**
     * 刷新令牌有效期（天）
     */
    public static final long REFRESH_TOKEN_EXPIRATION_DAYS = 30;
    
    /**
     * JWT 密钥
     */
    public static final String JWT_SECRET_KEY = "openidaas-jwt-secret-key-2024-qoobot";
    
    /**
     * 公开端点列表
     */
    public static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/refresh",
        "/api/auth/forgot-password",
        "/api/auth/reset-password",
        "/error",
        "/actuator/**",
        "/swagger-ui/**",
        "/v3/api-docs/**"
    );
    
    /**
     * 管理员角色
     */
    public static final String ROLE_ADMIN = "ADMIN";
    
    /**
     * 用户角色
     */
    public static final String ROLE_USER = "USER";
    
    /**
     * 超级管理员角色
     */
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    
    /**
     * 密码最小长度
     */
    public static final int PASSWORD_MIN_LENGTH = 8;
    
    /**
     * 密码最大长度
     */
    public static final int PASSWORD_MAX_LENGTH = 128;
    
    private SecurityConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
