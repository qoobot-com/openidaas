package com.qoobot.openidaas.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存中的 RegisteredClient Repository
 * 
 * 用于开发和测试环境
 * 生产环境应使用数据库实现
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class RegisteredClientRepositoryService implements RegisteredClientRepository {

    private final Map<String, RegisteredClient> clients = new ConcurrentHashMap<>();

    public RegisteredClientRepositoryService() {
        // 初始化默认客户端
        RegisteredClient defaultClient = RegisteredClient.withId("openidaas-client")
                .clientId("openidaas-client")
                .clientSecret("{noop}openidaas-secret")
                .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS)
                .redirectUri("http://localhost:8084/login/oauth2/code/openidaas-client")
                .redirectUri("http://localhost:8084/authorized")
                .scope(org.springframework.security.oauth2.core.oidc.OidcScopes.OPENID)
                .scope(org.springframework.security.oauth2.core.oidc.OidcScopes.PROFILE)
                .scope(org.springframework.security.oauth2.core.oidc.OidcScopes.EMAIL)
                .clientSettings(org.springframework.security.oauth2.server.authorization.settings.ClientSettings.builder()
                        .requireAuthorizationConsent(true)
                        .requireProofKey(true)
                        .build())
                .build();
        
        clients.put(defaultClient.getClientId(), defaultClient);
        log.info("Registered default OAuth2 client: {}", defaultClient.getClientId());
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return clients.get(clientId);
    }

    @Override
    public RegisteredClient findById(String id) {
        return clients.values().stream()
                .filter(client -> client.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        clients.put(registeredClient.getClientId(), registeredClient);
        log.info("Saved OAuth2 client: {}", registeredClient.getClientId());
    }
}
