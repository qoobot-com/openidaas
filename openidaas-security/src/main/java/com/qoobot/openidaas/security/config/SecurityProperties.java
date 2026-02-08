package com.qoobot.openidaas.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 安全配置属性
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "openidaas.security")
public class SecurityProperties {

    /**
     * JWT配置
     */
    private Jwt jwt = new Jwt();

    /**
     * 密码策略配置
     */
    private Password password = new Password();

    /**
     * 限流配置
     */
    private RateLimit rateLimit = new RateLimit();

    /**
     * MFA配置
     */
    private Mfa mfa = new Mfa();

    /**
     * 审计配置
     */
    private Audit audit = new Audit();

    /**
     * CORS配置
     */
    private Cors cors = new Cors();

    /**
     * JWT配置
     */
    @Data
    public static class Jwt {
        /**
         * 签名密钥
         */
        private String secret = "openidaas-secret-key-change-in-production";

        /**
         * Token有效期（小时）
         */
        private Long expirationHours = 24L;

        /**
         * Refresh Token有效期（天）
         */
        private Long refreshExpirationDays = 7L;

        /**
         * Issuer
         */
        private String issuer = "openidaas";
    }

    /**
     * 密码策略配置
     */
    @Data
    public static class Password {
        /**
         * 最小长度
         */
        private Integer minLength = 8;

        /**
         * 最大长度
         */
        private Integer maxLength = 128;

        /**
         * 是否需要大写字母
         */
        private Boolean requireUppercase = true;

        /**
         * 是否需要小写字母
         */
        private Boolean requireLowercase = true;

        /**
         * 是否需要数字
         */
        private Boolean requireDigit = true;

        /**
         * 是否需要特殊字符
         */
        private Boolean requireSpecialChar = false;

        /**
         * 特殊字符集
         */
        private String specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";

        /**
         * 密码过期天数
         */
        private Integer expireDays = 90;

        /**
         * 密码历史记录数量
         */
        private Integer historyCount = 5;

        /**
         * 最大登录失败次数
         */
        private Integer maxFailedAttempts = 5;

        /**
         * 账户锁定时间（分钟）
         */
        private Integer lockMinutes = 30;
    }

    /**
     * 限流配置
     */
    @Data
    public static class RateLimit {
        /**
         * 是否启用限流
         */
        private Boolean enabled = true;

        /**
         * 默认令牌桶容量
         */
        private Integer defaultCapacity = 100;

        /**
         * 默认补充速率（令牌/秒）
         */
        private Integer defaultRefillRate = 10;

        /**
         * 登录令牌桶容量
         */
        private Integer loginCapacity = 5;

        /**
         * 登录补充速率（令牌/分钟）
         */
        private Integer loginRefillRate = 1;

        /**
         * API令牌桶容量
         */
        private Integer apiCapacity = 100;

        /**
         * API补充速率（令牌/秒）
         */
        private Integer apiRefillRate = 50;
    }

    /**
     * MFA配置
     */
    @Data
    public static class Mfa {
        /**
         * 是否启用MFA
         */
        private Boolean enabled = true;

        /**
         * 验证码长度
         */
        private Integer codeLength = 6;

        /**
         * 验证码有效期（秒）
         */
        private Integer codeExpireSeconds = 30;

        /**
         * Secret长度（字节）
         */
        private Integer secretSize = 20;

        /**
         * Issuer
         */
        private String issuer = "OpenIDaaS";
    }

    /**
     * 审计配置
     */
    @Data
    public static class Audit {
        /**
         * 是否启用审计
         */
        private Boolean enabled = true;

        /**
         * 是否记录敏感操作
         */
        private Boolean logSensitive = true;

        /**
         * 日志保留天数
         */
        private Integer retentionDays = 90;

        /**
         * 是否记录请求体
         */
        private Boolean logRequestBody = false;

        /**
         * 是否记录响应体
         */
        private Boolean logResponseBody = false;
    }

    /**
     * CORS配置
     */
    @Data
    public static class Cors {
        /**
         * 是否启用CORS
         */
        private Boolean enabled = true;

        /**
         * 允许的源
         */
        private String[] allowedOrigins = {"*"};

        /**
         * 允许的方法
         */
        private String[] allowedMethods = {"GET", "POST", "PUT", "DELETE", "OPTIONS"};

        /**
         * 允许的头
         */
        private String[] allowedHeaders = {"*"};

        /**
         * 允许凭证
         */
        private Boolean allowCredentials = false;

        /**
         * 预检请求缓存时间（秒）
         */
        private Long maxAge = 3600L;
    }
}
