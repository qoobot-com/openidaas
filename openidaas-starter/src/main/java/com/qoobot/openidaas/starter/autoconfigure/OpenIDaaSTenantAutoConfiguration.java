package com.qoobot.openidaas.starter.autoconfigure;

import com.qoobot.openidaas.starter.properties.OpenIDaaSProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 租户模块自动配置
 *
 * 自动配置以下功能：
 * 1. 租户隔离策略
 * 2. 租户识别器
 * 3. 租户缓存
 *
 * @author OpenIDaaS Team
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(
    prefix = "openidaas.tenant",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(OpenIDaaSProperties.class)
public class OpenIDaaSTenantAutoConfiguration {

    /**
     * 租户配置 Bean
     */
    @Bean
    public TenantConfiguration tenantConfiguration(OpenIDaaSProperties properties) {
        log.info("Initializing tenant configuration with isolation strategy: {}",
                properties.getTenant().getIsolationStrategy());
        return new TenantConfiguration(properties.getTenant());
    }

    /**
     * 租户配置类
     */
    public static class TenantConfiguration {
        private final OpenIDaaSProperties.TenantProperties properties;

        public TenantConfiguration(OpenIDaaSProperties.TenantProperties properties) {
            this.properties = properties;
        }

        public OpenIDaaSProperties.TenantProperties getProperties() {
            return properties;
        }
    }
}
