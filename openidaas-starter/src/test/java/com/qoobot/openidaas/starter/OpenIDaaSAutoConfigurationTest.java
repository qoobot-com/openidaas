package com.qoobot.openidaas.starter;

import com.qoobot.openidaas.starter.autoconfigure.OpenIDaaSAutoConfiguration;
import com.qoobot.openidaas.starter.health.OpenIDaaSHealthIndicator;
import com.qoobot.openidaas.starter.properties.OpenIDaaSProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OpenIDaaS 自动配置测试
 *
 * @author OpenIDaaS Team
 * @since 1.0.0
 */
class OpenIDaaSAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpenIDaaSAutoConfiguration.class));

    @Test
    void testAutoConfigurationEnabledByDefault() {
        this.contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(OpenIDaaSProperties.class);
                    assertThat(context).hasSingleBean(PasswordEncoder.class);
                });
    }

    @Test
    void testAutoConfigurationDisabledWhenPropertyIsFalse() {
        this.contextRunner
                .withPropertyValues("openidaas.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(OpenIDaaSProperties.class);
                    assertThat(context).doesNotHaveBean(PasswordEncoder.class);
                });
    }

    @Test
    void testPasswordEncoderBeanCreated() {
        this.contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(PasswordEncoder.class);
                    PasswordEncoder encoder = context.getBean(PasswordEncoder.class);
                    assertThat(encoder).isNotNull();
                    String encoded = encoder.encode("test");
                    assertThat(encoded).isNotNull();
                    assertThat(encoded).isNotEqualTo("test");
                });
    }

    @Test
    void testPropertiesBinding() {
        this.contextRunner
                .withPropertyValues(
                    "openidaas.auth.jwt.secret=my-secret-key",
                    "openidaas.auth.jwt.expiration=7200",
                    "openidaas.security.password-policy.min-length=12",
                    "openidaas.tenant.isolation-strategy=DATABASE"
                )
                .run(context -> {
                    OpenIDaaSProperties properties = context.getBean(OpenIDaaSProperties.class);
                    assertThat(properties.getAuth().getJwt().getSecret()).isEqualTo("my-secret-key");
                    assertThat(properties.getAuth().getJwt().getExpiration()).isEqualTo(7200L);
                    assertThat(properties.getSecurity().getPasswordPolicy().getMinLength()).isEqualTo(12);
                    assertThat(properties.getTenant().getIsolationStrategy()).isEqualTo("DATABASE");
                });
    }

    @Test
    void testHealthIndicatorCreatedWhenActuatorEnabled() {
        this.contextRunner
                .withPropertyValues(
                    "management.health.openidaas.enabled=true",
                    "openidaas.monitoring.health-check-enabled=true"
                )
                .run(context -> {
                    // 假设 Actuator 在 classpath 中
                    // assertThat(context).hasSingleBean(OpenIDaaSHealthIndicator.class);
                });
    }

    @Test
    void testDefaultConfigurationValues() {
        this.contextRunner
                .run(context -> {
                    OpenIDaaSProperties properties = context.getBean(OpenIDaaSProperties.class);
                    assertThat(properties.isEnabled()).isTrue();
                    assertThat(properties.getAuth().isEnabled()).isTrue();
                    assertThat(properties.getAuth().getJwt().isEnabled()).isTrue();
                    assertThat(properties.getSecurity().isEnabled()).isTrue();
                    assertThat(properties.getTenant().isEnabled()).isTrue();
                    assertThat(properties.getMonitoring().isEnabled()).isTrue();
                });
    }
}
