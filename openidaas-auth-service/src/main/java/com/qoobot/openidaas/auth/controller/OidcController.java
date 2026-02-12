package com.qoobot.openidaas.auth.controller;

import com.qoobot.openidaas.common.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * OpenID Connect 控制器
 * 处理 OIDC 相关请求
 */
@Slf4j
@RestController
@RequestMapping("/oidc")
public class OidcController {

    /**
     * 获取用户信息端点
     * OAuth 2.0 UserInfo Endpoint
     */
    @GetMapping("/userinfo")
    public ResultVO<Map<String, Object>> getUserInfo(Principal principal) {
        log.info("UserInfo request for principal: {}", principal.getName());

        Map<String, Object> userInfo = new HashMap<>();

        if (principal instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) principal;

            userInfo.put("sub", oidcUser.getSubject());
            userInfo.put("name", oidcUser.getFullName());
            userInfo.put("given_name", oidcUser.getGivenName());
            userInfo.put("family_name", oidcUser.getFamilyName());
            userInfo.put("email", oidcUser.getEmail());
            userInfo.put("email_verified", oidcUser.getEmailVerified());
            userInfo.put("picture", oidcUser.getPicture());
            userInfo.put("preferred_username", oidcUser.getPreferredUsername());

            // ID Token
            OidcIdToken idToken = oidcUser.getIdToken();
            userInfo.put("id_token", idToken.getTokenValue());
            userInfo.put("id_token_expires_at", idToken.getExpiresAt());

        } else {
            // 非 OIDC 用户，返回基本信息
            userInfo.put("sub", principal.getName());
            userInfo.put("name", principal.getName());
            userInfo.put("preferred_username", principal.getName());
        }

        return ResultVO.success(userInfo);
    }

    /**
     * 获取 ID Token 端点
     */
    @GetMapping("/id_token")
    public ResultVO<Map<String, Object>> getIdToken(Authentication authentication) {
        log.info("ID Token request for user: {}", authentication.getName());

        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("iss", "http://localhost:8082");
        tokenInfo.put("sub", authentication.getName());
        tokenInfo.put("aud", "admin-client");
        tokenInfo.put("exp", System.currentTimeMillis() / 1000 + 3600);
        tokenInfo.put("iat", System.currentTimeMillis() / 1000);

        return ResultVO.success(tokenInfo);
    }

    /**
     * 验证 ID Token
     */
    @PostMapping("/validate_id_token")
    public ResultVO<Map<String, Object>> validateIdToken(@RequestBody Map<String, String> request) {
        String idToken = request.get("id_token");

        log.info("Validate ID Token request");

        // TODO: 实现 ID Token 验证逻辑
        // 1. 验证签名
        // 2. 验证 issuer
        // 3. 验证 audience
        // 4. 验证过期时间
        // 5. 验证 issued time

        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        result.put("user_id", "user123");
        result.put("email", "user@example.com");

        return ResultVO.success(result);
    }

    /**
     * 发现端点
     * OpenID Connect Discovery 1.0
     */
    @GetMapping("/.well-known/openid-configuration")
    public ResultVO<Map<String, Object>> getOpenIdConfiguration() {
        log.info("OpenID Connect Discovery request");

        Map<String, Object> config = new HashMap<>();

        // Issuer
        config.put("issuer", "http://localhost:8082");

        // Authorization Endpoint
        config.put("authorization_endpoint", "http://localhost:8082/oauth2/authorize");

        // Token Endpoint
        config.put("token_endpoint", "http://localhost:8082/oauth2/token");

        // UserInfo Endpoint
        config.put("userinfo_endpoint", "http://localhost:8082/oidc/userinfo");

        // JWK Set Endpoint
        config.put("jwks_uri", "http://localhost:8082/.well-known/jwks.json");

        // Registration Endpoint
        config.put("registration_endpoint", "http://localhost:8082/connect/register");

        // End Session Endpoint (Logout)
        config.put("end_session_endpoint", "http://localhost:8082/oauth2/logout");

        // Response Types Supported
        config.put("response_types_supported", new String[]{"code", "token", "id_token"});

        // Response Modes Supported
        config.put("response_modes_supported", new String[]{"query", "fragment", "form_post"});

        // Grant Types Supported
        config.put("grant_types_supported", new String[]{"authorization_code", "implicit", "refresh_token", "client_credentials", "password"});

        // Subject Types Supported
        config.put("subject_types_supported", new String[]{"public"});

        // ID Token Signing Algorithm Values
        config.put("id_token_signing_alg_values_supported", new String[]{"RS256"});

        // Scopes Supported
        config.put("scopes_supported", new String[]{"openid", "profile", "email", "phone", "address"});

        // Token Types Supported
        config.put("token_types_supported", new String[]{"Bearer"});

        // Claims Supported
        config.put("claims_supported", new String[]{"sub", "name", "given_name", "family_name",
                "middle_name", "nickname", "preferred_username", "profile", "picture", "website",
                "email", "email_verified", "gender", "birthdate", "zoneinfo", "locale", "phone_number",
                "phone_number_verified", "address", "updated_at"});

        // Code Challenge Methods Supported (PKCE)
        config.put("code_challenge_methods_supported", new String[]{"plain", "S256"});

        // Authentication Methods Supported
        config.put("token_endpoint_auth_methods_supported", new String[]{"client_secret_basic", "client_secret_post", "none"});

        // Claims Parameter Supported
        config.put("claims_parameter_supported", true);

        // Request Parameter Supported
        config.put("request_parameter_supported", true);

        // Request URI Parameter Supported
        config.put("request_uri_parameter_supported", true);

        // Require Request URI Registration
        config.put("require_request_uri_registration", false);

        // Front Channel Logout Supported
        config.put("frontchannel_logout_supported", true);

        // Front Channel Logout Session Supported
        config.put("frontchannel_logout_session_supported", true);

        // Back Channel Logout Supported
        config.put("backchannel_logout_supported", false);

        return ResultVO.success(config);
    }
}
