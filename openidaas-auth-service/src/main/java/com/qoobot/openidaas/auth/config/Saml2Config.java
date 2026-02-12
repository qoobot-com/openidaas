package com.qoobot.openidaas.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.metadata.OpenSamlMetadataResolver;
import org.springframework.security.saml2.provider.service.registration.*;
import org.springframework.security.saml2.provider.service.web.Saml2MetadataFilter;
import org.springframework.security.saml2.provider.service.web.authentication.logout.OpenSaml5LogoutRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.OpenSaml5LogoutResponseResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutResponseResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Collection;
import java.util.Collections;

/**
 * SAML 2.0 配置
 * 用于企业级单点登录（SSO）
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(prefix = "app.saml2", name = "enabled", havingValue = "true")
public class Saml2Config {

    /**
     * SAML 2.0 登录配置
     */
    @Bean
    @Order(2)
    public SecurityFilterChain saml2SecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/saml2/**")
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/saml2/sso/**").permitAll()
                        .requestMatchers("/saml2/logout/**").permitAll()
                        .requestMatchers("/saml2/service-provider-metadata").permitAll()
                        .anyRequest().authenticated()
                )
                .saml2Login(saml2 -> saml2
                        .loginPage("/login")
                        .successHandler(saml2AuthenticationSuccessHandler())
                        .failureHandler(saml2AuthenticationFailureHandler())
                )
                .saml2Logout(saml2 -> saml2
                        .logoutUrl("/saml2/logout")
                        .logoutRequest((request) -> request
                                .logoutRequestResolver(saml2LogoutRequestResolver(relyingPartyRegistrationRepository()))
                        )
                        .logoutResponse((response) -> response
                                .logoutResponseResolver(saml2LogoutResponseResolver(relyingPartyRegistrationRepository()))
                        )
                )
                .saml2Metadata(metadata -> metadata
                        .metadataUrl("/saml2/service-provider-metadata")
                );

        return http.build();
    }

    /**
     * SAML 2.0 元数据过滤器
     */
    @Bean
    public Saml2MetadataFilter saml2MetadataFilter(
            RelyingPartyRegistrationRepository relyingPartyRegistrationRepository) {
        Saml2MetadataFilter filter = new Saml2MetadataFilter(
                relyingPartyRegistrationRepository,
                new OpenSamlMetadataResolver()
        );
        return filter;
    }

    /**
     * SAML 2.0 依赖方注册仓库
     * 配置与 Identity Provider (IdP) 的连接
     *
     * 注意：这是一个示例配置，实际使用时需要：
     * 1. 从配置文件读取 IdP 的元数据 URL
     * 2. 加载本地的签名和加密证书
     * 3. 配置正确的 entityId 和回调地址
     */
    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
        // 示例配置：连接到 ADFS IdP
        Saml2MessageBinding messageBinding = Saml2MessageBinding.POST;

        // TODO: 实际生产环境应从配置文件或密钥库加载凭证
        Collection<Saml2X509Credential> credentials = Collections.emptyList();

        RelyingPartyRegistration registration = RelyingPartyRegistration
                .withRegistrationId("saml2-sp")
                .entityId("https://openidaas.example.com/saml2/metadata")
                .assertionConsumerServiceLocation("http://localhost:8082/saml2/sso/{registrationId}")
                .assertionConsumerServiceBinding(messageBinding)
                .singleLogoutServiceLocation("http://localhost:8082/saml2/logout/{registrationId}")
                .singleLogoutServiceBinding(messageBinding)
                .signingX509Credentials(credentials1 -> credentials1.addAll(credentials))
                .build();

        return new InMemoryRelyingPartyRegistrationRepository(registration);
    }

    /**
     * SAML 2.0 认证成功处理器
     */
    @Bean
    public AuthenticationSuccessHandler saml2AuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            response.sendRedirect("/");
        };
    }

    /**
     * SAML 2.0 认证失败处理器
     */
    @Bean
    public AuthenticationFailureHandler saml2AuthenticationFailureHandler() {
        return (request, response, exception) -> {
            response.sendRedirect("/login?error=saml");
        };
    }

    /**
     * SAML 2.0 登出请求解析器
     */
    @Bean
    public Saml2LogoutRequestResolver saml2LogoutRequestResolver(
            RelyingPartyRegistrationRepository relyingPartyRegistrationRepository) {
        return new OpenSaml5LogoutRequestResolver(relyingPartyRegistrationRepository);
    }

    /**
     * SAML 2.0 登出响应解析器
     */
    @Bean
    public Saml2LogoutResponseResolver saml2LogoutResponseResolver(
            RelyingPartyRegistrationRepository relyingPartyRegistrationRepository) {
        return new OpenSaml5LogoutResponseResolver(relyingPartyRegistrationRepository);
    }
}