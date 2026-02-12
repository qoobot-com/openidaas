package com.qoobot.openidaas.common.annotation;

import java.lang.annotation.*;

/**
 * API 版本注解
 * 用于标记 Controller 或方法支持的 API 版本
 *
 * @author QooBot
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiVersion {

    /**
     * 支持的版本号
     * 例如: "v1", "v2"
     *
     * @return 版本号
     */
    String value() default "v1";

    /**
     * 是否为最新版本
     *
     * @return 是否为最新版本
     */
    boolean latest() default false;

    /**
     * 是否已废弃
     *
     * @return 是否已废弃
     */
    boolean deprecated() default false;

    /**
     * 废弃日期（格式: yyyy-MM-dd）
     *
     * @return 废弃日期
     */
    String deprecationDate() default "";

    /**
     * 推荐的替代版本
     *
     * @return 推荐版本
     */
    String recommendedVersion() default "";
}
