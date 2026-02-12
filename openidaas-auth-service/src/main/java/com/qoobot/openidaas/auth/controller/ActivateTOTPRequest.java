package com.qoobot.openidaas.auth.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 激活TOTP请求DTO
 */
@Data
public class ActivateTOTPRequest {

    @NotBlank(message = "密钥不能为空")
    private String secret;

    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码必须为6位数字")
    private String code;
}