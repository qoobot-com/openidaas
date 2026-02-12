package com.qoobot.openidaas.common.enumeration;

import lombok.Getter;

/**
 * 性别枚举
 *
 * @author QooBot
 */
@Getter
public enum GenderEnum {

    MALE(1, "男"),
    FEMALE(2, "女"),
    UNKNOWN(0, "未知");

    private final Integer code;
    private final String description;

    GenderEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }

    public static GenderEnum fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (GenderEnum gender : GenderEnum.values()) {
            if (gender.getCode().equals(code)) {
                return gender;
            }
        }
        return UNKNOWN;
    }
}