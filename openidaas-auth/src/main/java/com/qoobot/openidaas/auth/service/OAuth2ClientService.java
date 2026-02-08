package com.qoobot.openidaas.auth.service;

import com.qoobot.openidaas.auth.model.RegisteredClientEntity;
import com.qoobot.openidaas.auth.repository.RegisteredClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
public class OAuth2ClientService implements org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository {

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
    public org.springframework.security.oauth2.server.authorization.client.RegisteredClient registerClient(String clientId,
                                        String clientSecret,
                                        List<String> redirectUris,
                                        List<String> scopes,
                                        List<String> grantTypes,
                                        List<String> clientAuthenticationMethods) {
        
        // 转换授权类型
        List<AuthorizationGrantType> authorizationGrantTypes = grantTypes.stream()
                .map(AuthorizationGrantType::new)
                .collect(Collectors.toList());
        
        // 转换认证方式
        List<ClientAuthenticationMethod> authMethods = clientAuthenticationMethods.stream()
                .map(ClientAuthenticationMethod::new)
                .collect(Collectors.toList());
        
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
        org.springframework.security.oauth2.server.authorization.client.RegisteredClient registeredClient = 
            org.springframework.security.oauth2.server.authorization.client.RegisteredClient.withId(UUID.randomUUID().toString())
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
    public org.springframework.security.oauth2.server.authorization.client.RegisteredClient registerDefaultClient() {
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
        org.springframework.security.oauth2.server.authorization.client.RegisteredClient client = findByClientId(clientId);
        
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
        org.springframework.security.oauth2.server.authorization.client.RegisteredClient client = findByClientId(clientId);
        
        if (client != null) {
            registeredClientRepository.deleteByClientId(clientId);
            log.info("Deleted OAuth2 client: {}", clientId);
        }
    }

    /**
     * 查询客户端
     */
    @Override
    public org.springframework.security.oauth2.server.authorization.client.RegisteredClient findByClientId(String clientId) {
        Optional<RegisteredClientEntity> entity = 
                registeredClientRepository.findByClientId(clientId);
        
        return entity.map(this::toRegisteredClient).orElse(null);
    }

    /**
     * 根据 ID 查询客户端
     */
    @Override
    public org.springframework.security.oauth2.server.authorization.client.RegisteredClient findById(String id) {
        // 注意：这里需要特殊处理，因为Spring Data JPA的findById期望Long类型
        // 但我们存储的是String类型的ID，所以需要先通过其他方式查找
        Optional<RegisteredClientEntity> entity = registeredClientRepository.findAll().stream()
                .filter(e -> e.getId().equals(id))
                .findFirst();
        
        return entity.map(this::toRegisteredClient).orElse(null);
    }

    /**
     * 保存客户端
     */
    @Transactional
    @Override
    public void save(org.springframework.security.oauth2.server.authorization.client.RegisteredClient registeredClient) {
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
        org.springframework.security.oauth2.server.authorization.client.RegisteredClient client = findByClientId(clientId);
        
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
        org.springframework.security.oauth2.server.authorization.client.RegisteredClient client = findByClientId(clientId);
        
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
        org.springframework.security.oauth2.server.authorization.client.RegisteredClient client = findByClientId(clientId);
        
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
        org.springframework.security.oauth2.server.authorization.client.RegisteredClient client = findByClientId(clientId);
        
        if (client == null) {
            return false;
        }
        
        // 检查所有请求的 scope 是否在客户端允许的范围内
        return client.getScopes().containsAll(requestedScopes);
    }

    /**
     * 转换为 RegisteredClient
     */
    private org.springframework.security.oauth2.server.authorization.client.RegisteredClient toRegisteredClient(RegisteredClientEntity entity) {
        return org.springframework.security.oauth2.server.authorization.client.RegisteredClient.withId(String.valueOf(entity.getId()))
                .clientId(entity.getClientId())
                .clientSecret(entity.getClientSecret())
                .clientName(entity.getClientName())
                .redirectUris(uris -> uris.addAll(
                    Arrays.asList(entity.getRedirectUris().split(","))
                ))
                .scopes(scopes -> scopes.addAll(
                    Arrays.asList(entity.getScopes().split(","))
                ))
                .authorizationGrantTypes(types -> types.addAll(
                    Arrays.stream(entity.getAuthorizationGrantTypes().split(","))
                        .map(AuthorizationGrantType::new)
                        .toList()
                ))
                .clientAuthenticationMethods(methods -> methods.addAll(
                    Arrays.stream(entity.getClientAuthenticationMethods().split(","))
                        .map(ClientAuthenticationMethod::new)
                        .toList()
                ))
                .clientSettings(ClientSettings.builder()
                    .requireAuthorizationConsent(true)
                    .requireProofKey(true)
                    .build())
                .tokenSettings(TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofHours(1))
                    .refreshTokenTimeToLive(Duration.ofDays(30))
                    .reuseRefreshTokens(false)
                    .build())
                .build();
    }

    /**
     * 转换为 Entity
     */
    private RegisteredClientEntity toEntity(org.springframework.security.oauth2.server.authorization.client.RegisteredClient client) {
        RegisteredClientEntity entity = new RegisteredClientEntity();
        entity.setId(Long.valueOf(client.getId()));
        entity.setClientId(client.getClientId());
        entity.setClientSecret(client.getClientSecret());
        entity.setClientName(client.getClientName());
        entity.setRedirectUris(String.join(",", client.getRedirectUris()));
        entity.setScopes(String.join(",", client.getScopes()));
        entity.setAuthorizationGrantTypes(
            client.getAuthorizationGrantTypes().stream()
                .map(AuthorizationGrantType::getValue)
                .collect(Collectors.joining(","))
        );
        entity.setClientAuthenticationMethods(
            client.getClientAuthenticationMethods().stream()
                .map(ClientAuthenticationMethod::getValue)
                .collect(Collectors.joining(","))
        );
        return entity;
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