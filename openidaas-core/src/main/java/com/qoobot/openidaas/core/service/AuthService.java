package com.qoobot.openidaas.core.service;

import com.qoobot.openidaas.core.dto.LoginRequest;
import com.qoobot.openidaas.core.dto.LoginResponse;
import com.qoobot.openidaas.core.entity.AuthToken;
import com.qoobot.openidaas.core.entity.User;

/**
 * 认证服务接口
 * 
 * 提供用户认证、令牌管理等功能
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public interface AuthService {
    
    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 登录响应，包含访问令牌和刷新令牌
     */
    LoginResponse login(LoginRequest request);
    
    /**
     * 刷新访问令牌
     * 
     * @param refreshToken 刷新令牌
     * @return 新的登录响应
     */
    LoginResponse refreshToken(String refreshToken);
    
    /**
     * 登出
     * 
     * @param accessToken 访问令牌
     */
    void logout(String accessToken);
    
    /**
     * 验证令牌
     * 
     * @param token 访问令牌
     * @return 是否有效
     */
    boolean validateToken(String token);
    
    /**
     * 获取当前用户
     * 
     * @param token 访问令牌
     * @return 用户信息
     */
    User getCurrentUser(String token);
    
    /**
     * 保存认证令牌
     * 
     * @param authToken 认证令牌实体
     */
    void saveAuthToken(AuthToken authToken);
    
    /**
     * 撤销令牌
     * 
     * @param tokenId 令牌ID
     */
    void revokeToken(Long tokenId);
    
    /**
     * 根据刷新令牌查找令牌
     * 
     * @param refreshToken 刷新令牌
     * @return 认证令牌实体
     */
    AuthToken findByRefreshToken(String refreshToken);
}
