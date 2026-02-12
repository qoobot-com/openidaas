package com.qoobot.openidaas.core.enumeration;

import lombok.Getter;

/**
 * 菜单类型枚举
 *
 * @author QooBot
 */
@Getter
public enum MenuTypeEnum {

    DIRECTORY("directory", "目录"),
    MENU("menu", "菜单"),
    BUTTON("button", "按钮");

    private final String code;
    private final String description;

    MenuTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static MenuTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (MenuTypeEnum type : MenuTypeEnum.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}