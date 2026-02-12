package com.qoobot.openidaas.audit.alert;

import com.qoobot.openidaas.audit.log.StructuredLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 审计告警组件
 * 监控关键指标并在异常情况下发送告警
 */
@Slf4j
@Component
public class AuditAlertManager {

    private final StructuredLogger structuredLogger;
    
    // 告警是否启用
    @Value("${audit.alert.enabled:true}")
    private boolean alertEnabled;
    
    // 告警阈值配置
    @Value("${audit.alert.error-rate-threshold:0.5}")
    private double errorRateThreshold;
    
    @Value("${audit.alert.slow-query-threshold:5000}")
    private long slowQueryThreshold;
    
    @Value("${audit.alert.high-failure-count-threshold:10}")
    private int highFailureCountThreshold;
    
    // 计数器
    private final AtomicInteger errorCount = new AtomicInteger(0);
    private final AtomicInteger slowQueryCount = new AtomicInteger(0);
    private final AtomicInteger securityEventCount = new AtomicInteger(0);
    private final AtomicInteger totalOperations = new AtomicInteger(0);
    
    // 最后告警时间（防止告警风暴）
    private volatile long lastAlertTime = 0;
    private static final long ALERT_COOLDOWN = 5 * 60 * 1000; // 5分钟冷却时间
    
    public AuditAlertManager(StructuredLogger structuredLogger) {
        this.structuredLogger = structuredLogger;
    }

    /**
     * 记录错误
     */
    public void recordError(String operation, String error) {
        int count = errorCount.incrementAndGet();
        totalOperations.incrementAndGet();
        
        log.error("Error recorded: operation={}, error={}, totalErrors={}", operation, error, count);
        
        // 检查是否需要告警
        checkAndAlertError(operation, error, count);
    }

    /**
     * 记录慢查询
     */
    public void recordSlowQuery(String query, long duration) {
        int count = slowQueryCount.incrementAndGet();
        
        log.warn("Slow query detected: duration={}ms, totalSlowQueries={}", duration, count);
        
        // 持续慢查询告警
        if (count >= 5) {
            sendAlert("HIGH_SLOW_QUERY_COUNT", 
                    String.format("Slow queries detected: %d queries exceeded threshold", count),
                    AlertSeverity.HIGH);
        }
    }

    /**
     * 记录安全事件
     */
    public void recordSecurityEvent(String eventType, String severity, String description) {
        int count = securityEventCount.incrementAndGet();
        
        structuredLogger.logSecurityEvent(eventType, severity, "system", "unknown", description);
        
        // 紧急安全事件立即告警
        if ("EMERGENCY".equals(severity) || "HIGH".equals(severity)) {
            sendAlert("SECURITY_EVENT", 
                    String.format("Security event detected: type=%s, severity=%s, description=%s", 
                            eventType, severity, description),
                    AlertSeverity.EMERGENCY);
        }
    }

    /**
     * 检查并触发错误告警
     */
    private void checkAndAlertError(String operation, String error, int errorCount) {
        int total = totalOperations.get();
        if (total > 10) {
            double errorRate = (double) errorCount / total;
            
            if (errorRate > errorRateThreshold) {
                sendAlert("HIGH_ERROR_RATE", 
                        String.format("High error rate detected: %.2f%% (operation=%s)", 
                                errorRate * 100, operation),
                        AlertSeverity.HIGH);
            }
        }
        
        if (errorCount >= highFailureCountThreshold) {
            sendAlert("HIGH_FAILURE_COUNT", 
                    String.format("High failure count detected: %d errors (operation=%s)", 
                            errorCount, operation),
                    AlertSeverity.HIGH);
        }
    }

    /**
     * 发送告警
     */
    public void sendAlert(String alertType, String message, AlertSeverity severity) {
        if (!alertEnabled) {
            return;
        }
        
        // 检查冷却时间
        long now = System.currentTimeMillis();
        if (now - lastAlertTime < ALERT_COOLDOWN && severity != AlertSeverity.EMERGENCY) {
            log.warn("Alert cooldown active, skipping alert: {}", alertType);
            return;
        }
        
        lastAlertTime = now;
        
        // 构建告警信息
        Map<String, Object> alertData = new HashMap<>();
        alertData.put("alertType", alertType);
        alertData.put("severity", severity.name());
        alertData.put("message", message);
        alertData.put("timestamp", System.currentTimeMillis());
        alertData.put("service", "audit-service");
        
        // 记录告警日志
        log.error("ALERT | type={}, severity={}, message={}", 
                alertType, severity.name(), message);
        
        // 这里可以集成各种告警渠道：
        // 1. 邮件告警
        // 2. 短信告警
        // 3. 企业微信/钉钉
        // 4. Slack
        // 5. PagerDuty
        // 6. Prometheus Alertmanager
        
        // 示例：记录到结构化日志
        structuredLogger.logAuditError("ALERT", message, alertData);
        
        // TODO: 实现实际的告警发送逻辑
        // sendEmailAlert(alertData);
        // sendWebhookAlert(alertData);
    }

    /**
     * 重置计数器
     */
    public void resetCounters() {
        errorCount.set(0);
        slowQueryCount.set(0);
        securityEventCount.set(0);
        totalOperations.set(0);
        log.info("Alert counters reset");
    }

    /**
     * 获取当前指标
     */
    public Map<String, Integer> getMetrics() {
        Map<String, Integer> metrics = new HashMap<>();
        metrics.put("errorCount", errorCount.get());
        metrics.put("slowQueryCount", slowQueryCount.get());
        metrics.put("securityEventCount", securityEventCount.get());
        metrics.put("totalOperations", totalOperations.get());
        return metrics;
    }

    /**
     * 告警严重程度枚举
     */
    public enum AlertSeverity {
        LOW,
        MEDIUM,
        HIGH,
        EMERGENCY
    }
}
