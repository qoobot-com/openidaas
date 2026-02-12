package com.qoobot.openidaas.common.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审计日志类型枚举
 *
 * @author QooBot
 */
@Getter
@AllArgsConstructor
public enum AuditLogTypeEnum {
    LOGIN(1, "登录日志", "LOGIN"),
    LOGOUT(2, "退出登录", "LOGOUT"),
    PASSWORD_CHANGE(3, "密码修改", "PASSWORD_CHANGE"),
    PERMISSION_CHANGE(4, "权限变更", "PERMISSION_CHANGE"),
    ROLE_CHANGE(5, "角色变更", "ROLE_CHANGE"),
    USER_CHANGE(6, "用户变更", "USER_CHANGE"),
    DATA_ACCESS(7, "数据访问", "DATA_ACCESS"),
    DATA_MODIFY(8, "数据修改", "DATA_MODIFY"),
    DATA_DELETE(9, "数据删除", "DATA_DELETE"),
    SYSTEM_CONFIG(10, "系统配置", "SYSTEM_CONFIG"),
    EXPORT_DATA(11, "数据导出", "EXPORT_DATA"),
    IMPORT_DATA(12, "数据导入", "IMPORT_DATA"),
    API_ACCESS(13, "API访问", "API_ACCESS");

    private final Integer code;
    private final String description;
    private final String value;
}
