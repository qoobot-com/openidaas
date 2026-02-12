package com.qoobot.openidaas.core.enumeration;

import lombok.Getter;

/**
 * 应用状态枚举
 *
 * @author QooBot
 */
@Getter
public enum AppStatusEnum {

    PENDING("pending", "待审核"),
    APPROVED("approved", "已审核"),
    REJECTED("rejected", "已拒绝"),
    ONLINE("online", "已上线"),
    OFFLINE("offline", "已下线");

    private final String code;
    private final String description;

    AppStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static AppStatusEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (AppStatusEnum status : AppStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}