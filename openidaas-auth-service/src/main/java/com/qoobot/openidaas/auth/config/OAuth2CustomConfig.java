package com.qoobot.openidaas.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.UUID;

/**
 * OAuth2 自定义配置
 */
@Configuration
public class OAuth2CustomConfig {

    /**
     * JWT Token 自定义器
     * 在 JWT Token 中添加自定义声明
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                // 添加自定义声明到 Access Token
                JwtClaimsSet.Builder claims = context.getClaims();
                claims.claim("custom_claim", "custom_value");

                // 添加用户详细信息
                if (context.getAuthorization() != null) {
                    String username = context.getAuthorization().getPrincipalName();
                    claims.claim("preferred_username", username);
                }
            } else if (OAuth2TokenType.REFRESH_TOKEN.equals(context.getTokenType())) {
                // Refresh Token 自定义
            }
        };
    }

    /**
     * OAuth2 授权服务配置
     */
    @Bean
    public OAuth2AuthorizationService authorizationService(
            RegisteredClientRepository registeredClientRepository,
            javax.sql.DataSource dataSource) {
        JdbcOAuth2AuthorizationService service = 
            new JdbcOAuth2AuthorizationService(
                jdbcTemplate(dataSource),
                registeredClientRepository
            );
        return service;
    }

    /**
     * OAuth2 授权确认服务
     */
    @Bean
    public org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService
    authorizationConsentService(RegisteredClientRepository registeredClientRepository,
                               javax.sql.DataSource dataSource) {
        return new org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService(
            jdbcTemplate(dataSource),
            registeredClientRepository
        );
    }

    /**
     * JDBC 模板
     */
    @Bean
    public org.springframework.jdbc.core.JdbcTemplate jdbcTemplate(
            javax.sql.DataSource dataSource) {
        return new org.springframework.jdbc.core.JdbcTemplate(dataSource);
    }
}
