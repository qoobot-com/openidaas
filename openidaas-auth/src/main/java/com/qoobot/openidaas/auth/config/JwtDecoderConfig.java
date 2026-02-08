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
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource,
                                 AuthorizationServerSettings authorizationServerSettings) {
        
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = 
                new DefaultJWTProcessor<>();
        
        // 配置 JWS Key 选择器
        JWSKeySelector<SecurityContext> jwsKeySelector = 
                new JWSVerificationKeySelector<>(
                        Collections.singletonList(JOSEObjectType.JWT),
                        jwkSource);
        
        jwtProcessor.setJWSKeySelector(jwsKeySelector);
        
        // 配置 JWT 处理器选项
        jwtProcessor.setJWTClaimsSetVerifier(
                new DefaultJWTClaimsVerifier.Builder<SecurityContext>()
                        .build());
        
        // 配置 JWK Set 资源获取器
        DefaultResourceRetriever resourceRetriever = new DefaultResourceRetriever();
        resourceRetriever.setConnectTimeout(Duration.ofSeconds(5));
        resourceRetriever.setReadTimeout(Duration.ofSeconds(5));
        
        // 创建 JWT 解码器
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                .withJwkSetUri(jwkSetUri)
                .jwkSetUriResolver(uri -> 
                    authorizationServerSettings.getJwkSetUri())
                .resourceRetriever(resourceRetriever)
                .processor(jwtProcessor)
                .build();
        
        log.info("JWT Decoder configured with JWK Set URI: {}", jwkSetUri);
        
        return jwtDecoder;
    }

    /**
     * 自定义 JWT 解码器（支持自定义验证逻辑）
     */
    @Bean
    public JwtDecoder customJwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtDecoder(jwkSource) {
            @Override
            public org.springframework.security.oauth2.jwt.Jwt decode(
                    String token) throws Exception {
                
                // 解析 JWT
                JWT jwt = JWTParser.parse(token);
                
                // 验证 JWT 签名
                validateJwtSignature(jwt);
                
                // 验证 JWT claims
                validateJwtClaims(jwt);
                
                // 调用父类解码方法
                return super.decode(token);
            }
            
            /**
             * 验证 JWT 签名
             */
            private void validateJwtSignature(JWT jwt) {
                // 实现签名验证逻辑
                // 检查算法、密钥等
                log.debug("Validating JWT signature for token: {}", jwt.getHeader().getKeyID());
            }
            
            /**
             * 验证 JWT Claims
             */
            private void validateJwtClaims(JWT jwt) {
                // 实现 Claims 验证逻辑
                // 检查过期时间、发行者、受众等
                log.debug("Validating JWT claims for token: {}", jwt.getJWTClaimsSet().getSubject());
            }
        };
    }
}
