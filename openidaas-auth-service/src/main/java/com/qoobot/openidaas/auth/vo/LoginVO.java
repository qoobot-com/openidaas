package com.qoobot.openidaas.auth.vo;

import lombok.Data;

/**
 * 登录响应VO
 */
@Data
public class LoginVO {

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private String tokenType;
    private UserInfo userInfo;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
    }
}