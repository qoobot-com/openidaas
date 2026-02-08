package com.qoobot.openidaas.auth.config;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.qoobot.openidaas.auth.service.OAuth2ClientService;
import com.qoobot.openidaas.auth.service.RegisteredClientRepositoryService;
import com.qoobot.openidaas.auth.service.TokenRevocationService;
import com.qoobot.openidaas.auth.service.UserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * OpenID Authorization Server 配置
 * 
 * 实现 OAuth2.1/OIDC 标准协议
 * 支持 Authorization Code Flow、Client Credentials Flow、PKCE
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class OpenIdAuthorizationServerConfig {

    private final PasswordEncoder passwordEncoder;
    private final OAuth2ClientService oAuth2ClientService;
    private final UserDetailsService userDetailsService;
    private final TokenRevocationService tokenRevocationService;

    /**
     * 配置 OAuth2 Authorization Server
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults()) // 启用 OIDC
                .tokenEndpoint(tokenEndpoint -> 
                        tokenEndpoint.accessTokenRequestConverter(
                                new CustomTokenRequestConverter()))
                .clientAuthentication(clientAuthentication -> 
                        clientAuthentication.errorResponseHandler(
                                new CustomClientAuthenticationErrorResponseHandler()))
                .authorizationEndpoint(authorizationEndpoint ->
                        authorizationEndpoint.consentPage("/oauth2/consent"))
                .oidcProviderConfigurationEndpoint(Customizer.withDefaults());

        http
                // 匹配授权服务器的端点
                .securityMatchers(matchers -> matchers
                        .requestMatchers("/oauth2/**", "/.well-known/**"))
                // 禁用 CSRF
                .csrf(csrf -> csrf.ignoringRequestMatchers("/oauth2/**", "/.well-known/**"))
                // 配置异常处理
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
                // 配置 OAuth2 自定义端点
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(Customizer.withDefaults()));

        return http.build();
    }

    /**
     * 注册客户端 Repository
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        return new RegisteredClientRepositoryService();
    }

    /**
     * JWK 源（JSON Web Key）
     * 用于 JWT 签名和验证
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = generateRsaKey();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    /**
     * JWT 解码器
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * Authorization Server 设置
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:8081")
                .authorizationEndpoint("/oauth2/authorize")
                .tokenEndpoint("/oauth2/token")
                .jwkSetUri("/.well-known/jwks.json")
                .oidcUserInfoEndpoint("/userinfo")
                .oidcClientRegistrationEndpoint("/connect/register")
                .build();
    }

    /**
     * OAuth2 Token 自定义器
     * 用于在 JWT 中添加自定义 claims
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            if (context.getTokenType().getValue().equals("id_token") ||
                context.getTokenType().getValue().equals("access_token")) {
                
                // 添加租户 ID
                String tenantId = context.getAuthorizationGrant().getName();
                context.getClaims().claim("tenant_id", tenantId);
                
                // 添加令牌 ID（用于撤销）
                String tokenId = UUID.randomUUID().toString();
                context.getClaims().claim("jti", tokenId);
                
                // 添加令牌类型
                context.getClaims().claim("token_type", context.getTokenType().getValue());
                
                // 添加用户详情（ID Token）
                if (context.getTokenType().getValue().equals("id_token")) {
                    OidcUserInfo userInfo = userDetailsService.loadUser(
                            context.getPrincipal().getName());
                    context.getClaims()
                            .claim(OAuth2TokenIntrospectionClaimNames.EMAIL, userInfo.getEmail())
                            .claim(OAuth2TokenIntrospectionClaimNames.PICTURE, userInfo.getPicture())
                            .claim(OAuth2TokenIntrospectionClaimNames.PREFERRED_USERNAME, 
                                    userInfo.getPreferredUsername());
                }
            }
        };
    }

    /**
     * 生成 RSA 密钥对
     */
    private RSAKey generateRsaKey() {
        try {
            RSAKey key = RSAKey.generateKey(2048);
            return new RSAKey.Builder(key.toRSAPublicKey())
                    .privateKey(key.toRSAPrivateKey())
                    .keyID(UUID.randomUUID().toString())
                    .keyUse(com.nimbusds.jose.JWKUse.SIG)
                    .algorithm(org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey.SIGNATURE_ALGORITHM)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate RSA key", e);
        }
    }

    /**
     * 自定义 Token 请求转换器
     */
    public static class CustomTokenRequestConverter implements
            org.springframework.security.web.authentication.AuthenticationConverter {
        
        @Override
        public org.springframework.security.core.Authentication convert(
                jakarta.servlet.http.HttpServletRequest request) {
            // 实现自定义的 Token 请求解析逻辑
            // 支持 PKCE、JWT bearer token 等
            return null;
        }
    }

    /**
     * 自定义客户端认证错误响应处理器
     */
    public static class CustomClientAuthenticationErrorResponseHandler
            implements org.springframework.security.web.authentication.AuthenticationFailureHandler {
        
        @Override
        public void onAuthenticationFailure(
                jakarta.servlet.http.HttpServletRequest request,
                jakarta.servlet.http.HttpServletResponse response,
                org.springframework.security.core.AuthenticationException exception)
                throws java.io.IOException {
            
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(401);
            
            String responseBody = String.format(
                    "{\"error\":\"invalid_client\",\"error_description\":\"%s\"}",
                    exception.getMessage());
            
            response.getWriter().write(responseBody);
        }
    }
}
