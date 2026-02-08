package com.qoobot.openidaas.starter.health;

import com.qoobot.openidaas.starter.properties.OpenIDaaSProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * OpenIDaaS 健康检查指示器
 *
 * 检查以下组件的健康状态：
 * 1. 数据库连接
 * 2. 配置状态
 * 3. 模块启用状态
 *
 * @author OpenIDaaS Team
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnBean(OpenIDaaSProperties.class)
public class OpenIDaaSHealthIndicator implements HealthIndicator {

    private final OpenIDaaSProperties properties;
    private DataSource dataSource;

    public OpenIDaaSHealthIndicator(OpenIDaaSProperties properties) {
        this.properties = properties;
    }

    public OpenIDaaSHealthIndicator(OpenIDaaSProperties properties, DataSource dataSource) {
        this.properties = properties;
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();

        try {
            // 检查配置状态
            details.put("enabled", properties.isEnabled());
            details.put("version", "1.0.0");

            // 检查模块启用状态
            Map<String, Boolean> modules = new HashMap<>();
            modules.put("auth", properties.getAuth().isEnabled());
            modules.put("security", properties.getSecurity().isEnabled());
            modules.put("tenant", properties.getTenant().isEnabled());
            details.put("modules", modules);

            // 检查数据库连接
            if (dataSource != null) {
                boolean dbHealthy = checkDatabaseConnection();
                details.put("database", dbHealthy ? "UP" : "DOWN");

                if (!dbHealthy) {
                    return Health.down().withDetails(details).build();
                }
            } else {
                details.put("database", "NOT_CONFIGURED");
            }

            // 检查配置完整性
            boolean configHealthy = checkConfiguration();
            details.put("configuration", configHealthy ? "OK" : "INCOMPLETE");

            return Health.up().withDetails(details).build();

        } catch (Exception e) {
            log.error("OpenIDaaS health check failed", e);
            details.put("error", e.getMessage());
            return Health.down().withDetails(details).withException(e).build();
        }
    }

    /**
     * 检查数据库连接
     */
    private boolean checkDatabaseConnection() {
        if (dataSource == null) {
            return false;
        }

        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5秒超时
        } catch (Exception e) {
            log.warn("Database connection check failed", e);
            return false;
        }
    }

    /**
     * 检查配置完整性
     */
    private boolean checkConfiguration() {
        // 检查 JWT 配置
        if (properties.getAuth().getJwt().isEnabled()) {
            String secret = properties.getAuth().getJwt().getSecret();
            if (secret == null || secret.trim().isEmpty() ||
                secret.equals("openidaas-default-secret-key-change-in-production")) {
                log.warn("JWT secret is not configured or using default value");
                return false;
            }
        }

        // 检查租户配置
        if (properties.getTenant().isEnabled()) {
            String strategy = properties.getTenant().getIsolationStrategy();
            if (strategy == null || strategy.trim().isEmpty()) {
                log.warn("Tenant isolation strategy is not configured");
                return false;
            }
        }

        return true;
    }

    /**
     * 获取详细健康信息
     */
    public Map<String, Object> getDetailedHealth() {
        Map<String, Object> details = new HashMap<>();
        details.put("auth", getAuthDetails());
        details.put("security", getSecurityDetails());
        details.put("tenant", getTenantDetails());
        details.put("monitoring", getMonitoringDetails());
        return details;
    }

    private Map<String, Object> getAuthDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("enabled", properties.getAuth().isEnabled());
        details.put("jwt.enabled", properties.getAuth().getJwt().isEnabled());
        details.put("oauth2.enabled", properties.getAuth().getOauth2().isEnabled());
        details.put("oidc.enabled", properties.getAuth().getOidc().isEnabled());
        return details;
    }

    private Map<String, Object> getSecurityDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("enabled", properties.getSecurity().isEnabled());
        details.put("passwordPolicy.min_length", properties.getSecurity().getPasswordPolicy().getMinLength());
        details.put("mfa.enabled", properties.getSecurity().getMfa().isEnabled());
        details.put("rateLimit.enabled", properties.getSecurity().getRateLimit().isEnabled());
        return details;
    }

    private Map<String, Object> getTenantDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("enabled", properties.getTenant().isEnabled());
        details.put("isolationStrategy", properties.getTenant().getIsolationStrategy());
        details.put("defaultTenantId", properties.getTenant().getDefaultTenantId());
        return details;
    }

    private Map<String, Object> getMonitoringDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("enabled", properties.getMonitoring().isEnabled());
        details.put("metrics.enabled", properties.getMonitoring().isMetricsEnabled());
        details.put("audit.enabled", properties.getMonitoring().isAuditEnabled());
        details.put("healthCheck.enabled", properties.getMonitoring().isHealthCheckEnabled());
        return details;
    }
}
