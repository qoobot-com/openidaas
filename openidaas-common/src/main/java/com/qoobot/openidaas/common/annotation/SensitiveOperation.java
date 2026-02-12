package com.qoobot.openidaas.common.annotation;

import java.lang.annotation.*;

/**
 * 敏感操作注解
 * 标记需要进行安全审计的敏感操作
 *
 * @author QooBot
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SensitiveOperation {

    /**
     * 操作描述
     */
    String value() default "";

    /**
     * 操作类型
     */
    OperationType type() default OperationType.UPDATE;

    /**
     * 敏感级别
     */
    SensitivityLevel level() default SensitivityLevel.MEDIUM;

    /**
     * 是否记录请求参数
     */
    boolean logRequest() default true;

    /**
     * 是否记录响应数据
     */
    boolean logResponse() default false;

    /**
     * 操作类型枚举
     */
    enum OperationType {
        /**
         * 查询
         */
        READ,

        /**
         * 创建
         */
        CREATE,

        /**
         * 更新
         */
        UPDATE,

        /**
         * 删除
         */
        DELETE,

        /**
         * 登录
         */
        LOGIN,

        /**
         * 登出
         */
        LOGOUT,

        /**
         * 密码修改
         */
        PASSWORD_CHANGE,

        /**
         * 权限变更
         */
        PERMISSION_CHANGE,

        /**
         * 角色分配
         */
        ROLE_ASSIGN,

        /**
         * 数据导出
         */
        EXPORT,

        /**
         * 系统配置
         */
        CONFIG
    }

    /**
     * 敏感级别枚举
     */
    enum SensitivityLevel {
        /**
         * 低敏感度
         */
        LOW,

        /**
         * 中敏感度
         */
        MEDIUM,

        /**
         * 高敏感度
         */
        HIGH,

        /**
         * 极高敏感度
         */
        CRITICAL
    }
}
