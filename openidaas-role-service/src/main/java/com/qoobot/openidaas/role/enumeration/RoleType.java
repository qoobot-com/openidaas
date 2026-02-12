package com.qoobot.openidaas.role.enumeration;

/**
 * 角色类型枚举
 *
 * @author QooBot
 */
public enum RoleType {

    /**
     * 系统角色
     */
    SYSTEM(1, "系统角色"),

    /**
     * 自定义角色
     */
    CUSTOM(2, "自定义角色");

    private final int code;
    private final String description;

    RoleType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static RoleType fromCode(int code) {
        for (RoleType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown role type: " + code);
    }
}
