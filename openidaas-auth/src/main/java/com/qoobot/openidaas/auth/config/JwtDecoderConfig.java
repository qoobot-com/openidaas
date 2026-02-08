package com.qoobot.openidaas.auth.config;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

import java.time.Duration;
import java.util.Collections;

/**
 * JWT 解码器配置
 * 
 * 配置 JWT 令牌的解析和验证
 * 支持 JWK (JSON Web Key) 密钥轮换
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class JwtDecoderConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${openidaas.auth.jwt.expiration:86400}")
    private long jwtExpiration;

    /**
     * JWT 解码器 Bean
     * 使用 NimbusDS 库实现 JWT 解析和验证
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // 简单的 JWT 解码器配置
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        
        log.info("JWT Decoder configured with JWK Set URI: {}", jwkSetUri);
        
        return jwtDecoder;
    }

    /**
     * 自定义 JWT 解码器（支持自定义验证逻辑）
     */
    @Bean
    public JwtDecoder customJwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        
        return token -> {
            org.springframework.security.oauth2.jwt.Jwt decodedJwt = decoder.decode(token);
            
            // 可以在这里添加自定义验证逻辑
            log.debug("Decoded JWT for subject: {}", decodedJwt.getSubject());
            
            return decodedJwt;
        };
    }
}
