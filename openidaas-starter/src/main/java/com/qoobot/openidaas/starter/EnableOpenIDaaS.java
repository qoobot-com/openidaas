package com.qoobot.openidaas.starter;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * OpenIDaaS 启用注解
 *
 * 使用此注解可以显式启用 OpenIDaaS 框架。
 * 默认情况下，OpenIDaaS 会自动检测和配置，
 * 但在某些场景下（如多数据源），可能需要显式启用。
 *
 * <p>示例：</p>
 * <pre>
 * &#64;SpringBootApplication
 * &#64;EnableOpenIDaaS
 * public class Application {
 *     public static void main(String[] args) {
 *         SpringApplication.run(Application.class, args);
 *     }
 * }
 * </pre>
 *
 * @author OpenIDaaS Team
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(OpenIDaaSAutoConfiguration.class)
public @interface EnableOpenIDaaS {

    /**
     * 是否启用认证功能
     */
    boolean enableAuth() default true;

    /**
     * 是否启用用户管理功能
     */
    boolean enableUser() default true;

    /**
     * 是否启用租户管理功能
     */
    boolean enableTenant() default true;

    /**
     * 是否启用安全功能
     */
    boolean enableSecurity() default true;

    /**
     * 是否启用网关功能
     */
    boolean enableGateway() default false;

    /**
     * 是否启用健康检查
     */
    boolean enableHealthCheck() default true;
}
