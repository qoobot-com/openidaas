package com.qoobot.openidaas.core.constants;

/**
 * 缓存相关常量
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public class CacheConstants {
    
    /**
     * 用户缓存前缀
     */
    public static final String USER_CACHE_PREFIX = "user:";
    
    /**
     * 租户缓存前缀
     */
    public static final String TENANT_CACHE_PREFIX = "tenant:";
    
    /**
     * 令牌缓存前缀
     */
    public static final String TOKEN_CACHE_PREFIX = "token:";
    
    /**
     * 权限缓存前缀
     */
    public static final String PERMISSION_CACHE_PREFIX = "permission:";
    
    /**
     * 用户缓存 TTL（秒）
     */
    public static final long USER_CACHE_TTL = 3600;
    
    /**
     * 租户缓存 TTL（秒）
     */
    public static final long TENANT_CACHE_TTL = 7200;
    
    /**
     * 令牌缓存 TTL（秒）
     */
    public static final long TOKEN_CACHE_TTL = 86400;
    
    private CacheConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
