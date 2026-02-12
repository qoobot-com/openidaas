package com.qoobot.openidaas.common.annotation;

import java.lang.annotation.*;

/**
 * API 废弃注解
 * 用于标记已废弃的 API
 *
 * @author QooBot
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DeprecatedApi {

    /**
     * 废弃日期（格式: yyyy-MM-dd）
     *
     * @return 废弃日期
     */
    String value() default "";

    /**
     * 停止支持日期（格式: yyyy-MM-dd）
     *
     * @return 停止支持日期
     */
    String sunsetDate() default "";

    /**
     * 推荐的替代 API
     *
     * @return 替代 API 路径
     */
    String replacement() default "";

    /**
     * 迁移文档 URL
     *
     * @return 文档 URL
     */
    String migrationGuide() default "";
}
