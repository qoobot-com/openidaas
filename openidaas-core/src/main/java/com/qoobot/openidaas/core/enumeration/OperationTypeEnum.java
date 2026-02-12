package com.qoobot.openidaas.core.enumeration;

import lombok.Getter;

/**
 * 审计操作类型枚举
 *
 * @author QooBot
 */
@Getter
public enum OperationTypeEnum {

    LOGIN("login", "登录"),
    LOGOUT("logout", "登出"),
    CREATE("create", "创建"),
    UPDATE("update", "更新"),
    DELETE("delete", "删除"),
    QUERY("query", "查询"),
    EXPORT("export", "导出"),
    IMPORT("import", "导入"),
    UPLOAD("upload", "上传"),
    DOWNLOAD("download", "下载");

    private final String code;
    private final String description;

    OperationTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static OperationTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (OperationTypeEnum type : OperationTypeEnum.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}