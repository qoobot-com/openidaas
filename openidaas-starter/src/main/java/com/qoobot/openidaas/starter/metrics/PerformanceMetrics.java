package com.qoobot.openidaas.starter.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 性能指标收集器
 *
 * <p>收集系统性能相关的指标</p>
 *
 * @author OpenIDaaS Team
 * @since 1.0.0
 */
@Slf4j
public class PerformanceMetrics {

    private final MeterRegistry registry;

    // 数据库操作计时器
    private final Timer databaseQueryTimer;
    private final Timer databaseUpdateTimer;
    private final Timer databaseInsertTimer;
    private final Timer databaseDeleteTimer;

    // 缓存操作计时器
    private final Timer cacheGetTimer;
    private final Timer cachePutTimer;
    private final Timer cacheDeleteTimer;

    // 外部 API 调用计时器
    private final Timer externalApiCallTimer;

    // 数据库连接池指标
    private final Counter databaseConnectionAcquiredCounter;
    private final Counter databaseConnectionReleasedCounter;
    private final Counter databaseConnectionTimeoutCounter;
    private final Counter databaseConnectionLeakCounter;

    // 缓存命中/未命中计数器
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;

    // 线程池指标
    private final ConcurrentHashMap<String, Timer> threadPoolTimers;

    public PerformanceMetrics(MeterRegistry registry) {
        this.registry = registry;

        // 数据库操作计时器
        this.databaseQueryTimer = Timer.builder("database_query_time_seconds")
                .description("Database query execution time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);

        this.databaseUpdateTimer = Timer.builder("database_update_time_seconds")
                .description("Database update execution time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);

        this.databaseInsertTimer = Timer.builder("database_insert_time_seconds")
                .description("Database insert execution time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);

        this.databaseDeleteTimer = Timer.builder("database_delete_time_seconds")
                .description("Database delete execution time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);

        // 缓存操作计时器
        this.cacheGetTimer = Timer.builder("cache_get_time_seconds")
                .description("Cache get operation time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);

        this.cachePutTimer = Timer.builder("cache_put_time_seconds")
                .description("Cache put operation time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);

        this.cacheDeleteTimer = Timer.builder("cache_delete_time_seconds")
                .description("Cache delete operation time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);

        // 外部 API 调用计时器
        this.externalApiCallTimer = Timer.builder("external_api_call_time_seconds")
                .description("External API call time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);

        // 数据库连接池指标
        this.databaseConnectionAcquiredCounter = Counter.builder("database_connection_acquired_total")
                .description("Total database connections acquired")
                .register(registry);

        this.databaseConnectionReleasedCounter = Counter.builder("database_connection_released_total")
                .description("Total database connections released")
                .register(registry);

        this.databaseConnectionTimeoutCounter = Counter.builder("database_connection_timeout_total")
                .description("Total database connection timeouts")
                .register(registry);

        this.databaseConnectionLeakCounter = Counter.builder("database_connection_leak_total")
                .description("Total database connection leaks detected")
                .register(registry);

        // 缓存命中/未命中计数器
        this.cacheHitCounter = Counter.builder("cache_hit_total")
                .description("Total cache hits")
                .register(registry);

        this.cacheMissCounter = Counter.builder("cache_miss_total")
                .description("Total cache misses")
                .register(registry);

        // 线程池计时器
        this.threadPoolTimers = new ConcurrentHashMap<>();
    }

    /**
     * 记录数据库查询时间
     *
     * @param startTime 开始时间（纳秒）
     * @param queryName 查询名称
     */
    public void recordDatabaseQueryTime(long startTime, String queryName) {
        long duration = System.nanoTime() - startTime;
        Timer.builder("database_query_time_seconds")
                .description("Database query execution time")
                .tags("query", queryName)
                .register(registry)
                .record(duration, TimeUnit.NANOSECONDS);
        databaseQueryTimer.record(duration, TimeUnit.NANOSECONDS);
    }

    /**
     * 记录数据库更新时间
     *
     * @param startTime 开始时间（纳秒）
     * @param tableName 表名
     */
    public void recordDatabaseUpdateTime(long startTime, String tableName) {
        long duration = System.nanoTime() - startTime;
        Timer.builder("database_update_time_seconds")
                .description("Database update execution time")
                .tags("table", tableName)
                .register(registry)
                .record(duration, TimeUnit.NANOSECONDS);
        databaseUpdateTimer.record(duration, TimeUnit.NANOSECONDS);
    }

    /**
     * 记录数据库插入时间
     *
     * @param startTime 开始时间（纳秒）
     * @param tableName 表名
     */
    public void recordDatabaseInsertTime(long startTime, String tableName) {
        long duration = System.nanoTime() - startTime;
        Timer.builder("database_insert_time_seconds")
                .description("Database insert execution time")
                .tags("table", tableName)
                .register(registry)
                .record(duration, TimeUnit.NANOSECONDS);
        databaseInsertTimer.record(duration, TimeUnit.NANOSECONDS);
    }

    /**
     * 记录数据库删除时间
     *
     * @param startTime 开始时间（纳秒）
     * @param tableName 表名
     */
    public void recordDatabaseDeleteTime(long startTime, String tableName) {
        long duration = System.nanoTime() - startTime;
        Timer.builder("database_delete_time_seconds")
                .description("Database delete execution time")
                .tags("table", tableName)
                .register(registry)
                .record(duration, TimeUnit.NANOSECONDS);
        databaseDeleteTimer.record(duration, TimeUnit.NANOSECONDS);
    }

    /**
     * 记录缓存获取时间
     *
     * @param startTime 开始时间（纳秒）
     * @param cacheName 缓存名称
     * @param hit 是否命中
     */
    public void recordCacheGetTime(long startTime, String cacheName, boolean hit) {
        long duration = System.nanoTime() - startTime;
        Timer.builder("cache_get_time_seconds")
                .description("Cache get operation time")
                .tags("cache", cacheName, "hit", String.valueOf(hit))
                .register(registry)
                .record(duration, TimeUnit.NANOSECONDS);
        cacheGetTimer.record(duration, TimeUnit.NANOSECONDS);

        // 记录命中/未命中
        if (hit) {
            Counter.builder("cache_hit_total")
                    .description("Total cache hits")
                    .tags("cache", cacheName)
                    .register(registry)
                    .increment();
            cacheHitCounter.increment();
        } else {
            Counter.builder("cache_miss_total")
                    .description("Total cache misses")
                    .tags("cache", cacheName)
                    .register(registry)
                    .increment();
            cacheMissCounter.increment();
        }
    }

    /**
     * 记录缓存设置时间
     *
     * @param startTime 开始时间（纳秒）
     * @param cacheName 缓存名称
     */
    public void recordCachePutTime(long startTime, String cacheName) {
        long duration = System.nanoTime() - startTime;
        Timer.builder("cache_put_time_seconds")
                .description("Cache put operation time")
                .tags("cache", cacheName)
                .register(registry)
                .record(duration, TimeUnit.NANOSECONDS);
        cachePutTimer.record(duration, TimeUnit.NANOSECONDS);
    }

    /**
     * 记录缓存删除时间
     *
     * @param startTime 开始时间（纳秒）
     * @param cacheName 缓存名称
     */
    public void recordCacheDeleteTime(long startTime, String cacheName) {
        long duration = System.nanoTime() - startTime;
        Timer.builder("cache_delete_time_seconds")
                .description("Cache delete operation time")
                .tags("cache", cacheName)
                .register(registry)
                .record(duration, TimeUnit.NANOSECONDS);
        cacheDeleteTimer.record(duration, TimeUnit.NANOSECONDS);
    }

    /**
     * 记录外部 API 调用时间
     *
     * @param startTime 开始时间（纳秒）
     * @param apiName API 名称
     * @param success 是否成功
     */
    public void recordExternalApiCallTime(long startTime, String apiName, boolean success) {
        long duration = System.nanoTime() - startTime;
        Timer.builder("external_api_call_time_seconds")
                .description("External API call time")
                .tags("api", apiName, "success", String.valueOf(success))
                .register(registry)
                .record(duration, TimeUnit.NANOSECONDS);
        externalApiCallTimer.record(duration, TimeUnit.NANOSECONDS);
    }

    /**
     * 记录数据库连接获取
     */
    public void recordDatabaseConnectionAcquired() {
        databaseConnectionAcquiredCounter.increment();
    }

    /**
     * 记录数据库连接释放
     */
    public void recordDatabaseConnectionReleased() {
        databaseConnectionReleasedCounter.increment();
    }

    /**
     * 记录数据库连接超时
     */
    public void recordDatabaseConnectionTimeout() {
        databaseConnectionTimeoutCounter.increment();
    }

    /**
     * 记录数据库连接泄露
     */
    public void recordDatabaseConnectionLeak() {
        databaseConnectionLeakCounter.increment();
    }

    /**
     * 记录线程池任务执行时间
     *
     * @param startTime 开始时间（纳秒）
     * @param threadPoolName 线程池名称
     */
    public void recordThreadPoolTaskTime(long startTime, String threadPoolName) {
        long duration = System.nanoTime() - startTime;
        threadPoolTimers.computeIfAbsent(threadPoolName, name ->
                Timer.builder("thread_pool_task_time_seconds")
                        .description("Thread pool task execution time")
                        .tags("thread_pool", name)
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .publishPercentileHistogram()
                        .register(registry)
        ).record(duration, TimeUnit.NANOSECONDS);
    }

    /**
     * 获取缓存命中率
     *
     * @return 命中率（百分比）
     */
    public double getCacheHitRate() {
        long hits = (long) cacheHitCounter.count();
        long misses = (long) cacheMissCounter.count();
        long total = hits + misses;
        if (total == 0) {
            return 0.0;
        }
        return ((double) hits / total) * 100;
    }

    /**
     * 获取数据库连接泄露数
     *
     * @return 泄露数
     */
    public long getDatabaseConnectionLeaks() {
        return (long) databaseConnectionAcquiredCounter.count() -
               (long) databaseConnectionReleasedCounter.count();
    }
}
