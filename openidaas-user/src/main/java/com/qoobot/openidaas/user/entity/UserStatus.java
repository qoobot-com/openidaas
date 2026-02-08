package com.qoobot.openidaas.user.entity;

/**
 * 用户状态枚举
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public enum UserStatus {
    /**
     * 活跃
     */
    ACTIVE,

    /**
     * 未激活
     */
    INACTIVE,

    /**
     * 已锁定
     */
    LOCKED,

    /**
     * 已停用
     */
    DISABLED,

    /**
     * 已挂起
     */
    SUSPENDED,

    /**
     * 待审核
     */
    PENDING,

    /**
     * 已拒绝
     */
    REJECTED
}
