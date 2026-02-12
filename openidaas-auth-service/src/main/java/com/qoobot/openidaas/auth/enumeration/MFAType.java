package com.qoobot.openidaas.auth.enumeration;

/**
 * MFA类型枚举
 *
 * @author QooBot
 */
public enum MFAType {

    /**
     * TOTP - 基于时间的一次性密码（如Google Authenticator）
     */
    TOTP("TOTP", "基于时间的一次性密码"),

    /**
     * SMS - 短信验证码
     */
    SMS("SMS", "短信验证码"),

    /**
     * EMAIL - 邮箱验证码
     */
    EMAIL("EMAIL", "邮箱验证码"),

    /**
     * BACKUP_CODE - 备用码
     */
    BACKUP_CODE("BACKUP_CODE", "备用码"),

    /**
     * HARDWARE_TOKEN - 硬件令牌
     */
    HARDWARE_TOKEN("HARDWARE_TOKEN", "硬件令牌"),

    /**
     * BIOMETRIC - 生物识别
     */
    BIOMETRIC("BIOMETRIC", "生物识别");

    private final String code;
    private final String description;

    MFAType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static MFAType fromCode(String code) {
        for (MFAType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown MFA type: " + code);
    }
}
