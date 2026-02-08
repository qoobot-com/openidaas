package com.qoobot.openidaas.starter.autoconfigure;

import com.qoobot.openidaas.starter.properties.OpenIDaaSProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 网关模块自动配置
 *
 * 自动配置以下功能：
 * 1. API 路由
 * 2. 认证过滤器
 * 3. 限流配置
 * 4. 监控统计
 *
 * @author OpenIDaaS Team
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.cloud.gateway.config.GatewayAutoConfiguration")
@ConditionalOnProperty(
    prefix = "openidaas.gateway",
    name = "enabled",
    havingValue = "true"
)
@EnableConfigurationProperties(OpenIDaaSProperties.class)
public class OpenIDaaSGatewayAutoConfiguration {

    /**
     * 网关配置 Bean
     */
    @Bean
    public GatewayConfiguration gatewayConfiguration(OpenIDaaSProperties properties) {
        log.info("Initializing gateway configuration");
        return new GatewayConfiguration(properties);
    }

    /**
     * 网关配置类
     */
    public static class GatewayConfiguration {
        private final OpenIDaaSProperties properties;

        public GatewayConfiguration(OpenIDaaSProperties properties) {
            this.properties = properties;
        }

        public OpenIDaaSProperties getProperties() {
            return properties;
        }
    }
}
