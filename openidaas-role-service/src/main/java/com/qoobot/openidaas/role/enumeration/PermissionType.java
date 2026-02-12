package com.qoobot.openidaas.role.enumeration;

/**
 * 权限类型枚举
 *
 * @author QooBot
 */
public enum PermissionType {

    /**
     * 菜单
     */
    MENU("menu", "菜单"),

    /**
     * 按钮
     */
    BUTTON("button", "按钮"),

    /**
     * API接口
     */
    API("api", "API接口");

    private final String code;
    private final String description;

    PermissionType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PermissionType fromCode(String code) {
        for (PermissionType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown permission type: " + code);
    }
}
