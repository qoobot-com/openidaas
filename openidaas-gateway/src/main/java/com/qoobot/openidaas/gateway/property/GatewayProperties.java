package com.qoobot.openidaas.gateway.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 网关配置属性
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "openidaas.gateway")
public class GatewayProperties {

    /**
     * 用户服务URI
     */
    private String userServiceUri = "lb://openidaas-user";

    /**
     * 认证服务URI
     */
    private String authServiceUri = "lb://openidaas-auth";

    /**
     * 租户服务URI
     */
    private String tenantServiceUri = "lb://openidaas-tenant";

    /**
     * 安全服务URI
     */
    private String securityServiceUri = "lb://openidaas-security";

    /**
     * 内部服务URI
     */
    private String internalServiceUri = "lb://openidaas-internal";

    /**
     * 限流配置
     */
    private RateLimit rateLimit = new RateLimit();

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
         * 默认容量
         */
        private Integer defaultCapacity = 100;

        /**
         * 默认补充速率（令牌/秒）
         */
        private Integer defaultRefillRate = 50;

        /**
         * 登录容量
         */
        private Integer loginCapacity = 10;

        /**
         * 登录补充速率（令牌/分钟）
         */
        private Integer loginRefillRate = 5;
    }
}
