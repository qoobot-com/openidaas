package com.qoobot.openidaas.auth.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 业务指标监控
 *
 * @author QooBot
 */
@Component
public class BusinessMetrics {

    private final MeterRegistry registry;

    // 登录成功计数器
    private final Counter loginSuccessCounter;

    // 登录失败计数器
    private final Counter loginFailureCounter;

    // MFA验证成功计数器
    private final Counter mfaSuccessCounter;

    // MFA验证失败计数器
    private final Counter mfaFailureCounter;

    // 登出计数器
    private final Counter logoutCounter;

    // 密码重置计数器
    private final Counter passwordResetCounter;

    // 登录耗时
    private final Timer loginTimer;

    // Token刷新计数器
    private final Counter tokenRefreshCounter;

    // Token刷新失败计数器
    private final Counter tokenRefreshFailureCounter;

    public BusinessMetrics(MeterRegistry registry) {
        this.registry = registry;

        // 初始化计数器
        this.loginSuccessCounter = Counter.builder("auth.login.success")
            .description("Successful login attempts")
            .tag("type", "all")
            .register(registry);

        this.loginFailureCounter = Counter.builder("auth.login.failure")
            .description("Failed login attempts")
            .tag("type", "all")
            .register(registry);

        this.mfaSuccessCounter = Counter.builder("auth.mfa.success")
            .description("Successful MFA verifications")
            .register(registry);

        this.mfaFailureCounter = Counter.builder("auth.mfa.failure")
            .description("Failed MFA verifications")
            .register(registry);

        this.logoutCounter = Counter.builder("auth.logout")
            .description("User logout events")
            .register(registry);

        this.passwordResetCounter = Counter.builder("auth.password.reset")
            .description("Password reset attempts")
            .register(registry);

        this.tokenRefreshCounter = Counter.builder("auth.token.refresh")
            .description("Token refresh attempts")
            .register(registry);

        this.tokenRefreshFailureCounter = Counter.builder("auth.token.refresh.failure")
            .description("Failed token refresh attempts")
            .register(registry);

        // 初始化计时器
        this.loginTimer = Timer.builder("auth.login.duration")
            .description("Login duration")
            .tag("type", "all")
            .publishPercentiles(0.5, 0.95, 0.99)
            .publishPercentileHistogram()
            .register(registry);
    }

    /**
     * 记录登录成功
     */
    public void recordLoginSuccess(String type) {
        Counter.builder("auth.login.success")
            .description("Successful login attempts")
            .tag("type", type)
            .register(registry)
            .increment();
    }

    /**
     * 记录登录失败
     */
    public void recordLoginFailure(String reason) {
        Counter.builder("auth.login.failure")
            .description("Failed login attempts")
            .tag("reason", reason)
            .register(registry)
            .increment();
    }

    /**
     * 记录MFA验证成功
     */
    public void recordMfaSuccess(String type) {
        Counter.builder("auth.mfa.success")
            .description("Successful MFA verifications")
            .tag("type", type)
            .register(registry)
            .increment();
    }

    /**
     * 记录MFA验证失败
     */
    public void recordMfaFailure(String type) {
        Counter.builder("auth.mfa.failure")
            .description("Failed MFA verifications")
            .tag("type", type)
            .register(registry)
            .increment();
    }

    /**
     * 记录登出
     */
    public void recordLogout() {
        logoutCounter.increment();
    }

    /**
     * 记录密码重置
     */
    public void recordPasswordReset() {
        passwordResetCounter.increment();
    }

    /**
     * 记录Token刷新
     */
    public void recordTokenRefresh() {
        tokenRefreshCounter.increment();
    }

    /**
     * 记录Token刷新失败
     */
    public void recordTokenRefreshFailure(String reason) {
        Counter.builder("auth.token.refresh.failure")
            .description("Failed token refresh attempts")
            .tag("reason", reason)
            .register(registry)
            .increment();
    }

    /**
     * 记录登录耗时
     */
    public void recordLoginDuration(long durationMs, String type) {
        Timer.builder("auth.login.duration")
            .description("Login duration")
            .tag("type", type)
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(registry)
            .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 记录自定义指标
     */
    public void recordCustomMetric(String name, double value, String... tags) {
        registry.gauge(name, tags, value);
    }
}
