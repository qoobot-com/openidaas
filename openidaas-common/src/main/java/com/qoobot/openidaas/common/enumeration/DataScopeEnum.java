package com.qoobot.openidaas.common.enumeration;

import lombok.Getter;

/**
 * 数据权限范围枚举
 *
 * @author QooBot
 */
@Getter
public enum DataScopeEnum {

    /**
     * 全部数据权限
     */
    ALL(1, "全部数据"),

    /**
     * 本部门及以下数据权限
     */
    DEPT_AND_SUB(2, "本部门及以下"),

    /**
     * 本部门数据权限
     */
    DEPT_ONLY(3, "本部门"),

    /**
     * 本人数据权限
     */
    SELF(4, "本人"),

    /**
     * 自定义数据权限
     */
    CUSTOM(5, "自定义");

    private final Integer code;
    private final String description;

    DataScopeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据code获取枚举
     */
    public static DataScopeEnum fromCode(Integer code) {
        for (DataScopeEnum scope : DataScopeEnum.values()) {
            if (scope.getCode().equals(code)) {
                return scope;
            }
        }
        throw new IllegalArgumentException("Invalid data scope code: " + code);
    }
}