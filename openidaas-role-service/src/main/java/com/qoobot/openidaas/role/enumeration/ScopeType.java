package com.qoobot.openidaas.role.enumeration;

/**
 * 授权作用域类型枚举
 *
 * @author QooBot
 */
public enum ScopeType {

    /**
     * 全局
     */
    GLOBAL(1, "全局"),

    /**
     * 部门
     */
    DEPARTMENT(2, "部门"),

    /**
     * 项目
     */
    PROJECT(3, "项目"),

    /**
     * 应用
     */
    APPLICATION(4, "应用");

    private final int code;
    private final String description;

    ScopeType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ScopeType fromCode(int code) {
        for (ScopeType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown scope type: " + code);
    }
}
