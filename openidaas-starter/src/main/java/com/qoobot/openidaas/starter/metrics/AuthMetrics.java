package com.qoobot.openidaas.starter.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 认证指标收集器
 *
 * <p>收集用户认证相关的业务指标</p>
 *
 * @author OpenIDaaS Team
 * @since 1.0.0
 */
@Slf4j
public class AuthMetrics {

    private final MeterRegistry registry;

    // 认证请求计数器
    private final Counter authTotalCounter;
    private final Counter authSuccessCounter;
    private final Counter authFailureCounter;

    // 认证失败原因计数器
    private final Counter invalidCredentialsCounter;
    private final Counter accountLockedCounter;
    private final Counter accountDisabledCounter;
    private final Counter mfaRequiredCounter;

    // 认证响应时间
    private final Timer authResponseTimer;

    // 活跃会话数
    private final AtomicLong activeSessions;

    // 在线用户数
    private final AtomicLong onlineUsers;

    public AuthMetrics(MeterRegistry registry) {
        this.registry = registry;

        // 认证请求计数器
        this.authTotalCounter = Counter.builder("auth_total")
                .description("Total authentication requests")
                .tags("type", "all")
                .register(registry);

        this.authSuccessCounter = Counter.builder("auth_success_total")
                .description("Total successful authentications")
                .register(registry);

        this.authFailureCounter = Counter.builder("auth_failure_total")
                .description("Total failed authentications")
                .register(registry);

        // 认证失败原因计数器
        this.invalidCredentialsCounter = Counter.builder("auth_failure_total")
                .description("Failed authentications due to invalid credentials")
                .tags("reason", "invalid_credentials")
                .register(registry);

        this.accountLockedCounter = Counter.builder("auth_failure_total")
                .description("Failed authentications due to account locked")
                .tags("reason", "account_locked")
                .register(registry);

        this.accountDisabledCounter = Counter.builder("auth_failure_total")
                .description("Failed authentications due to account disabled")
                .tags("reason", "account_disabled")
                .register(registry);

        this.mfaRequiredCounter = Counter.builder("auth_failure_total")
                .description("Failed authentications due to MFA required")
                .tags("reason", "mfa_required")
                .register(registry);

        // 认证响应时间
        this.authResponseTimer = Timer.builder("auth_response_time_seconds")
                .description("Authentication response time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);

        // 活跃会话数
        this.activeSessions = new AtomicLong(0);
        Gauge.builder("auth_active_sessions", activeSessions, AtomicLong::get)
                .description("Number of active authentication sessions")
                .register(registry);

        // 在线用户数
        this.onlineUsers = new AtomicLong(0);
        Gauge.builder("auth_online_users", onlineUsers, AtomicLong::get)
                .description("Number of online users")
                .register(registry);
    }

    /**
     * 记录认证请求
     */
    public void recordAuthRequest() {
        authTotalCounter.increment();
    }

    /**
     * 记录认证成功
     */
    public void recordAuthSuccess() {
        authSuccessCounter.increment();
    }

    /**
     * 记录认证失败
     *
     * @param reason 失败原因
     */
    public void recordAuthFailure(String reason) {
        authFailureCounter.increment();

        // 根据原因增加特定的计数器
        switch (reason) {
            case "invalid_credentials":
                invalidCredentialsCounter.increment();
                break;
            case "account_locked":
                accountLockedCounter.increment();
                break;
            case "account_disabled":
                accountDisabledCounter.increment();
                break;
            case "mfa_required":
                mfaRequiredCounter.increment();
                break;
            default:
                log.warn("Unknown auth failure reason: {}", reason);
        }
    }

    /**
     * 记录认证响应时间
     *
     * @param startTime 开始时间（纳秒）
     */
    public void recordAuthResponseTime(long startTime) {
        long duration = System.nanoTime() - startTime;
        authResponseTimer.record(duration, java.util.concurrent.TimeUnit.NANOSECONDS);
    }

    /**
     * 增加活跃会话数
     */
    public void incrementActiveSessions() {
        activeSessions.incrementAndGet();
    }

    /**
     * 减少活跃会话数
     */
    public void decrementActiveSessions() {
        activeSessions.decrementAndGet();
    }

    /**
     * 设置在线用户数
     *
     * @param count 用户数
     */
    public void setOnlineUsers(long count) {
        onlineUsers.set(count);
    }

    /**
     * 获取认证成功率
     *
     * @return 成功率（百分比）
     */
    public double getAuthSuccessRate() {
        long total = (long) authTotalCounter.count();
        if (total == 0) {
            return 0.0;
        }
        return (authSuccessCounter.count() / total) * 100;
    }

    /**
     * 获取认证失败率
     *
     * @return 失败率（百分比）
     */
    public double getAuthFailureRate() {
        long total = (long) authTotalCounter.count();
        if (total == 0) {
            return 0.0;
        }
        return (authFailureCounter.count() / total) * 100;
    }
}
