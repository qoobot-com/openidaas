package com.qoobot.openidaas.user.entity;

/**
 * 角色范围枚举
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public enum RoleScope {
    /**
     * 全局
     */
    GLOBAL,

    /**
     * 租户级别
     */
    TENANT,

    /**
     * 部门级别
     */
    DEPARTMENT,

    /**
     * 个人
     */
    PERSONAL
}
