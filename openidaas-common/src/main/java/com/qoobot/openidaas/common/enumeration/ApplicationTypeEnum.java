package com.qoobot.openidaas.common.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 应用类型枚举
 */
@Getter
@AllArgsConstructor
public enum ApplicationTypeEnum {

    WEB(1, "Web应用"),
    MOBILE(2, "移动应用"),
    API(3, "API应用"),
    DESKTOP(4, "桌面应用"),
    SERVICE(5, "服务应用");

    private final Integer code;
    private final String description;

    public static ApplicationTypeEnum fromCode(Integer code) {
        if (code == null) {
            return WEB;
        }
        for (ApplicationTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return WEB;
    }
}
