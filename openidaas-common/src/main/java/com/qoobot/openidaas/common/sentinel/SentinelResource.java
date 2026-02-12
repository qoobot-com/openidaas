package com.qoobot.openidaas.common.sentinel;

import java.lang.annotation.*;

/**
 * Sentinel 资源注解
 * 用于标记需要进行流控和降级的方法
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SentinelResource {

    /**
     * 资源名称
     */
    String value() default "";

    /**
     * 资源类型
     */
    ResourceType resourceType() default ResourceType.COMMON;

    /**
     * 流控处理类
     */
    String blockHandler() default "";

    /**
     * 流控处理方法
     */
    String blockHandlerMethod() default "";

    /**
     * 降级处理类
     */
    String fallback() default "";

    /**
     * 降级处理方法
     */
    String fallbackMethod() default "";

    /**
     * 资源类型枚举
     */
    enum ResourceType {
        COMMON,      // 普通资源
        GATEWAY,     // 网关资源
        API,         // API 资源
        DATABASE,    // 数据库资源
        CACHE,       // 缓存资源
        EXTERNAL     // 外部服务资源
    }
}
