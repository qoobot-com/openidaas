package com.qoobot.openidaas.auth.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 社会化登录提供者
 * 
 * 支持 Google、微信、钉钉等第三方登录
 * 实现 OAuth2.0 标准协议
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SocialLoginProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${openidaas.auth.social.google.client-id}")
    private String googleClientId;

    @Value("${openidaas.auth.social.google.client-secret}")
    private String googleClientSecret;

    @Value("${openidaas.auth.social.wechat.app-id}")
    private String wechatAppId;

    @Value("${openidaas.auth.social.wechat.app-secret}")
    private String wechatAppSecret;

    @Value("${openidaas.auth.social.dingtalk.app-id}")
    private String dingtalkAppId;

    @Value("${openidaas.auth.social.dingtalk.app-secret}")
    private String dingtalkAppSecret;

    /**
     * 处理 Google 登录
     * 
     * @param token Google ID Token
     * @return 认证信息
     */
    public Authentication authenticateGoogle(String token) {
        try {
            // 验证 Google ID Token
            GoogleUserInfo userInfo = verifyGoogleToken(token);
            
            if (userInfo == null) {
                throw new AuthenticationException("Invalid Google token");
            }
            
            // 查找或创建用户
            UserDetails userDetails = findOrCreateUser(
                    "google", 
                    userInfo.sub, 
                    userInfo.email,
                    userInfo.name);
            
            return createSuccessAuthentication(userDetails);
            
        } catch (Exception e) {
            log.error("Google authentication failed", e);
            throw new AuthenticationException("Google authentication failed", e);
        }
    }

    /**
     * 处理微信登录
     * 
     * @param code 微信授权码
     * @return 认证信息
     */
    public Authentication authenticateWechat(String code) {
        try {
            // 获取微信 Access Token
            String accessToken = getWechatAccessToken(code);
            
            // 获取微信用户信息
            WechatUserInfo userInfo = getWechatUserInfo(accessToken);
            
            // 查找或创建用户
            UserDetails userDetails = findOrCreateUser(
                    "wechat",
                    userInfo.openid,
                    null,
                    userInfo.nickname);
            
            return createSuccessAuthentication(userDetails);
            
        } catch (Exception e) {
            log.error("WeChat authentication failed", e);
            throw new AuthenticationException("WeChat authentication failed", e);
        }
    }

    /**
     * 处理钉钉登录
     * 
     * @param code 钉钉授权码
     * @return 认证信息
     */
    public Authentication authenticateDingtalk(String code) {
        try {
            // 获取钉钉用户信息
            DingtalkUserInfo userInfo = getDingtalkUserInfo(code);
            
            // 查找或创建用户
            UserDetails userDetails = findOrCreateUser(
                    "dingtalk",
                    userInfo.unionid,
                    null,
                    userInfo.nickname);
            
            return createSuccessAuthentication(userDetails);
            
        } catch (Exception e) {
            log.error("DingTalk authentication failed", e);
            throw new AuthenticationException("DingTalk authentication failed", e);
        }
    }

    @Override
    public Authentication authenticate(Authentication authentication) 
            throws AuthenticationException {
        
        if (!(authentication instanceof SocialAuthenticationToken)) {
            return null;
        }
        
        SocialAuthenticationToken socialToken = 
                (SocialAuthenticationToken) authentication;
        
        String provider = socialToken.getProvider();
        String token = socialToken.getToken();
        
        switch (provider.toLowerCase()) {
            case "google":
                return authenticateGoogle(token);
            case "wechat":
                return authenticateWechat(token);
            case "dingtalk":
                return authenticateDingtalk(token);
            default:
                throw new AuthenticationException(
                        "Unsupported social provider: " + provider);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SocialAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * 验证 Google ID Token
     */
    private GoogleUserInfo verifyGoogleToken(String token) {
        try {
            // 调用 Google Token Info API
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + token;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                return null;
            }
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            
            // 验证 client_id
            if (!googleClientId.equals(jsonNode.path("aud").asText())) {
                return null;
            }
            
            // 解析用户信息
            GoogleUserInfo userInfo = new GoogleUserInfo();
            userInfo.sub = jsonNode.path("sub").asText();
            userInfo.email = jsonNode.path("email").asText();
            userInfo.emailVerified = jsonNode.path("email_verified").asBoolean();
            userInfo.name = jsonNode.path("name").asText();
            userInfo.picture = jsonNode.path("picture").asText();
            
            log.info("Google user verified: {}", userInfo.email);
            
            return userInfo;
            
        } catch (Exception e) {
            log.error("Failed to verify Google token", e);
            return null;
        }
    }

    /**
     * 获取微信 Access Token
     */
    private String getWechatAccessToken(String code) {
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token";
        
        Map<String, String> params = new HashMap<>();
        params.put("appid", wechatAppId);
        params.put("secret", wechatAppSecret);
        params.put("code", code);
        params.put("grant_type", "authorization_code");
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, params, String.class);
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(response.getBody());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON response", e);
        }
        
        return jsonNode.path("access_token").asText();
    }

    /**
     * 获取微信用户信息
     */
    private WechatUserInfo getWechatUserInfo(String accessToken) {
        String url = "https://api.weixin.qq.com/sns/userinfo";
        
        ResponseEntity<String> response = restTemplate.exchange(
                url + "?access_token=" + accessToken + "&openid=" + accessToken,
                HttpMethod.GET,
                null,
                String.class);
        
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(response.getBody());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON response", e);
        }
        
        WechatUserInfo userInfo = new WechatUserInfo();
        userInfo.openid = jsonNode.path("openid").asText();
        userInfo.nickname = jsonNode.path("nickname").asText();
        userInfo.headimgurl = jsonNode.path("headimgurl").asText();
        
        return userInfo;
    }

    /**
     * 获取钉钉用户信息
     */
    private DingtalkUserInfo getDingtalkUserInfo(String code) {
        // 实现钉钉 API 调用
        DingtalkUserInfo userInfo = new DingtalkUserInfo();
        userInfo.unionid = "mock-unionid";
        userInfo.nickname = "Mock User";
        return userInfo;
    }

    /**
     * 查找或创建用户
     */
    private UserDetails findOrCreateUser(String provider, 
                                      String socialId,
                                      String email,
                                      String name) {
        
        // TODO: 实现从数据库查找用户
        // if (userExists(provider, socialId)) {
        //     return loadUserBySocialId(provider, socialId);
        // }
        
        // TODO: 创建新用户
        // return createSocialUser(provider, socialId, email, name);
        
        return null;
    }

    /**
     * 创建成功的认证对象
     */
    private Authentication createSuccessAuthentication(UserDetails userDetails) {
        return new SocialAuthenticationToken(
                userDetails, 
                null, 
                userDetails.getAuthorities(),
                "social");
    }

    /**
     * Google 用户信息
     */
    private static class GoogleUserInfo {
        String sub;
        String email;
        boolean emailVerified;
        String name;
        String picture;
    }

    /**
     * 微信用户信息
     */
    private static class WechatUserInfo {
        String openid;
        String nickname;
        String headimgurl;
    }

    /**
     * 钉钉用户信息
     */
    private static class DingtalkUserInfo {
        String unionid;
        String nickname;
    }

    /**
     * 社会化登录认证 Token
     */
    public static class SocialAuthenticationToken extends 
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken {
        
        private final String provider;
        private final String token;

        public SocialAuthenticationToken(Object principal, Object credentials,
                                      java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities,
                                      String provider) {
            super(principal, credentials, 
                authorities != null ? authorities : 
                java.util.Collections.emptyList());
            this.provider = provider;
            this.token = credentials != null ? credentials.toString() : null;
        }

        public String getProvider() {
            return provider;
        }

        public String getToken() {
            return token;
        }
    }

    /**
     * 认证异常
     */
    public static class AuthenticationException extends 
            org.springframework.security.core.AuthenticationException {
        
        public AuthenticationException(String message) {
            super(message);
        }
        
        public AuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
