package com.qoobot.openidaas.user.entity;

/**
 * 账户类型枚举
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public enum AccountType {
    /**
     * 本地账户
     */
    LOCAL,

    /**
     * LDAP/AD账户
     */
    LDAP,

    /**
     * SSO账户
     */
    SSO,

    /**
     * OAuth2社交登录
     */
    OAUTH2,

    /**
     * 系统账户
     */
    SYSTEM
}
