package com.qoobot.openidaas.core.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
public class LoginRequest {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    private String tenantCode;
    
    private String deviceId;
    
    private String deviceType;
}
