package com.qoobot.openidaas.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * OpenIDaaS 配置属性
 *
 * 通过 application.yml 或 application.properties 配置：
 *
 * <pre>
 * openidaas:
 *   enabled: true
 *   auth:
 *     jwt:
 *       secret: your-secret-key
 *       expiration: 3600
 *   security:
 *     password-policy:
 *       min-length: 8
 *   tenant:
 *     isolation-strategy: SCHEMA
 * </pre>
 *
 * @author OpenIDaaS Team
 * @since 1.0.0
 */
@Data
@Validated
@ConfigurationProperties(prefix = "openidaas")
public class OpenIDaaSProperties {

    /**
     * 是否启用 OpenIDaaS
     */
    private boolean enabled = true;

    /**
     * 认证配置
     */
    @Valid
    private AuthProperties auth = new AuthProperties();

    /**
     * 安全配置
     */
    @Valid
    private SecurityProperties security = new SecurityProperties();

    /**
     * 租户配置
     */
    @Valid
    private TenantProperties tenant = new TenantProperties();

    /**
     * 监控配置
     */
    @Valid
    private MonitoringProperties monitoring = new MonitoringProperties();

    /**
     * 缓存配置
     */
    @Valid
    private CacheProperties cache = new CacheProperties();

    /**
     * 数据库配置
     */
    @Valid
    private DatabaseProperties database = new DatabaseProperties();

    /**
     * 认证配置
     */
    @Data
    public static class AuthProperties {

        /**
         * 是否启用认证
         */
        private boolean enabled = true;

        /**
         * JWT 配置
         */
        @Valid
        private JwtProperties jwt = new JwtProperties();

        /**
         * OAuth2 配置
         */
        @Valid
        private OAuth2Properties oauth2 = new OAuth2Properties();

        /**
         * OIDC 配置
         */
        @Valid
        private OidcProperties oidc = new OidcProperties();

        /**
         * 会话配置
         */
        @Valid
        private SessionProperties session = new SessionProperties();
    }

    /**
     * JWT 配置
     */
    @Data
    public static class JwtProperties {

        /**
         * 是否启用 JWT
         */
        private boolean enabled = true;

        /**
         * JWT 密钥
         */
        @NotBlank(message = "JWT secret cannot be blank")
        private String secret = "openidaas-default-secret-key-change-in-production";

        /**
         * Access Token 过期时间（秒）
         */
        private Long expiration = 3600L;

        /**
         * Refresh Token 过期时间（秒）
         */
        private Long refreshExpiration = 2592000L;

        /**
         * Token 签发者
         */
        private String issuer = "openidaas";

        /**
         * Token 算法 (HS256, RS256)
         */
        private String algorithm = "HS256";

        /**
         * 密钥文件路径（RS256 算法使用）
         */
        private String privateKeyPath;

        /**
         * 公钥文件路径（RS256 算法使用）
         */
        private String publicKeyPath;
    }

    /**
     * OAuth2 配置
     */
    @Data
    public static class OAuth2Properties {

        /**
         * 是否启用 OAuth2
         */
        private boolean enabled = true;

        /**
         * 授权码有效期（秒）
         */
        private Long authorizationCodeValidity = 300L;

        /**
         * 访问令牌有效期（秒）
         */
        private Long accessTokenValidity = 3600L;

        /**
         * 刷新令牌有效期（秒）
         */
        private Long refreshTokenValidity = 2592000L;

        /**
         * 是否要求 PKCE
         */
        private boolean requireProofKey = false;

        /**
         * 是否要求授权同意
         */
        private boolean requireAuthorizationConsent = false;
    }

    /**
     * OIDC 配置
     */
    @Data
    public static class OidcProperties {

        /**
         * 是否启用 OIDC
         */
        private boolean enabled = true;

        /**
         * 是否启用 UserInfo 端点
         */
        private boolean userInfoEnabled = true;

        /**
         * 是否启用 Client Registration 端点
         */
        private boolean clientRegistrationEnabled = true;
    }

    /**
     * 会话配置
     */
    @Data
    public static class SessionProperties {

        /**
         * 会话超时时间（秒）
         */
        private Long timeout = 1800L;

        /**
         * 记住我有效时间（秒）
         */
        private Long rememberMeDuration = 2592000L;

        /**
         * 每用户最大并发会话数
         */
        private Integer maxConcurrentSessions = 5;

        /**
         * 是否允许并发登录
         */
        private boolean allowConcurrentLogin = true;

        /**
         * 会话固定保护策略
         */
        private String sessionFixationProtection = "migrateSession";
    }

    /**
     * 安全配置
     */
    @Data
    public static class SecurityProperties {

        /**
         * 是否启用安全功能
         */
        private boolean enabled = true;

        /**
         * 密码策略配置
         */
        @Valid
        private PasswordPolicyProperties passwordPolicy = new PasswordPolicyProperties();

        /**
         * MFA 配置
         */
        @Valid
        private MfaProperties mfa = new MfaProperties();

        /**
         * 限流配置
         */
        @Valid
        private RateLimitProperties rateLimit = new RateLimitProperties();

        /**
         * 访问控制配置
         */
        @Valid
        private AccessControlProperties accessControl = new AccessControlProperties();
    }

    /**
     * 密码策略配置
     */
    @Data
    public static class PasswordPolicyProperties {

        /**
         * 最小长度
         */
        @NotNull(message = "Password min length cannot be null")
        private Integer minLength = 8;

        /**
         * 最大长度
         */
        private Integer maxLength = 128;

        /**
         * 是否要求大写字母
         */
        private boolean requireUppercase = true;

        /**
         * 是否要求小写字母
         */
        private boolean requireLowercase = true;

        /**
         * 是否要求数字
         */
        private boolean requireNumbers = true;

        /**
         * 是否要求特殊字符
         */
        private boolean requireSpecialChars = true;

        /**
         * 密码历史记录数量
         */
        private Integer passwordHistory = 5;

        /**
         * 密码过期天数（0 表示不过期）
         */
        private Integer expirationDays = 90;

        /**
         * 禁止的特殊字符列表
         */
        private String[] forbiddenChars;
    }

    /**
     * MFA 配置
     */
    @Data
    public static class MfaProperties {

        /**
         * 是否启用 MFA
         */
        private boolean enabled = false;

        /**
         * 是否要求管理员必须启用 MFA
         */
        private boolean requiredForAdmin = true;

        /**
         * 支持的 MFA 类型
         */
        private String[] supportedTypes = {"TOTP", "SMS", "EMAIL"};

        /**
         * 默认 MFA 类型
         */
        private String defaultType = "TOTP";

        /**
         * 备用码数量
         */
        private Integer backupCodesCount = 10;

        /**
         * TOTP 有效期（秒）
         */
        private Integer totpValidity = 30;

        /**
         * 是否允许记住设备（30天）
         */
        private boolean rememberDevice = true;
    }

    /**
     * 限流配置
     */
    @Data
    public static class RateLimitProperties {

        /**
         * 是否启用限流
         */
        private boolean enabled = true;

        /**
         * 限流算法 (TOKEN_BUCKET, FIXED_WINDOW, SLIDING_WINDOW)
         */
        private String algorithm = "TOKEN_BUCKET";

        /**
         * 每分钟最大请求次数
         */
        private Integer requestsPerMinute = 100;

        /**
         * 每小时最大请求次数
         */
        private Integer requestsPerHour = 1000;

        /**
         * 每天最大请求次数
         */
        private Integer requestsPerDay = 10000;

        /**
         * Token 桶容量
         */
        private Integer bucketCapacity = 100;

        /**
         * Token 填充速率（每秒）
         */
        private Integer refillRate = 10;
    }

    /**
     * 访问控制配置
     */
    @Data
    public static class AccessControlProperties {

        /**
         * 是否启用访问控制
         */
        private boolean enabled = true;

        /**
         * 默认访问策略 (DENY_ALL, PERMIT_ALL)
         */
        private String defaultPolicy = "DENY_ALL";

        /**
         * 是否启用 IP 白名单
         */
        private boolean ipWhitelistEnabled = false;

        /**
         * IP 白名单
         */
        private String[] ipWhitelist;

        /**
         * 是否启用 IP 黑名单
         */
        private boolean ipBlacklistEnabled = false;

        /**
         * IP 黑名单
         */
        private String[] ipBlacklist;

        /**
         * 是否启用 CORS
         */
        private boolean corsEnabled = true;

        /**
         * CORS 允许的源
         */
        private String[] corsAllowedOrigins;

        /**
         * CORS 允许的方法
         */
        private String[] corsAllowedMethods = {"GET", "POST", "PUT", "DELETE", "OPTIONS"};

        /**
         * CORS 允许的头部
         */
        private String[] corsAllowedHeaders = {"*"};
    }

    /**
     * 租户配置
     */
    @Data
    public static class TenantProperties {

        /**
         * 是否启用多租户
         */
        private boolean enabled = true;

        /**
         * 租户隔离策略 (NONE, SCHEMA, DATABASE)
         */
        @NotNull(message = "Tenant isolation strategy cannot be null")
        private String isolationStrategy = "SCHEMA";

        /**
         * 默认租户 ID
         */
        private String defaultTenantId = "00000000-0000-0000-0000-000000000001";

        /**
         * 租户识别方式 (HEADER, COOKIE, PATH)
         */
        private String tenantResolver = "HEADER";

        /**
         * 租户识别头名称
         */
        private String tenantHeaderName = "X-Tenant-ID";

        /**
         * 租户识别 Cookie 名称
         */
        private String tenantCookieName = "tenant_id";

        /**
         * 是否启用租户缓存
         */
        private boolean cacheEnabled = true;

        /**
         * 租户缓存过期时间（秒）
         */
        private Long cacheExpiration = 3600L;
    }

    /**
     * 监控配置
     */
    @Data
    public static class MonitoringProperties {

        /**
         * 是否启用监控
         */
        private boolean enabled = true;

        /**
         * 是否启用指标收集
         */
        private boolean metricsEnabled = true;

        /**
         * 指标导出方式 (PROMETHEUS, INFLUX, LOGGING)
         */
        private String metricsExport = "PROMETHEUS";

        /**
         * 指标前缀
         */
        private String metricsPrefix = "openidaas";

        /**
         * 是否启用审计日志
         */
        private boolean auditEnabled = true;

        /**
         * 审计日志级别 (INFO, WARN, ERROR, ALL)
         */
        private String auditLevel = "INFO";

        /**
         * 是否启用性能追踪
         */
        private boolean tracingEnabled = false;

        /**
         * 追踪采样率 (0.0 - 1.0)
         */
        private Double tracingSamplingRate = 0.1;

        /**
         * 是否启用健康检查
         */
        private boolean healthCheckEnabled = true;
    }

    /**
     * 缓存配置
     */
    @Data
    public static class CacheProperties {

        /**
         * 缓存类型 (REDIS, HAZELCAST, CAFFEINE, SIMPLE)
         */
        private String type = "REDIS";

        /**
         * 默认缓存过期时间（秒）
         */
        private Long defaultExpiration = 1800L;

        /**
         * 用户信息缓存过期时间（秒）
         */
        private Long userInfoExpiration = 1800L;

        /**
         * Token 缓存过期时间（秒）
         */
        private Long tokenExpiration = 3600L;

        /**
         * 权限缓存过期时间（秒）
         */
        private Long permissionExpiration = 600L;

        /**
         * 是否启用本地缓存
         */
        private boolean localCacheEnabled = true;

        /**
         * 本地缓存最大条目数
         */
        private Integer localCacheMaxSize = 1000;
    }

    /**
     * 数据库配置
     */
    @Data
    public static class DatabaseProperties {

        /**
         * 是否启用数据库自动初始化
         */
        private boolean autoInit = false;

        /**
         * Schema 初始化位置
         */
        private String schemaLocation = "classpath:db/schema.sql";

        /**
         * 数据初始化位置
         */
        private String dataLocation = "classpath:db/init-data.sql";

        /**
         * 是否启用 Flyway 迁移
         */
        private boolean flywayEnabled = false;

        /**
         * Flyway 迁移脚本位置
         */
        private String flywayLocations = "classpath:db/migration";

        /**
         * 是否启用 Liquibase 迁移
         */
        private boolean liquibaseEnabled = false;

        /**
         * Liquibase 变更日志位置
         */
        private String liquibaseChangeLog = "classpath:db/liquibase/changelog-master.xml";
    }
}
