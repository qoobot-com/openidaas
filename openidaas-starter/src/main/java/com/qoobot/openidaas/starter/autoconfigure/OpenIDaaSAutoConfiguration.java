package com.qoobot.openidaas.starter.autoconfigure;

import com.qoobot.openidaas.starter.EnableOpenIDaaS;
import com.qoobot.openidaas.starter.health.OpenIDaaSHealthIndicator;
import com.qoobot.openidaas.starter.properties.OpenIDaaSProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * OpenIDaaS 自动配置类
 *
 * 此类负责自动配置 OpenIDaaS 框架的核心组件。
 * 根据 classpath 中的依赖和配置属性，自动装配所需的功能模块。
 *
 * <p>自动配置优先级：</p>
 * <ul>
 *   <li>1. 检查 openidaas.enabled 配置</li>
 *   <li>2. 检查 @EnableOpenIDaaS 注解</li>
 *   <li>3. 检查 classpath 中的依赖</li>
 *   <li>4. 检查配置属性</li>
 * </ul>
 *
 * @author OpenIDaaS Team
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(
    prefix = "openidaas",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(OpenIDaaSProperties.class)
@Import({
    OpenIDaaSAuthAutoConfiguration.class,
    OpenIDaaSSecurityAutoConfiguration.class,
    OpenIDaaSTenantAutoConfiguration.class,
    OpenIDaaSUserAutoConfiguration.class,
    OpenIDaaSGatewayAutoConfiguration.class
})
public class OpenIDaaSAutoConfiguration {

    /**
     * 密码编码器 Bean
     *
     * 默认使用 BCrypt 加密算法，强度为 10。
     * 如果用户已自定义 PasswordEncoder，则不创建此 Bean。
     */
    @Bean
    @ConditionalOnMissingBean
    @Order(100)
    public PasswordEncoder passwordEncoder(OpenIDaaSProperties properties) {
        log.info("Initializing BCryptPasswordEncoder for OpenIDaaS");
        return new BCryptPasswordEncoder();
    }

    /**
     * OpenIDaaS 健康检查 Bean
     *
     * 需要满足以下条件：
     * 1. openidaas.monitoring.health-check-enabled = true
     * 2. classpath 中存在 HealthIndicator 类
     * 3. actuator 已启用
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.actuator.health.HealthIndicator")
    @ConditionalOnEnabledHealthIndicator("openidaas")
    @ConditionalOnProperty(
        prefix = "openidaas.monitoring",
        name = "health-check-enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    @ConditionalOnMissingBean
    public OpenIDaaSHealthIndicator openIDaaSHealthIndicator(OpenIDaaSProperties properties) {
        log.info("Initializing OpenIDaaSHealthIndicator");
        return new OpenIDaaSHealthIndicator(properties);
    }

    /**
     * OpenIDaaS 初始化后置处理器
     *
     * 在所有 Bean 初始化完成后，执行初始化逻辑。
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "openidaas",
        name = "database.auto-init",
        havingValue = "true"
    )
    public OpenIDaaSInitializer openIDaaSInitializer(OpenIDaaSProperties properties) {
        log.info("Initializing OpenIDaaS with auto-init enabled");
        return new OpenIDaaSInitializer(properties);
    }
}
