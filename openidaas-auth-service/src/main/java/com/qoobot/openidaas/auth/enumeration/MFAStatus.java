package com.qoobot.openidaas.auth.enumeration;

/**
 * MFA状态枚举
 *
 * @author QooBot
 */
public enum MFAStatus {

    /**
     * 已配置
     */
    ACTIVE(1, "已配置"),

    /**
     * 待验证
     */
    PENDING(2, "待验证"),

    /**
     * 已禁用
     */
    DISABLED(3, "已禁用"),

    /**
     * 已删除
     */
    DELETED(4, "已删除");

    private final int code;
    private final String description;

    MFAStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static MFAStatus fromCode(int code) {
        for (MFAStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown MFA status code: " + code);
    }
}
