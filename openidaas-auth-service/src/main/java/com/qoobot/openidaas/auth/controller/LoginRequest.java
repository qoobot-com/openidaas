package com.qoobot.openidaas.auth.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @Size(min = 6, max = 6, message = "MFA验证码必须为6位")
    private String mfaCode;

    private String clientIp;
}