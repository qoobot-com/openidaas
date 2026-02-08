package com.qoobot.openidaas.user.entity;

/**
 * 数据范围枚举
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public enum DataScope {
    /**
     * 全部数据
     */
    ALL,

    /**
     * 自定义数据
     */
    CUSTOM,

    /**
     * 本部门及以下
     */
    DEPT_AND_CHILD,

    /**
     * 仅本部门
     */
    DEPT_ONLY,

    /**
     * 仅本人
     */
    SELF
}
