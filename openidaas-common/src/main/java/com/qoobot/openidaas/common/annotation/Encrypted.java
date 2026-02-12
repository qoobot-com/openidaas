package com.qoobot.openidaas.common.annotation;

import java.lang.annotation.*;

/**
 * 敏感字段加密注解
 *
 * 使用在实体类字段上,标记该字段需要加密存储
 *
 * @author QooBot
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Encrypted {

    /**
     * 加密算法,默认使用配置中的算法
     */
    String algorithm() default "";

    /**
     * 是否启用加密,默认true
     */
    boolean enabled() default true;

    /**
     * 字段描述,用于日志记录(避免输出敏感信息)
     */
    String description() default "敏感信息";
}
