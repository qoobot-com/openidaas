package com.qoobot.openidaas.audit.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 审计日志指标收集器
 * 使用Micrometer收集和暴露审计相关的指标
 */
@Slf4j
@Component
public class AuditMetricsCollector {

    private final MeterRegistry meterRegistry;
    
    // 审计日志总数
    private final Counter totalLogsCounter;
    
    // 成功审计日志数
    private final Counter successLogsCounter;
    
    // 失败审计日志数
    private final Counter failedLogsCounter;
    
    // 异步审计日志数
    private final Counter asyncLogsCounter;
    
    // 按操作类型统计的Counter
    private final Counter operationTypeCounter;
    
    // 按模块统计的Counter
    private final Counter moduleCounter;
    
    // 按租户统计的Counter
    private final Counter tenantCounter;
    
    // 审计日志记录耗时Timer
    private final Timer recordLogTimer;
    
    // 审计日志查询耗时Timer
    private final Timer queryLogTimer;
    
    // 审计日志导出耗时Timer
    private final Timer exportLogTimer;

    public AuditMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // 初始化Counters
        this.totalLogsCounter = Counter.builder("audit.logs.total")
                .description("Total number of audit logs recorded")
                .tag("service", "audit-service")
                .register(meterRegistry);
        
        this.successLogsCounter = Counter.builder("audit.logs.success")
                .description("Number of successful audit logs")
                .tag("service", "audit-service")
                .register(meterRegistry);
        
        this.failedLogsCounter = Counter.builder("audit.logs.failed")
                .description("Number of failed audit logs")
                .tag("service", "audit-service")
                .register(meterRegistry);
        
        this.asyncLogsCounter = Counter.builder("audit.logs.async")
                .description("Number of async audit logs recorded")
                .tag("service", "audit-service")
                .register(meterRegistry);
        
        this.operationTypeCounter = Counter.builder("audit.logs.by.operation.type")
                .description("Audit logs by operation type")
                .tag("service", "audit-service")
                .register(meterRegistry);
        
        this.moduleCounter = Counter.builder("audit.logs.by.module")
                .description("Audit logs by module")
                .tag("service", "audit-service")
                .register(meterRegistry);
        
        this.tenantCounter = Counter.builder("audit.logs.by.tenant")
                .description("Audit logs by tenant")
                .tag("service", "audit-service")
                .register(meterRegistry);
        
        // 初始化Timers
        this.recordLogTimer = Timer.builder("audit.log.record.duration")
                .description("Audit log record duration")
                .tag("operation", "record")
                .register(meterRegistry);
        
        this.queryLogTimer = Timer.builder("audit.log.query.duration")
                .description("Audit log query duration")
                .tag("operation", "query")
                .register(meterRegistry);
        
        this.exportLogTimer = Timer.builder("audit.log.export.duration")
                .description("Audit log export duration")
                .tag("operation", "export")
                .register(meterRegistry);
    }
    
    /**
     * 记录审计日志总数
     */
    public void incrementTotalLogs() {
        totalLogsCounter.increment();
        log.debug("Total audit logs counter incremented");
    }
    
    /**
     * 记录成功的审计日志
     */
    public void incrementSuccessLogs() {
        successLogsCounter.increment();
        log.debug("Success audit logs counter incremented");
    }
    
    /**
     * 记录失败的审计日志
     */
    public void incrementFailedLogs() {
        failedLogsCounter.increment();
        log.debug("Failed audit logs counter incremented");
    }
    
    /**
     * 记录异步审计日志
     */
    public void incrementAsyncLogs() {
        asyncLogsCounter.increment();
        log.debug("Async audit logs counter incremented");
    }
    
    /**
     * 按操作类型记录审计日志
     */
    public void incrementByOperationType(String operationType) {
        Counter.builder("audit.logs.by.operation.type")
                .tag("service", "audit-service")
                .tag("operation_type", operationType)
                .description("Audit logs by operation type")
                .register(meterRegistry)
                .increment();
        log.debug("Audit logs by operation type {} incremented", operationType);
    }
    
    /**
     * 按模块记录审计日志
     */
    public void incrementByModule(String module) {
        Counter.builder("audit.logs.by.module")
                .tag("service", "audit-service")
                .tag("module", module)
                .description("Audit logs by module")
                .register(meterRegistry)
                .increment();
        log.debug("Audit logs by module {} incremented", module);
    }
    
    /**
     * 按租户记录审计日志
     */
    public void incrementByTenant(Long tenantId) {
        Counter.builder("audit.logs.by.tenant")
                .tag("service", "audit-service")
                .tag("tenant_id", tenantId != null ? String.valueOf(tenantId) : "unknown")
                .description("Audit logs by tenant")
                .register(meterRegistry)
                .increment();
        log.debug("Audit logs by tenant {} incremented", tenantId);
    }
    
    /**
     * 记录审计日志记录耗时
     */
    public void recordRecordDuration(Runnable action) {
        recordLogTimer.record(action);
    }
    
    /**
     * 记录审计日志记录耗时（带返回值）
     */
    public <T> T recordRecordDuration(java.util.function.Supplier<T> action) {
        return recordLogTimer.record(action);
    }
    
    /**
     * 记录审计日志查询耗时
     */
    public void recordQueryDuration(Runnable action) {
        queryLogTimer.record(action);
    }
    
    /**
     * 记录审计日志查询耗时（带返回值）
     */
    public <T> T recordQueryDuration(java.util.function.Supplier<T> action) {
        return queryLogTimer.record(action);
    }
    
    /**
     * 记录审计日志导出耗时
     */
    public void recordExportDuration(Runnable action) {
        exportLogTimer.record(action);
    }
    
    /**
     * 记录审计日志导出耗时（带返回值）
     */
    public <T> T recordExportDuration(java.util.function.Supplier<T> action) {
        return exportLogTimer.record(action);
    }
    
    /**
     * 记录自定义指标
     */
    public void recordCustomMetric(String name, String description, double value, String... tags) {
        io.micrometer.core.instrument.Gauge.builder(name, () -> value)
                .description(description)
                .tags(tags)
                .register(meterRegistry);
    }
    
    /**
     * 记录当前活跃操作数
     */
    public void recordActiveOperations(int count) {
        recordCustomMetric("audit.operations.active", "Number of active audit operations", count,
                "service", "audit-service");
    }
    
    /**
     * 记录缓存命中率
     */
    public void recordCacheHitRate(String cacheName, double hitRate) {
        recordCustomMetric("audit.cache.hit.rate", "Cache hit rate", hitRate,
                "service", "audit-service",
                "cache", cacheName);
    }
}
