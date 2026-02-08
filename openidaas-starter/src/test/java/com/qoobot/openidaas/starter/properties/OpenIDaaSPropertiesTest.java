package com.qoobot.openidaas.starter.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OpenIDaaS 配置属性测试
 *
 * @author OpenIDaaS Team
 * @since 1.0.0
 */
@SpringBootTest
class OpenIDaaSPropertiesTest {

    @Autowired
    private Validator validator;

    @Test
    void testDefaultProperties() {
        OpenIDaaSProperties properties = new OpenIDaaSProperties();
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getAuth().isEnabled()).isTrue();
        assertThat(properties.getSecurity().isEnabled()).isTrue();
        assertThat(properties.getTenant().isEnabled()).isTrue();
    }

    @Test
    void testJwtProperties() {
        OpenIDaaSProperties properties = new OpenIDaaSProperties();
        OpenIDaaSProperties.JwtProperties jwt = properties.getAuth().getJwt();

        assertThat(jwt.isEnabled()).isTrue();
        assertThat(jwt.getSecret()).isNotBlank();
        assertThat(jwt.getExpiration()).isEqualTo(3600L);
        assertThat(jwt.getRefreshExpiration()).isEqualTo(2592000L);
        assertThat(jwt.getAlgorithm()).isEqualTo("HS256");
    }

    @Test
    void testPasswordPolicyProperties() {
        OpenIDaaSProperties properties = new OpenIDaaSProperties();
        OpenIDaaSProperties.PasswordPolicyProperties policy = properties.getSecurity().getPasswordPolicy();

        assertThat(policy.getMinLength()).isEqualTo(8);
        assertThat(policy.getMaxLength()).isEqualTo(128);
        assertThat(policy.isRequireUppercase()).isTrue();
        assertThat(policy.isRequireLowercase()).isTrue();
        assertThat(policy.isRequireNumbers()).isTrue();
        assertThat(policy.isRequireSpecialChars()).isTrue();
        assertThat(policy.getPasswordHistory()).isEqualTo(5);
        assertThat(policy.getExpirationDays()).isEqualTo(90);
    }

    @Test
    void testMfaProperties() {
        OpenIDaaSProperties properties = new OpenIDaaSProperties();
        OpenIDaaSProperties.MfaProperties mfa = properties.getSecurity().getMfa();

        assertThat(mfa.isEnabled()).isFalse();
        assertThat(mfa.isRequiredForAdmin()).isTrue();
        assertThat(mfa.getSupportedTypes()).isNotEmpty();
        assertThat(mfa.getDefaultType()).isEqualTo("TOTP");
        assertThat(mfa.getBackupCodesCount()).isEqualTo(10);
    }

    @Test
    void testTenantProperties() {
        OpenIDaaSProperties properties = new OpenIDaaSProperties();
        OpenIDaaSProperties.TenantProperties tenant = properties.getTenant();

        assertThat(tenant.isEnabled()).isTrue();
        assertThat(tenant.getIsolationStrategy()).isEqualTo("SCHEMA");
        assertThat(tenant.getDefaultTenantId()).isNotNull();
        assertThat(tenant.getTenantResolver()).isEqualTo("HEADER");
        assertThat(tenant.getTenantHeaderName()).isEqualTo("X-Tenant-ID");
    }

    @Test
    void testMonitoringProperties() {
        OpenIDaaSProperties properties = new OpenIDaaSProperties();
        OpenIDaaSProperties.MonitoringProperties monitoring = properties.getMonitoring();

        assertThat(monitoring.isEnabled()).isTrue();
        assertThat(monitoring.isMetricsEnabled()).isTrue();
        assertThat(monitoring.isAuditEnabled()).isTrue();
        assertThat(monitoring.isHealthCheckEnabled()).isTrue();
    }

    @Test
    void testJwtSecretValidation() {
        OpenIDaaSProperties properties = new OpenIDaaSProperties();
        properties.getAuth().getJwt().setSecret("");

        Set<ConstraintViolation<OpenIDaaSProperties>> violations =
            validator.validate(properties);

        // 应该有验证错误，因为 JWT secret 不能为空
        assertThat(violations).isNotEmpty();
    }

    @Test
    void testPasswordPolicyMinLengthValidation() {
        OpenIDaaSProperties properties = new OpenIDaaSProperties();
        properties.getSecurity().getPasswordPolicy().setMinLength(null);

        Set<ConstraintViolation<OpenIDaaSProperties>> violations =
            validator.validate(properties);

        // 应该有验证错误，因为 min-length 不能为 null
        assertThat(violations).isNotEmpty();
    }

    @Test
    void testTenantIsolationStrategyValidation() {
        OpenIDaaSProperties properties = new OpenIDaaSProperties();
        properties.getTenant().setIsolationStrategy(null);

        Set<ConstraintViolation<OpenIDaaSProperties>> violations =
            validator.validate(properties);

        // 应该有验证错误，因为 isolation strategy 不能为 null
        assertThat(violations).isNotEmpty();
    }
}
