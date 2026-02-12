package com.qoobot.openidaas.common.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审计结果枚举
 *
 * @author QooBot
 */
@Getter
@AllArgsConstructor
public enum AuditResultEnum {
    SUCCESS("SUCCESS", "成功"),
    FAILURE("FAILURE", "失败"),
    PARTIAL("PARTIAL", "部分成功");

    private final String code;
    private final String description;
}
