package com.qoobot.openidaas.auth.service;

import com.qoobot.openidaas.core.entity.User;
import com.qoobot.openidaas.core.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 用户详情服务
 * 
 * 实现 Spring Security 的 UserDetailsService 接口
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) 
            throws UsernameNotFoundException {
        
        User user = userService.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));
        
        if (!user.getEnabled()) {
            throw new UsernameNotFoundException("User is disabled: " + username);
        }
        
        if (user.getAccountNonLocked()) {
            throw new UsernameNotFoundException("User account is locked: " + username);
        }
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.emptyList()) // TODO: 加载用户权限
                .accountExpired(!user.getAccountNonExpired())
                .accountLocked(!user.getAccountNonLocked())
                .credentialsExpired(!user.getCredentialsNonExpired())
                .disabled(!user.getEnabled())
                .build();
    }

    /**
     * 加载 OIDC 用户信息
     * 
     * @param username 用户名
     * @return OIDC 用户信息
     */
    public OidcUserInfo loadUser(String username) {
        User user = userService.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));
        
        return OidcUserInfo.builder()
                .sub(user.getId().toString())
                .email(user.getEmail())
                .emailVerified(true)
                .name(user.getNickname())
                .givenName(user.getNickname())
                .familyName("")
                .preferredUsername(user.getUsername())
                .picture(user.getAvatarUrl())
                .build();
    }

    /**
     * OIDC 用户信息
     */
    @lombok.Data
    @lombok.Builder
    public static class OidcUserInfo {
        private String sub;
        private String email;
        private Boolean emailVerified;
        private String name;
        private String givenName;
        private String familyName;
        private String preferredUsername;
        private String picture;
    }
}
