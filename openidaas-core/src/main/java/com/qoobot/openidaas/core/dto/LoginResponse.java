package com.qoobot.openidaas.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    /**
     * 访问令牌
     */
    private String accessToken;
    
    /**
     * 访问令牌类型
     */
    private String tokenType;
    
    /**
     * 访问令牌过期时间（秒）
     */
    private Long expiresIn;
    
    /**
     * 刷新令牌
     */
    private String refreshToken;
    
    /**
     * 刷新令牌过期时间（秒）
     */
    private Long refreshTokenExpiresIn;
    
    /**
     * 用户信息
     */
    private UserDTO user;
    
    /**
     * 租户信息
     */
    private TenantDTO tenant;
}
