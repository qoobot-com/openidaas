package com.qoobot.openidaas.common.enumeration;

import lombok.Getter;

/**
 * 角色类型枚举
 *
 * @author QooBot
 */
@Getter
public enum RoleTypeEnum {

    /**
     * 系统角色
     */
    SYSTEM(1, "系统角色"),

    /**
     * 自定义角色
     */
    CUSTOM(2, "自定义角色"),

    /**
     * 默认角色
     */
    DEFAULT(3, "默认角色");

    private final Integer code;
    private final String description;

    RoleTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static RoleTypeEnum fromCode(Integer code) {
        for (RoleTypeEnum type : RoleTypeEnum.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid role type code: " + code);
    }
}