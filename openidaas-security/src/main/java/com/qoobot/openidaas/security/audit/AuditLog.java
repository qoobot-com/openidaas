package com.qoobot.openidaas.security.audit;

import java.lang.annotation.*;

/**
 * 审计日志注解
 * 
 * 标记需要记录审计日志的方法
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * 操作名称
     */
    String operation() default "";

    /**
     * 模块名称
     */
    String module() default "";

    /**
     * 操作描述
     */
    String description() default "";

    /**
     * 是否记录参数
     */
    boolean logParams() default false;

    /**
     * 是否记录结果
     */
    boolean logResult() default false;

    /**
     * 风险级别
     */
    RiskLevel riskLevel() default RiskLevel.LOW;

    /**
     * 风险级别枚举
     */
    enum RiskLevel {
        /**
         * 低风险
         */
        LOW,
        
        /**
         * 中风险
         */
        MEDIUM,
        
        /**
         * 高风险
         */
        HIGH,
        
        /**
         * 严重风险
         */
        CRITICAL
    }
}
