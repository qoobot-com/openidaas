package com.qoobot.openidaas.starter.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 租户指标收集器
 *
 * <p>收集租户相关的业务指标</p>
 *
 * @author OpenIDaaS Team
 * @since 1.0.0
 */
@Slf4j
public class TenantMetrics {

    private final MeterRegistry registry;

    // 租户创建计数器
    private final Counter tenantCreatedCounter;

    // 租户删除计数器
    private final Counter tenantDeletedCounter;

    // 租户激活计数器
    private final Counter tenantActivatedCounter;

    // 租户停用计数器
    private final Counter tenantDeactivatedCounter;

    // 租户配额计数器
    private final ConcurrentHashMap<String, AtomicLong> tenantUserCounts;
    private final ConcurrentHashMap<String, AtomicLong> tenantActiveUserCounts;
    private final ConcurrentHashMap<String, AtomicLong> tenantRequestCounts;

    // 租户错误计数器
    private final Counter tenantErrorCounter;

    // 租户请求量
    private final Counter tenantRequestCounter;

    public TenantMetrics(MeterRegistry registry) {
        this.registry = registry;

        // 租户创建计数器
        this.tenantCreatedCounter = Counter.builder("tenant_created_total")
                .description("Total tenants created")
                .register(registry);

        // 租户删除计数器
        this.tenantDeletedCounter = Counter.builder("tenant_deleted_total")
                .description("Total tenants deleted")
                .register(registry);

        // 租户激活计数器
        this.tenantActivatedCounter = Counter.builder("tenant_activated_total")
                .description("Total tenants activated")
                .register(registry);

        // 租户停用计数器
        this.tenantDeactivatedCounter = Counter.builder("tenant_deactivated_total")
                .description("Total tenants deactivated")
                .register(registry);

        // 租户配额计数器
        this.tenantUserCounts = new ConcurrentHashMap<>();
        this.tenantActiveUserCounts = new ConcurrentHashMap<>();
        this.tenantRequestCounts = new ConcurrentHashMap<>();

        // 租户错误计数器
        this.tenantErrorCounter = Counter.builder("tenant_error_total")
                .description("Total tenant errors")
                .register(registry);

        // 租户请求量
        this.tenantRequestCounter = Counter.builder("tenant_request_total")
                .description("Total tenant requests")
                .register(registry);
    }

    /**
     * 记录租户创建
     *
     * @param tenantId 租户ID
     */
    public void recordTenantCreated(String tenantId) {
        Counter.builder("tenant_created_total")
                .description("Total tenants created")
                .tags("tenant_id", tenantId)
                .register(registry)
                .increment();
        tenantCreatedCounter.increment();

        // 初始化租户指标
        tenantUserCounts.putIfAbsent(tenantId, new AtomicLong(0));
        tenantActiveUserCounts.putIfAbsent(tenantId, new AtomicLong(0));
        tenantRequestCounts.putIfAbsent(tenantId, new AtomicLong(0));
    }

    /**
     * 记录租户删除
     *
     * @param tenantId 租户ID
     */
    public void recordTenantDeleted(String tenantId) {
        Counter.builder("tenant_deleted_total")
                .description("Total tenants deleted")
                .tags("tenant_id", tenantId)
                .register(registry)
                .increment();
        tenantDeletedCounter.increment();

        // 清理租户指标
        tenantUserCounts.remove(tenantId);
        tenantActiveUserCounts.remove(tenantId);
        tenantRequestCounts.remove(tenantId);
    }

    /**
     * 记录租户激活
     *
     * @param tenantId 租户ID
     */
    public void recordTenantActivated(String tenantId) {
        Counter.builder("tenant_activated_total")
                .description("Total tenants activated")
                .tags("tenant_id", tenantId)
                .register(registry)
                .increment();
        tenantActivatedCounter.increment();
    }

    /**
     * 记录租户停用
     *
     * @param tenantId 租户ID
     */
    public void recordTenantDeactivated(String tenantId) {
        Counter.builder("tenant_deactivated_total")
                .description("Total tenants deactivated")
                .tags("tenant_id", tenantId)
                .register(registry)
                .increment();
        tenantDeactivatedCounter.increment();
    }

    /**
     * 设置租户用户数
     *
     * @param tenantId 租户ID
     * @param count 用户数
     */
    public void setTenantUserCount(String tenantId, long count) {
        tenantUserCounts.computeIfAbsent(tenantId, k -> {
            AtomicLong counter = new AtomicLong(count);
            Gauge.builder("tenant_users_total", counter, AtomicLong::get)
                    .description("Total users per tenant")
                    .tags("tenant_id", tenantId)
                    .register(registry);
            return counter;
        }).set(count);
    }

    /**
     * 设置租户活跃用户数
     *
     * @param tenantId 租户ID
     * @param count 活跃用户数
     */
    public void setTenantActiveUserCount(String tenantId, long count) {
        tenantActiveUserCounts.computeIfAbsent(tenantId, k -> {
            AtomicLong counter = new AtomicLong(count);
            Gauge.builder("tenant_active_users", counter, AtomicLong::get)
                    .description("Active users per tenant")
                    .tags("tenant_id", tenantId)
                    .register(registry);
            return counter;
        }).set(count);
    }

    /**
     * 记录租户请求
     *
     * @param tenantId 租户ID
     */
    public void recordTenantRequest(String tenantId) {
        Counter.builder("tenant_request_total")
                .description("Total tenant requests")
                .tags("tenant_id", tenantId)
                .register(registry)
                .increment();
        tenantRequestCounter.increment();

        tenantRequestCounts.computeIfAbsent(tenantId, k -> {
            AtomicLong counter = new AtomicLong(0);
            Gauge.builder("tenant_request_rate", counter, AtomicLong::get)
                    .description("Request rate per tenant")
                    .tags("tenant_id", tenantId)
                    .register(registry);
            return counter;
        }).incrementAndGet();
    }

    /**
     * 记录租户错误
     *
     * @param tenantId 租户ID
     * @param errorType 错误类型
     */
    public void recordTenantError(String tenantId, String errorType) {
        Counter.builder("tenant_error_total")
                .description("Total tenant errors")
                .tags("tenant_id", tenantId, "error_type", errorType)
                .register(registry)
                .increment();
        tenantErrorCounter.increment();
    }

    /**
     * 记录租户配额使用情况
     *
     * @param tenantId 租户ID
     * @param resourceType 资源类型
     * @param used 已使用
     * @param limit 限制
     */
    public void recordTenantQuotaUsage(String tenantId, String resourceType, long used, long limit) {
        Gauge.builder("tenant_quota_usage_ratio", () -> (double) used / limit)
                .description("Quota usage ratio per tenant")
                .tags("tenant_id", tenantId, "resource_type", resourceType)
                .register(registry);

        // 记录配额使用绝对值
        Gauge.builder("tenant_quota_used", () -> used)
                .description("Quota used per tenant")
                .tags("tenant_id", tenantId, "resource_type", resourceType)
                .register(registry);

        // 记录配额限制
        Gauge.builder("tenant_quota_limit", () -> limit)
                .description("Quota limit per tenant")
                .tags("tenant_id", tenantId, "resource_type", resourceType)
                .register(registry);
    }

    /**
     * 获取租户活跃用户数
     *
     * @param tenantId 租户ID
     * @return 活跃用户数
     */
    public long getTenantActiveUserCount(String tenantId) {
        AtomicLong counter = tenantActiveUserCounts.get(tenantId);
        return counter != null ? counter.get() : 0;
    }

    /**
     * 获取租户请求量
     *
     * @param tenantId 租户ID
     * @return 请求量
     */
    public long getTenantRequestCount(String tenantId) {
        AtomicLong counter = tenantRequestCounts.get(tenantId);
        return counter != null ? counter.get() : 0;
    }
}
