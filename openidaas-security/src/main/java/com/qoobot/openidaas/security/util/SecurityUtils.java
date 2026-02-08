package com.qoobot.openidaas.security.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * 安全工具类
 * 
 * 提供安全相关的工具方法
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public class SecurityUtils {
    
    /**
     * 获取当前认证信息
     */
    public static Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }
    
    /**
     * 获取当前用户名
     */
    public static Optional<String> getCurrentUsername() {
        return getAuthentication()
                .map(Authentication::getName);
    }
    
    /**
     * 检查当前用户是否认证
     */
    public static boolean isAuthenticated() {
        return getAuthentication()
                .map(Authentication::isAuthenticated)
                .orElse(false);
    }
    
    /**
     * 检查当前用户是否拥有指定角色
     */
    public static boolean hasRole(String role) {
        return getAuthentication()
                .map(auth -> auth.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role)))
                .orElse(false);
    }
    
    /**
     * 检查当前用户是否拥有指定权限
     */
    public static boolean hasAuthority(String authority) {
        return getAuthentication()
                .map(auth -> auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals(authority)))
                .orElse(false);
    }
    
    /**
     * 清除认证信息
     */
    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
    
    private SecurityUtils() {
        throw new UnsupportedOperationException("Utility class");
    }
}
