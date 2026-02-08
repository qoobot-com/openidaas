package com.qoobot.openidaas.starter.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Token 指标收集器
 *
 * <p>收集 Token 相关的业务指标</p>
 *
 * @author OpenIDaaS Team
 * @since 1.0.0
 */
@Slf4j
public class TokenMetrics {

    private final MeterRegistry registry;

    // Token 生成计数器
    private final Counter tokenGeneratedCounter;

    // Token 刷新计数器
    private final Counter tokenRefreshCounter;
    private final Counter tokenRefreshSuccessCounter;
    private final Counter tokenRefreshFailureCounter;

    // Token 验证计数器
    private final Counter tokenVerifiedCounter;
    private final Counter tokenVerificationSuccessCounter;
    private final Counter tokenVerificationFailureCounter;

    // Token 撤销计数器
    private final Counter tokenRevokedCounter;

    // Token 泄露检测计数器
    private final Counter tokenLeakDetectedCounter;

    // Token 响应时间
    private final Timer tokenGenerationTimer;
    private final Timer tokenRefreshTimer;
    private final Timer tokenVerificationTimer;

    // 活跃 Token 数
    private final AtomicLong activeAccessTokens;
    private final AtomicLong activeRefreshTokens;
    private final AtomicLong expiredTokens;

    public TokenMetrics(MeterRegistry registry) {
        this.registry = registry;

        // Token 生成计数器
        this.tokenGeneratedCounter = Counter.builder("token_generated_total")
                .description("Total tokens generated")
                .register(registry);

        // Token 刷新计数器
        this.tokenRefreshCounter = Counter.builder("token_refresh_total")
                .description("Total token refresh requests")
                .register(registry);

        this.tokenRefreshSuccessCounter = Counter.builder("token_refresh_success_total")
                .description("Total successful token refreshes")
                .register(registry);

        this.tokenRefreshFailureCounter = Counter.builder("token_refresh_failure_total")
                .description("Total failed token refreshes")
                .register(registry);

        // Token 验证计数器
        this.tokenVerifiedCounter = Counter.builder("token_verified_total")
                .description("Total token verifications")
                .register(registry);

        this.tokenVerificationSuccessCounter = Counter.builder("token_verification_success_total")
                .description("Total successful token verifications")
                .register(registry);

        this.tokenVerificationFailureCounter = Counter.builder("token_verification_failure_total")
                .description("Total failed token verifications")
                .register(registry);

        // Token 撤销计数器
        this.tokenRevokedCounter = Counter.builder("token_revoked_total")
                .description("Total tokens revoked")
                .register(registry);

        // Token 泄露检测计数器
        this.tokenLeakDetectedCounter = Counter.builder("token_leak_detected_total")
                .description("Total token leaks detected")
                .register(registry);

        // Token 响应时间
        this.tokenGenerationTimer = Timer.builder("token_generation_time_seconds")
                .description("Token generation time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);

        this.tokenRefreshTimer = Timer.builder("token_refresh_time_seconds")
                .description("Token refresh time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);

        this.tokenVerificationTimer = Timer.builder("token_verification_time_seconds")
                .description("Token verification time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);

        // 活跃 Token 数
        this.activeAccessTokens = new AtomicLong(0);
        Gauge.builder("token_active_access_tokens", activeAccessTokens, AtomicLong::get)
                .description("Number of active access tokens")
                .register(registry);

        this.activeRefreshTokens = new AtomicLong(0);
        Gauge.builder("token_active_refresh_tokens", activeRefreshTokens, AtomicLong::get)
                .description("Number of active refresh tokens")
                .register(registry);

        this.expiredTokens = new AtomicLong(0);
        Gauge.builder("token_expired_total", expiredTokens, AtomicLong::get)
                .description("Number of expired tokens")
                .register(registry);
    }

    /**
     * 记录 Token 生成
     */
    public void recordTokenGenerated(String tokenType) {
        Counter.builder("token_generated_total")
                .description("Total tokens generated")
                .tags("type", tokenType)
                .register(registry)
                .increment();
        tokenGeneratedCounter.increment();
        incrementActiveAccessTokens();
    }

    /**
     * 记录 Token 刷新
     */
    public void recordTokenRefresh() {
        tokenRefreshCounter.increment();
    }

    /**
     * 记录 Token 刷新成功
     */
    public void recordTokenRefreshSuccess() {
        tokenRefreshSuccessCounter.increment();
    }

    /**
     * 记录 Token 刷新失败
     *
     * @param reason 失败原因
     */
    public void recordTokenRefreshFailure(String reason) {
        tokenRefreshFailureCounter.increment();
        Counter.builder("token_refresh_failure_total")
                .description("Total failed token refreshes")
                .tags("reason", reason)
                .register(registry)
                .increment();
    }

    /**
     * 记录 Token 验证
     */
    public void recordTokenVerified() {
        tokenVerifiedCounter.increment();
    }

    /**
     * 记录 Token 验证成功
     */
    public void recordTokenVerificationSuccess() {
        tokenVerificationSuccessCounter.increment();
    }

    /**
     * 记录 Token 验证失败
     *
     * @param reason 失败原因
     */
    public void recordTokenVerificationFailure(String reason) {
        tokenVerificationFailureCounter.increment();
        Counter.builder("token_verification_failure_total")
                .description("Total failed token verifications")
                .tags("reason", reason)
                .register(registry)
                .increment();
    }

    /**
     * 记录 Token 撤销
     */
    public void recordTokenRevoked(String tokenType) {
        Counter.builder("token_revoked_total")
                .description("Total tokens revoked")
                .tags("type", tokenType)
                .register(registry)
                .increment();
        tokenRevokedCounter.increment();
        decrementActiveAccessTokens();
    }

    /**
     * 记录 Token 泄露检测
     *
     * @param type 泄露类型
     */
    public void recordTokenLeakDetected(String type) {
        Counter.builder("token_leak_detected_total")
                .description("Total token leaks detected")
                .tags("type", type)
                .register(registry)
                .increment();
        tokenLeakDetectedCounter.increment();
    }

    /**
     * 记录 Token 生成时间
     *
     * @param startTime 开始时间（纳秒）
     */
    public void recordTokenGenerationTime(long startTime) {
        long duration = System.nanoTime() - startTime;
        tokenGenerationTimer.record(duration, java.util.concurrent.TimeUnit.NANOSECONDS);
    }

    /**
     * 记录 Token 刷新时间
     *
     * @param startTime 开始时间（纳秒）
     */
    public void recordTokenRefreshTime(long startTime) {
        long duration = System.nanoTime() - startTime;
        tokenRefreshTimer.record(duration, java.util.concurrent.TimeUnit.NANOSECONDS);
    }

    /**
     * 记录 Token 验证时间
     *
     * @param startTime 开始时间（纳秒）
     */
    public void recordTokenVerificationTime(long startTime) {
        long duration = System.nanoTime() - startTime;
        tokenVerificationTimer.record(duration, java.util.concurrent.TimeUnit.NANOSECONDS);
    }

    /**
     * 增加活跃 Access Token 数
     */
    public void incrementActiveAccessTokens() {
        activeAccessTokens.incrementAndGet();
    }

    /**
     * 减少活跃 Access Token 数
     */
    public void decrementActiveAccessTokens() {
        activeAccessTokens.decrementAndGet();
    }

    /**
     * 设置活跃 Access Token 数
     *
     * @param count Token 数量
     */
    public void setActiveAccessTokens(long count) {
        activeAccessTokens.set(count);
    }

    /**
     * 设置活跃 Refresh Token 数
     *
     * @param count Token 数量
     */
    public void setActiveRefreshTokens(long count) {
        activeRefreshTokens.set(count);
    }

    /**
     * 增加过期 Token 数
     */
    public void incrementExpiredTokens() {
        expiredTokens.incrementAndGet();
    }

    /**
     * 获取 Token 刷新成功率
     *
     * @return 成功率（百分比）
     */
    public double getTokenRefreshSuccessRate() {
        long total = (long) tokenRefreshCounter.count();
        if (total == 0) {
            return 0.0;
        }
        return (tokenRefreshSuccessCounter.count() / total) * 100;
    }

    /**
     * 获取 Token 验证成功率
     *
     * @return 成功率（百分比）
     */
    public double getTokenVerificationSuccessRate() {
        long total = (long) tokenVerifiedCounter.count();
        if (total == 0) {
            return 0.0;
        }
        return (tokenVerificationSuccessCounter.count() / total) * 100;
    }
}
