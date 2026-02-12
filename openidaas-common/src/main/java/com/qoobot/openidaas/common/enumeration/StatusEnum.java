package com.qoobot.openidaas.common.enumeration;

import lombok.Getter;

/**
 * 数据状态枚举
 *
 * @author QooBot
 */
@Getter
public enum StatusEnum {

    ENABLED(1, "启用"),
    DISABLED(0, "禁用"),
    DELETED(-1, "已删除");

    private final Integer code;
    private final String description;

    StatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }

    public static StatusEnum fromCode(Integer code) {
        if (code == null) {
            return DISABLED;
        }
        for (StatusEnum status : StatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return DISABLED;
    }

    public boolean isEnabled() {
        return this == ENABLED;
    }

    public boolean isDisabled() {
        return this == DISABLED;
    }

    public boolean isDeleted() {
        return this == DELETED;
    }
}