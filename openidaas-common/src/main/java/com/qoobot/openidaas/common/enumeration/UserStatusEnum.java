package com.qoobot.openidaas.common.enumeration;

import lombok.Getter;

/**
 * 用户状态枚举
 *
 * @author QooBot
 */
@Getter
public enum UserStatusEnum {

    /**
     * 正常
     */
    ACTIVE(1, "正常"),
    
    /**
     * 正常（兼容旧版本）
     */
    NORMAL(1, "正常"),

    /**
     * 锁定
     */
    LOCKED(2, "锁定"),

    /**
     * 禁用
     */
    DISABLED(3, "禁用"),

    /**
     * 过期
     */
    EXPIRED(4, "过期");

    private final Integer code;
    private final String description;

    UserStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static UserStatusEnum fromCode(Integer code) {
        for (UserStatusEnum status : UserStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid user status code: " + code);
    }
}