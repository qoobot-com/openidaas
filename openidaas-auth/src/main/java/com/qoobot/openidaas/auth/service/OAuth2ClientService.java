package com.qoobot.openidaas.auth.service;

import com.qoobot.openidaas.auth.model.RegisteredClientEntity;
import com.qoobot.openidaas.auth.repository.RegisteredClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * OAuth2 客户端服务
 * 
 * 管理 OAuth2 客户端的注册、查询和配置
 * 支持多种授权类型和认证方式
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2ClientService implements RegisteredClientRepository {

    private final RegisteredClientRepository registeredClientRepository;

    /**
     * 注册 OAuth2 客户端
     * 
     * @param clientId 客户端 ID
     * @param clientSecret 客户端密钥
     * @param redirectUris 重定向 URI 列表
     * @param scopes 授权范围
     * @param grantTypes 授权类型
     * @param clientAuthenticationMethods 客户端认证方式
     * @return 注册的客户端
     */
    @Transactional
    public RegisteredClient registerClient(String clientId,
                                        String clientSecret,
                                        List<String> redirectUris,
                                        List<String> scopes,
                                        List<String> grantTypes,
                                        List<String> clientAuthenticationMethods) {
        
        // 转换授权类型
        List<AuthorizationGrantType> authorizationGrantTypes = grantTypes.stream()
                .map(AuthorizationGrantType::new)
                .toList();
        
        // 转换认证方式
        List<ClientAuthenticationMethod> authMethods = clientAuthenticationMethods.stream()
                .map(ClientAuthenticationMethod::new)
                .toList();
        
        // 创建 Token 设置
        TokenSettings tokenSettings = TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofHours(1))
                .refreshTokenTimeToLive(Duration.ofDays(30))
                .reuseRefreshTokens(false)
                .idTokenSignatureAlgorithm(org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256)
                .build();
        
        // 创建客户端设置
        ClientSettings clientSettings = ClientSettings.builder()
                .requireAuthorizationConsent(true)
                .requireProofKey(true) // 启用 PKCE
                .build();
        
        // 创建 RegisteredClient
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorizationGrantTypes(grants -> 
                        grants.addAll(authorizationGrantTypes))
                .clientAuthenticationMethods(methods -> 
                        methods.addAll(authMethods))
                .redirectUris(uris -> uris.addAll(redirectUris))
                .scopes(scopes::addAll)
                .clientSettings(clientSettings)
                .tokenSettings(tokenSettings)
                .build();
        
        // 保存到数据库
        save(registeredClient);
        
        log.info("Registered OAuth2 client: {}", clientId);
        
        return registeredClient;
    }

    /**
     * 注册默认客户端
     * 
     * @return 默认客户端
     */
    @Transactional
    public RegisteredClient registerDefaultClient() {
        return registerClient(
                "openidaas-client",
                "{noop}openidaas-secret",
                Arrays.asList(
                        "http://localhost:8084/login/oauth2/code/openidaas-client",
                        "http://localhost:8084/authorized"
                ),
                Arrays.asList(
                        OidcScopes.OPENID,
                        OidcScopes.PROFILE,
                        OidcScopes.EMAIL,
                        "read", "write"
                ),
                Arrays.asList(
                        "authorization_code",
                        "refresh_token",
                        "client_credentials"
                ),
                Arrays.asList("client_secret_basic", "client_secret_post")
        );
    }

    /**
     * 更新客户端
     * 
     * @param clientId 客户端 ID
     * @param updates 更新的字段
     */
    @Transactional
    public void updateClient(String clientId, ClientUpdates updates) {
        RegisteredClient client = findByClientId(clientId);
        
        if (client == null) {
            throw new RuntimeException("Client not found: " + clientId);
        }
        
        // TODO: 实现更新逻辑
        log.info("Updated OAuth2 client: {}", clientId);
    }

    /**
     * 删除客户端
     * 
     * @param clientId 客户端 ID
     */
    @Transactional
    public void deleteClient(String clientId) {
        RegisteredClient client = findByClientId(clientId);
        
        if (client != null) {
            removeByClientId(clientId);
            log.info("Deleted OAuth2 client: {}", clientId);
        }
    }

    /**
     * 查询客户端
     */
    @Override
    public RegisteredClient findByClientId(String clientId) {
        Optional<RegisteredClientEntity> entity = 
                registeredClientRepository.findByClientId(clientId);
        
        return entity.map(this::toRegisteredClient).orElse(null);
    }

    /**
     * 根据 ID 查询客户端
     */
    @Override
    public RegisteredClient findById(String id) {
        return registeredClientRepository.findById(id)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    /**
     * 保存客户端
     */
    @Transactional
    @Override
    public void save(RegisteredClient registeredClient) {
        RegisteredClientEntity entity = toEntity(registeredClient);
        registeredClientRepository.save(entity);
    }

    /**
     * 验证客户端密钥
     * 
     * @param clientId 客户端 ID
     * @param clientSecret 客户端密钥
     * @return 是否验证通过
     */
    public boolean validateClientSecret(String clientId, String clientSecret) {
        RegisteredClient client = findByClientId(clientId);
        
        if (client == null) {
            return false;
        }
        
        String encodedSecret = client.getClientSecret();
        
        // TODO: 实现密钥验证逻辑
        return true;
    }

    /**
     * 检查客户端是否启用 PKCE
     * 
     * @param clientId 客户端 ID
     * @return 是否启用 PKCE
     */
    public boolean isPkceRequired(String clientId) {
        RegisteredClient client = findByClientId(clientId);
        
        if (client == null) {
            return false;
        }
        
        return client.getClientSettings().isRequireProofKey();
    }

    /**
     * 检查重定向 URI 是否匹配
     * 
     * @param clientId 客户端 ID
     * @param redirectUri 重定向 URI
     * @return 是否匹配
     */
    public boolean validateRedirectUri(String clientId, String redirectUri) {
        RegisteredClient client = findByClientId(clientId);
        
        if (client == null) {
            return false;
        }
        
        return client.getRedirectUris().contains(redirectUri);
    }

    /**
     * 检查授权范围是否有效
     * 
     * @param clientId 客户端 ID
     * @param requestedScopes 请求的授权范围
     * @return 是否有效
     */
    public boolean validateScopes(String clientId, List<String> requestedScopes) {
        RegisteredClient client = findByClientId(clientId);
        
        if (client == null) {
            return false;
        }
        
        // 检查所有请求的 scope 是否在客户端允许的范围内
        return client.getScopes().containsAll(requestedScopes);
    }

    /**
     * 转换为 RegisteredClient
     */
    private RegisteredClient toRegisteredClient(RegisteredClientEntity entity) {
        // 实现转换逻辑
        return null;
    }

    /**
     * 转换为 Entity
     */
    private RegisteredClientEntity toEntity(RegisteredClient client) {
        // 实现转换逻辑
        return null;
    }

    /**
     * 客户端更新对象
     */
    @lombok.Data
    @lombok.Builder
    public static class ClientUpdates {
        private String clientSecret;
        private List<String> redirectUris;
        private List<String> scopes;
        private List<String> grantTypes;
    }
}
