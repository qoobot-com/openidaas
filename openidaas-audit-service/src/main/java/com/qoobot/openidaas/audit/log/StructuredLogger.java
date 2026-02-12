package com.qoobot.openidaas.audit.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 结构化日志记录器
 * 提供统一的日志格式，便于日志分析和查询
 */
@Slf4j
@Component
public class StructuredLogger {

    /**
     * 记录审计操作日志
     */
    public void logAuditOperation(String operation, String module, String operator, 
                                  String target, boolean success, long duration) {
        log.info("AUDIT_OPERATION | operation={}, module={}, operator={}, target={}, success={}, duration={}ms",
                operation, module, operator, target, success, duration);
    }
    
    /**
     * 记录审计查询日志
     */
    public void logAuditQuery(String queryType, String filters, int resultCount, long duration) {
        log.info("AUDIT_QUERY | queryType={}, filters={}, resultCount={}, duration={}ms",
                queryType, filters, resultCount, duration);
    }
    
    /**
     * 记录审计导出日志
     */
    public void logAuditExport(String format, String timeRange, long recordCount, long fileSize) {
        log.info("AUDIT_EXPORT | format={}, timeRange={}, recordCount={}, fileSize={}bytes",
                format, timeRange, recordCount, fileSize);
    }
    
    /**
     * 记录审计清理日志
     */
    public void logAuditCleanup(long beforeTime, int deletedCount) {
        log.info("AUDIT_CLEANUP | beforeTime={}, deletedCount={}", beforeTime, deletedCount);
    }
    
    /**
     * 记录审计异常日志
     */
    public void logAuditError(String operation, String error, Object... params) {
        log.error("AUDIT_ERROR | operation={}, error={}, params={}", 
                operation, error, params);
    }
    
    /**
     * 记录审计警告日志
     */
    public void logAuditWarning(String warning, Object... params) {
        log.warn("AUDIT_WARNING | warning={}, params={}", warning, params);
    }
    
    /**
     * 记录安全事件日志
     */
    public void logSecurityEvent(String eventType, String severity, String user, 
                                 String ip, String description) {
        log.warn("SECURITY_EVENT | eventType={}, severity={}, user={}, ip={}, description={}",
                eventType, severity, user, ip, description);
    }
    
    /**
     * 记录性能指标日志
     */
    public void logPerformanceMetric(String metricName, double value, String unit) {
        log.info("PERFORMANCE | metric={}, value={}{}", metricName, value, unit);
    }
    
    /**
     * 记录业务指标日志
     */
    public void logBusinessMetric(String metricName, String action, double value) {
        log.info("BUSINESS_METRIC | metric={}, action={}, value={}", metricName, action, value);
    }
    
    /**
     * 记录慢查询日志
     */
    public void logSlowQuery(String queryType, String sql, long duration, long threshold) {
        log.warn("SLOW_QUERY | queryType={}, duration={}ms, threshold={}ms, sql={}",
                queryType, duration, threshold, sql);
    }
    
    /**
     * 记录API调用日志
     */
    public void logApiCall(String method, String endpoint, int statusCode, 
                          long duration, String user) {
        log.info("API_CALL | method={}, endpoint={}, statusCode={}, duration={}ms, user={}",
                method, endpoint, statusCode, duration, user);
    }
    
    /**
     * 记录缓存命中/未命中
     */
    public void logCacheHit(String cacheName, String key, boolean hit) {
        log.debug("CACHE | cache={}, key={}, hit={}", cacheName, key, hit);
    }
    
    /**
     * 记录Kafka消息发送
     */
    public void logKafkaSend(String topic, String key, boolean success, long duration) {
        if (success) {
            log.info("KAFKA_SEND | topic={}, key={}, duration={}ms", topic, key, duration);
        } else {
            log.error("KAFKA_SEND_FAILED | topic={}, key={}, duration={}ms", topic, key, duration);
        }
    }
    
    /**
     * 记录Kafka消息接收
     */
    public void logKafkaReceive(String topic, String key, long duration) {
        log.info("KAFKA_RECEIVE | topic={}, key={}, duration={}ms", topic, key, duration);
    }
    
    /**
     * 记录数据库操作
     */
    public void logDatabaseOperation(String operation, String table, int affectedRows, 
                                     long duration, boolean success) {
        if (success) {
            log.debug("DB_OPERATION | operation={}, table={}, affectedRows={}, duration={}ms",
                    operation, table, affectedRows, duration);
        } else {
            log.error("DB_OPERATION_FAILED | operation={}, table={}, duration={}ms",
                    operation, table, duration);
        }
    }
    
    /**
     * 记录Redis操作
     */
    public void logRedisOperation(String operation, String key, boolean success, long duration) {
        if (success) {
            log.debug("REDIS_OPERATION | operation={}, key={}, duration={}ms", operation, key, duration);
        } else {
            log.error("REDIS_OPERATION_FAILED | operation={}, key={}, duration={}ms", operation, key, duration);
        }
    }
    
    /**
     * 带耗时测量的日志记录
     */
    public <T> T logWithDuration(String logPrefix, Supplier<T> action) {
        long startTime = System.currentTimeMillis();
        try {
            T result = action.get();
            long duration = System.currentTimeMillis() - startTime;
            log.info("{} | duration={}ms, success=true", logPrefix, duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("{} | duration={}ms, success=false, error={}", 
                    logPrefix, duration, e.getMessage());
            throw e;
        }
    }
}
