package com.qoobot.openidaas.common.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OAuth2 授权类型枚举
 */
@Getter
@AllArgsConstructor
public enum GrantTypeEnum {

    AUTHORIZATION_CODE("authorization_code", "授权码模式"),
    IMPLICIT("implicit", "隐式模式"),
    REFRESH_TOKEN("refresh_token", "刷新令牌"),
    CLIENT_CREDENTIALS("client_credentials", "客户端凭证模式"),
    PASSWORD("password", "密码模式"),
    JWT_BEARER("urn:ietf:params:oauth:grant-type:jwt-bearer", "JWT承载模式");

    private final String code;
    private final String description;

    public static GrantTypeEnum fromCode(String code) {
        if (code == null) {
            return AUTHORIZATION_CODE;
        }
        for (GrantTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return AUTHORIZATION_CODE;
    }
}
