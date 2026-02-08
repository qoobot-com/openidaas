package com.qoobot.openidaas.security.constants;

import java.util.concurrent.TimeUnit;

/**
 * 安全常量
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public final class SecurityConstants {

    private SecurityConstants() {}

    // ==================== JWT 配置 ====================
    
    /**
     * JWT Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * JWT Token请求头
     */
    public static final String HEADER_STRING = "Authorization";

    /**
     * JWT Token有效期（小时）
     */
    public static final long TOKEN_EXPIRATION_HOURS = 24;

    /**
     * JWT Refresh Token有效期（天）
     */
    public static final long REFRESH_TOKEN_EXPIRATION_DAYS = 7;

    /**
     * JWT签名密钥
     */
    public static final String SIGNING_KEY = "openidaas-secret-key-change-in-production";

    /**
     * JWT Issuer
     */
    public static final String ISSUER = "openidaas";

    // ==================== 限流配置 ====================

    /**
     * 默认限流令牌桶容量
     */
    public static final int DEFAULT_BUCKET_CAPACITY = 100;

    /**
     * 默认限流速率（令牌/秒）
     */
    public static final int DEFAULT_REFILL_RATE = 10;

    /**
     * 登录限流令牌桶容量
     */
    public static final int LOGIN_BUCKET_CAPACITY = 5;

    /**
     * 登录限流速率（令牌/分钟）
     */
    public static final int LOGIN_REFILL_RATE = 1;

    /**
     * API限流令牌桶容量
     */
    public static final int API_BUCKET_CAPACITY = 100;

    /**
     * API限流速率（令牌/秒）
     */
    public static final int API_REFILL_RATE = 50;

    // ==================== 密码策略配置 ====================

    /**
     * 密码最小长度
     */
    public static final int PASSWORD_MIN_LENGTH = 8;

    /**
     * 密码最大长度
     */
    public static final int PASSWORD_MAX_LENGTH = 128;

    /**
     * 密码过期天数
     */
    public static final int PASSWORD_EXPIRE_DAYS = 90;

    /**
     * 密码历史记录数量
     */
    public static final int PASSWORD_HISTORY_COUNT = 5;

    /**
     * 最大登录失败次数
     */
    public static final int MAX_FAILED_ATTEMPTS = 5;

    /**
     * 账户锁定时间（分钟）
     */
    public static final long ACCOUNT_LOCK_MINUTES = 30;

    // ==================== MFA 配置 ====================

    /**
     * MFA验证码长度
     */
    public static final int MFA_CODE_LENGTH = 6;

    /**
     * MFA验证码有效期（秒）
     */
    public static final int MFA_CODE_EXPIRE_SECONDS = 30;

    /**
     * MFA Secret长度（字节）
     */
    public static final int MFA_SECRET_SIZE = 20;

    /**
     * MFA Issuer
     */
    public static final String MFA_ISSUER = "OpenIDaaS";

    // ==================== 安全头配置 ====================

    /**
     * X-Content-Type-Options
     */
    public static final String X_CONTENT_TYPE_OPTIONS = "nosniff";

    /**
     * X-Frame-Options
     */
    public static final String X_FRAME_OPTIONS = "DENY";

    /**
     * X-XSS-Protection
     */
    public static final String X_XSS_PROTECTION = "1; mode=block";

    /**
     * Strict-Transport-Security
     */
    public static final String STRICT_TRANSPORT_SECURITY = "max-age=31536000; includeSubDomains";

    /**
     * Content-Security-Policy
     */
    public static final String CONTENT_SECURITY_POLICY = 
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
            "style-src 'self' 'unsafe-inline'; " +
            "img-src 'self' data: https:; " +
            "font-src 'self' data:; " +
            "connect-src 'self'; " +
            "frame-ancestors 'none'";

    // ==================== Redis Key 前缀 ====================

    /**
     * 限流Key前缀
     */
    public static final String RATE_LIMIT_PREFIX = "rate_limit:";

    /**
     * 登录失败Key前缀
     */
    public static final String LOGIN_FAILED_PREFIX = "login_failed:";

    /**
     * 账户锁定Key前缀
     */
    public static final String ACCOUNT_LOCK_PREFIX = "account_lock:";

    /**
     * 在线用户Key前缀
     */
    public static final String ONLINE_USER_PREFIX = "online_user:";

    /**
     * JWT黑名单Key前缀
     */
    public static final String TOKEN_BLACKLIST_PREFIX = "token_blacklist:";

    /**
     * 密码历史Key前缀
     */
    public static final String PASSWORD_HISTORY_PREFIX = "password_history:";

    // ==================== 审计日志事件 ====================

    /**
     * 登录事件
     */
    public static final String EVENT_LOGIN = "LOGIN";

    /**
     * 登出事件
     */
    public static final String EVENT_LOGOUT = "LOGOUT";

    /**
     * 密码变更事件
     */
    public static final String EVENT_PASSWORD_CHANGE = "PASSWORD_CHANGE";

    /**
     * 账户锁定事件
     */
    public static final String EVENT_ACCOUNT_LOCK = "ACCOUNT_LOCK";

    /**
     * 账户解锁事件
     */
    public static final String EVENT_ACCOUNT_UNLOCK = "ACCOUNT_UNLOCK";

    /**
     * 权限变更事件
     */
    public static final String EVENT_PERMISSION_CHANGE = "PERMISSION_CHANGE";

    /**
     * MFA启用事件
     */
    public static final String EVENT_MFA_ENABLE = "MFA_ENABLE";

    /**
     * MFA禁用事件
     */
    public static final String EVENT_MFA_DISABLE = "MFA_DISABLE";

    /**
     * 异常访问事件
     */
    public static final String EVENT_ACCESS_DENIED = "ACCESS_DENIED";

    // ==================== 安全级别 ====================

    /**
     * 低风险
     */
    public static final int RISK_LOW = 1;

    /**
     * 中风险
     */
    public static final int RISK_MEDIUM = 2;

    /**
     * 高风险
     */
    public static final int RISK_HIGH = 3;

    /**
     * 严重风险
     */
    public static final int RISK_CRITICAL = 4;
}
