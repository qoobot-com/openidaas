package com.qoobot.openidaas.auth.service;

import com.qoobot.openidaas.core.entity.User;
import com.qoobot.openidaas.core.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * OIDC 用户信息服务
 * 
 * 用于加载用户的 OIDC 信息（如 email, picture, preferred_username 等）
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OidcUserInfoService {

    private final UserService userService;

    /**
     * 根据用户名加载 OIDC 用户信息
     * 
     * @param username 用户名
     * @return OIDC 用户信息
     */
    public OidcUserInfo loadUser(String username) {
        try {
            Optional<User> userOpt = userService.findUserByUsername(username);
            
            if (userOpt.isEmpty()) {
                log.warn("User not found: {}", username);
                return createEmptyUserInfo(username);
            }
            
            User user = userOpt.get();
            return createUserInfo(user);
            
        } catch (Exception e) {
            log.error("Failed to load OIDC user info for: {}", username, e);
            return createEmptyUserInfo(username);
        }
    }

    /**
     * 根据邮箱加载 OIDC 用户信息
     * 
     * @param email 邮箱
     * @return OIDC 用户信息
     */
    public OidcUserInfo loadUserByEmail(String email) {
        try {
            Optional<User> userOpt = userService.findUserByEmail(email);
            
            if (userOpt.isEmpty()) {
                log.warn("User not found by email: {}", email);
                return createEmptyUserInfo(email);
            }
            
            User user = userOpt.get();
            return createUserInfo(user);
            
        } catch (Exception e) {
            log.error("Failed to load OIDC user info for email: {}", email, e);
            return createEmptyUserInfo(email);
        }
    }

    /**
     * 创建用户 OIDC 信息
     */
    private OidcUserInfo createUserInfo(User user) {
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("sub", user.getUsername());
        claims.put("name", user.getNickname() != null ? user.getNickname() : user.getUsername());
        claims.put("given_name", user.getNickname());
        claims.put("family_name", "");
        claims.put("middle_name", "");
        claims.put("nickname", user.getUsername());
        claims.put("preferred_username", user.getUsername());
        claims.put("profile", "http://localhost:8081/users/" + user.getId());
        claims.put("picture", user.getAvatarUrl());
        claims.put("website", "");
        claims.put("email", user.getEmail());
        claims.put("email_verified", "true");
        claims.put("gender", "");
        claims.put("birthdate", "");
        claims.put("zoneinfo", "Asia/Shanghai");
        claims.put("locale", "zh-CN");
        claims.put("phone_number", user.getPhone());
        claims.put("phone_number_verified", String.valueOf(user.getPhone() != null));
        claims.put("address", "");
        claims.put("updated_at", String.valueOf(System.currentTimeMillis()));
        
        return new OidcUserInfo(claims);
    }

    /**
     * 创建空的用户信息（当用户不存在时）
     */
    private OidcUserInfo createEmptyUserInfo(String identifier) {
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("sub", identifier);
        claims.put("name", identifier);
        claims.put("preferred_username", identifier);
        claims.put("email_verified", "false");
        claims.put("phone_number_verified", "false");
        
        return new OidcUserInfo(claims);
    }
}