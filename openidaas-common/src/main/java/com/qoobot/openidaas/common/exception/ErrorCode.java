package com.qoobot.openidaas.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 *
 * @author QooBot
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 系统级别错误 (1000-1999)
    SYSTEM_ERROR("1000", "系统内部错误"),
    PARAMETER_ERROR("1001", "参数错误"),
    UNAUTHORIZED("1002", "未授权访问"),
    FORBIDDEN("1003", "禁止访问"),
    NOT_FOUND("1004", "资源不存在"),
    INTERNAL_SERVER_ERROR("1005", "服务器内部错误"),

    // 认证相关错误 (2000-2999)
    AUTHENTICATION_FAILED("2000", "认证失败"),
    USERNAME_PASSWORD_ERROR("2001", "用户名或密码错误"),
    ACCOUNT_LOCKED("2002", "账户已被锁定"),
    ACCOUNT_DISABLED("2003", "账户已被禁用"),
    TOKEN_EXPIRED("2004", "令牌已过期"),
    INVALID_TOKEN("2005", "无效令牌"),
    REFRESH_TOKEN_INVALID("2006", "刷新令牌无效"),

    // 用户相关错误 (3000-3999)
    USER_NOT_FOUND("3000", "用户不存在"),
    USER_ALREADY_EXISTS("3001", "用户已存在"),
    EMAIL_ALREADY_EXISTS("3002", "邮箱已被使用"),
    MOBILE_ALREADY_EXISTS("3003", "手机号已被使用"),
    PASSWORD_WEAK("3004", "密码强度不足"),
    OLD_PASSWORD_ERROR("3005", "原密码错误"),

    // 角色相关错误 (4000-4999)
    ROLE_NOT_FOUND("4000", "角色不存在"),
    ROLE_ALREADY_EXISTS("4001", "角色已存在"),
    ROLE_IN_USE("4002", "角色正在使用中，无法删除"),

    // 权限相关错误 (5000-5999)
    PERMISSION_DENIED("5000", "权限不足"),
    PERMISSION_NOT_FOUND("5001", "权限不存在"),

    // 组织机构相关错误 (6000-6999)
    ORG_NOT_FOUND("6000", "组织机构不存在"),
    ORG_ALREADY_EXISTS("6001", "组织机构已存在"),
    ORG_HAS_CHILDREN("6002", "组织机构下有子节点，无法删除"),

    // 应用相关错误 (7000-7999)
    APP_NOT_FOUND("7000", "应用不存在"),
    APP_ALREADY_EXISTS("7001", "应用已存在"),
    INVALID_CLIENT_CREDENTIALS("7002", "无效的客户端凭证"),

    // 数据验证相关错误 (8000-8999)
    DATA_VALIDATION_ERROR("8000", "数据验证失败"),
    ILLEGAL_ARGUMENT("8001", "非法参数");

    private final String code;
    private final String message;

    /**
     * 根据code获取错误码
     */
    public static ErrorCode fromCode(String code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return SYSTEM_ERROR;
    }
}