package com.qoobot.openidaas.starter.autoconfigure;

import com.qoobot.openidaas.starter.properties.OpenIDaaSProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 用户模块自动配置
 *
 * 自动配置以下功能：
 * 1. 用户服务
 * 2. 角色管理
 * 3. 权限管理
 *
 * @author OpenIDaaS Team
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(
    prefix = "openidaas.user",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(OpenIDaaSProperties.class)
public class OpenIDaaSUserAutoConfiguration {

    /**
     * 用户配置 Bean
     */
    @Bean
    public UserConfiguration userConfiguration(OpenIDaaSProperties properties) {
        log.info("Initializing user configuration");
        return new UserConfiguration(properties);
    }

    /**
     * 用户配置类
     */
    public static class UserConfiguration {
        private final OpenIDaaSProperties properties;

        public UserConfiguration(OpenIDaaSProperties properties) {
            this.properties = properties;
        }

        public OpenIDaaSProperties getProperties() {
            return properties;
        }
    }
}
