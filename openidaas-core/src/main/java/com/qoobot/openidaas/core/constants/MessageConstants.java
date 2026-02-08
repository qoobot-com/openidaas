package com.qoobot.openidaas.core.constants;

/**
 * 消息常量
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public class MessageConstants {
    
    // 成功消息
    public static final String SUCCESS = "操作成功";
    public static final String LOGIN_SUCCESS = "登录成功";
    public static final String LOGOUT_SUCCESS = "登出成功";
    public static final String USER_CREATED = "用户创建成功";
    public static final String USER_UPDATED = "用户更新成功";
    public static final String USER_DELETED = "用户删除成功";
    public static final String TENANT_CREATED = "租户创建成功";
    public static final String TENANT_UPDATED = "租户更新成功";
    public static final String TENANT_DELETED = "租户删除成功";
    public static final String PASSWORD_CHANGED = "密码修改成功";
    public static final String PASSWORD_RESET = "密码重置成功";
    
    // 错误消息
    public static final String ERROR = "操作失败";
    public static final String LOGIN_FAILED = "登录失败";
    public static final String INVALID_CREDENTIALS = "用户名或密码错误";
    public static final String USER_NOT_FOUND = "用户不存在";
    public static final String TENANT_NOT_FOUND = "租户不存在";
    public static final String USER_ALREADY_EXISTS = "用户已存在";
    public static final String TENANT_ALREADY_EXISTS = "租户已存在";
    public static final String INVALID_TOKEN = "令牌无效或已过期";
    public static final String TOKEN_EXPIRED = "令牌已过期";
    public static final String ACCESS_DENIED = "访问被拒绝";
    public static final String ACCOUNT_LOCKED = "账户已被锁定";
    public static final String ACCOUNT_DISABLED = "账户已被禁用";
    public static final String PASSWORD_MISMATCH = "旧密码不正确";
    public static final String WEAK_PASSWORD = "密码强度不足";
    public static final String EMAIL_ALREADY_EXISTS = "邮箱已被使用";
    public static final String USERNAME_ALREADY_EXISTS = "用户名已被使用";
    public static final String TENANT_USER_LIMIT_REACHED = "租户用户数量已达上限";
    public static final String TENANT_EXPIRED = "租户已过期";
    public static final String TENANT_SUSPENDED = "租户已被暂停";
    
    private MessageConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
