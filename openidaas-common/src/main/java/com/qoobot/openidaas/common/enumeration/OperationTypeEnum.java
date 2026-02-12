package com.qoobot.openidaas.common.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作类型枚举
 *
 * @author QooBot
 */
@Getter
@AllArgsConstructor
public enum OperationTypeEnum {
    CREATE("CREATE", "创建"),
    READ("READ", "查询"),
    UPDATE("UPDATE", "更新"),
    DELETE("DELETE", "删除"),
    EXPORT("EXPORT", "导出"),
    IMPORT("IMPORT", "导入"),
    APPROVE("APPROVE", "审批"),
    REJECT("REJECT", "拒绝"),
    LOGIN("LOGIN", "登录"),
    LOGOUT("LOGOUT", "退出");

    private final String code;
    private final String description;
}
