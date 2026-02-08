package com.qoobot.openidaas.starter.autoconfigure;

import com.qoobot.openidaas.starter.properties.OpenIDaaSProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static com.qoobot.openidaas.starter.autoconfigure.AutoConfigurationHelper.isClassPresent;

/**
 * 安全模块自动配置
 *
 * 自动配置以下功能：
 * 1. 密码策略验证器
 * 2. MFA 服务
 * 3. 限流过滤器
 * 4. 访问控制
 *
 * @author OpenIDaaS Team
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration(after = SecurityFilterAutoConfiguration.class)
@ConditionalOnClass(name = "org.springframework.security.config.annotation.web.configuration.EnableWebSecurity")
@ConditionalOnWebApplication
@ConditionalOnProperty(
    prefix = "openidaas.security",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(OpenIDaaSProperties.class)
public class OpenIDaaSSecurityAutoConfiguration {

    /**
     * 密码策略配置 Bean
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "openidaas.security.password-policy",
        name = "min-length"
    )
    public PasswordPolicyConfiguration passwordPolicyConfiguration(OpenIDaaSProperties properties) {
        log.info("Initializing password policy configuration");
        return new PasswordPolicyConfiguration(properties.getSecurity().getPasswordPolicy());
    }

    /**
     * MFA 配置 Bean
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "openidaas.security.mfa",
        name = "enabled"
    )
    public MfaConfiguration mfaConfiguration(OpenIDaaSProperties properties) {
        log.info("Initializing MFA configuration");
        return new MfaConfiguration(properties.getSecurity().getMfa());
    }

    /**
     * 限流配置 Bean
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "openidaas.security.rate-limit",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    public RateLimitConfiguration rateLimitConfiguration(OpenIDaaSProperties properties) {
        log.info("Initializing rate limit configuration");
        return new RateLimitConfiguration(properties.getSecurity().getRateLimit());
    }

    /**
     * 访问控制配置 Bean
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "openidaas.security.access-control",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    public AccessControlConfiguration accessControlConfiguration(OpenIDaaSProperties properties) {
        log.info("Initializing access control configuration");
        return new AccessControlConfiguration(properties.getSecurity().getAccessControl());
    }

    /**
     * 密码策略配置类
     */
    public static class PasswordPolicyConfiguration {
        private final OpenIDaaSProperties.PasswordPolicyProperties properties;

        public PasswordPolicyConfiguration(OpenIDaaSProperties.PasswordPolicyProperties properties) {
            this.properties = properties;
        }

        public OpenIDaaSProperties.PasswordPolicyProperties getProperties() {
            return properties;
        }
    }

    /**
     * MFA 配置类
     */
    public static class MfaConfiguration {
        private final OpenIDaaSProperties.MfaProperties properties;

        public MfaConfiguration(OpenIDaaSProperties.MfaProperties properties) {
            this.properties = properties;
        }

        public OpenIDaaSProperties.MfaProperties getProperties() {
            return properties;
        }
    }

    /**
     * 限流配置类
     */
    public static class RateLimitConfiguration {
        private final OpenIDaaSProperties.RateLimitProperties properties;

        public RateLimitConfiguration(OpenIDaaSProperties.RateLimitProperties properties) {
            this.properties = properties;
        }

        public OpenIDaaSProperties.RateLimitProperties getProperties() {
            return properties;
        }
    }

    /**
     * 访问控制配置类
     */
    public static class AccessControlConfiguration {
        private final OpenIDaaSProperties.AccessControlProperties properties;

        public AccessControlConfiguration(OpenIDaaSProperties.AccessControlProperties properties) {
            this.properties = properties;
        }

        public OpenIDaaSProperties.AccessControlProperties getProperties() {
            return properties;
        }
    }
}
