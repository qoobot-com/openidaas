package com.qoobot.openidaas.starter.autoconfigure;

import com.qoobot.openidaas.starter.properties.OpenIDaaSProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;

import static com.qoobot.openidaas.starter.autoconfigure.AutoConfigurationHelper.isClassPresent;

/**
 * 认证模块自动配置
 *
 * 自动配置以下功能：
 * 1. OAuth2 Authorization Server
 * 2. JWT Token 配置
 * 3. OIDC 配置
 * 4. 会话管理
 *
 * @author OpenIDaaS Team
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(OAuth2AuthorizationServerConfiguration.class)
@ConditionalOnProperty(
    prefix = "openidaas.auth",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(OpenIDaaSProperties.class)
public class OpenIDaaSAuthAutoConfiguration {

    /**
     * JWT 配置 Bean
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "openidaas.auth.jwt",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    public JwtConfiguration jwtConfiguration(OpenIDaaSProperties properties) {
        log.info("Initializing JWT configuration");
        return new JwtConfiguration(properties.getAuth().getJwt());
    }

    /**
     * OAuth2 配置 Bean
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "openidaas.auth.oauth2",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    public OAuth2Configuration oauth2Configuration(OpenIDaaSProperties properties) {
        log.info("Initializing OAuth2 configuration");
        return new OAuth2Configuration(properties.getAuth().getOauth2());
    }

    /**
     * OIDC 配置 Bean
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "openidaas.auth.oidc",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    public OidcConfiguration oidcConfiguration(OpenIDaaSProperties properties) {
        log.info("Initializing OIDC configuration");
        return new OidcConfiguration(properties.getAuth().getOidc());
    }

    /**
     * 会话配置 Bean
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "openidaas.auth.session",
        name = "timeout"
    )
    public SessionConfiguration sessionConfiguration(OpenIDaaSProperties properties) {
        log.info("Initializing session configuration");
        return new SessionConfiguration(properties.getAuth().getSession());
    }

    /**
     * JWT 配置类
     */
    public static class JwtConfiguration {
        private final OpenIDaaSProperties.JwtProperties properties;

        public JwtConfiguration(OpenIDaaSProperties.JwtProperties properties) {
            this.properties = properties;
        }

        public OpenIDaaSProperties.JwtProperties getProperties() {
            return properties;
        }
    }

    /**
     * OAuth2 配置类
     */
    public static class OAuth2Configuration {
        private final OpenIDaaSProperties.OAuth2Properties properties;

        public OAuth2Configuration(OpenIDaaSProperties.OAuth2Properties properties) {
            this.properties = properties;
        }

        public OpenIDaaSProperties.OAuth2Properties getProperties() {
            return properties;
        }
    }

    /**
     * OIDC 配置类
     */
    public static class OidcConfiguration {
        private final OpenIDaaSProperties.OidcProperties properties;

        public OidcConfiguration(OpenIDaaSProperties.OidcProperties properties) {
            this.properties = properties;
        }

        public OpenIDaaSProperties.OidcProperties getProperties() {
            return properties;
        }
    }

    /**
     * 会话配置类
     */
    public static class SessionConfiguration {
        private final OpenIDaaSProperties.SessionProperties properties;

        public SessionConfiguration(OpenIDaaSProperties.SessionProperties properties) {
            this.properties = properties;
        }

        public OpenIDaaSProperties.SessionProperties getProperties() {
            return properties;
        }
    }
}
