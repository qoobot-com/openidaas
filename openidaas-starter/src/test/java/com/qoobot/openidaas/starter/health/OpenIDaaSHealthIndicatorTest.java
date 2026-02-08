package com.qoobot.openidaas.starter.health;

import com.qoobot.openidaas.starter.properties.OpenIDaaSProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OpenIDaaS 健康检查测试
 *
 * @author OpenIDaaS Team
 * @since 1.0.0
 */
class OpenIDaaSHealthIndicatorTest {

    @Test
    void testHealthIndicatorWithDefaultProperties() {
        OpenIDaaSProperties properties = new OpenIDaaSProperties();
        OpenIDaaSHealthIndicator indicator = new OpenIDaaSHealthIndicator(properties);

        Health health = indicator.health();

        assertThat(health.getStatus()).isIn(Status.UP, Status.DOWN);
        assertThat(health.getDetails()).containsKey("enabled");
        assertThat(health.getDetails()).containsKey("version");
        assertThat(health.getDetails()).containsKey("modules");
    }

    @Test
    void testHealthIndicatorWithInvalidConfiguration() {
        OpenIDaaSProperties properties = new OpenIDaaSProperties();
        properties.getAuth().getJwt().setSecret(""); // 无效的 JWT secret

        OpenIDaaSHealthIndicator indicator = new OpenIDaaSHealthIndicator(properties);
        Health health = indicator.health();

        // 配置不完整时应该返回 DOWN
        assertThat(health.getDetails()).containsKey("configuration");
        assertThat(health.getDetails().get("configuration")).isEqualTo("INCOMPLETE");
    }

    @Test
    void testHealthIndicatorWithValidConfiguration() {
        OpenIDaaSProperties properties = new OpenIDaaSProperties();
        properties.getAuth().getJwt().setSecret("valid-secret-key");

        OpenIDaaSHealthIndicator indicator = new OpenIDaaSHealthIndicator(properties);
        Health health = indicator.health();

        // 配置完整时应该返回 UP（假设数据库连接正常）
        assertThat(health.getDetails()).containsKey("configuration");
        assertThat(health.getDetails().get("configuration")).isEqualTo("OK");
    }

    @Test
    void testGetDetailedHealth() {
        OpenIDaaSProperties properties = new OpenIDaaSProperties();
        OpenIDaaSHealthIndicator indicator = new OpenIDaaSHealthIndicator(properties);

        var details = indicator.getDetailedHealth();

        assertThat(details).containsKey("auth");
        assertThat(details).containsKey("security");
        assertThat(details).containsKey("tenant");
        assertThat(details).containsKey("monitoring");

        var authDetails = (java.util.Map<String, Object>) details.get("auth");
        assertThat(authDetails).containsKey("enabled");
        assertThat(authDetails).containsKey("jwt.enabled");
        assertThat(authDetails).containsKey("oauth2.enabled");
        assertThat(authDetails).containsKey("oidc.enabled");
    }

    @Test
    void testModuleStatusReporting() {
        OpenIDaaSProperties properties = new OpenIDaaSProperties();
        properties.getAuth().setEnabled(false);
        properties.getSecurity().setEnabled(false);

        OpenIDaaSHealthIndicator indicator = new OpenIDaaSHealthIndicator(properties);
        Health health = indicator.health();

        var modules = (java.util.Map<String, Boolean>) health.getDetails().get("modules");
        assertThat(modules.get("auth")).isFalse();
        assertThat(modules.get("security")).isFalse();
        assertThat(modules.get("tenant")).isTrue();
    }
}
