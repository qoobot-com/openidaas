package com.qoobot.openidaas.starter.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 安全指标收集器
 *
 * <p>收集安全相关的业务指标</p>
 *
 * @author OpenIDaaS Team
 * @since 1.0.0
 */
@Slf4j
public class SecurityMetrics {

    private final MeterRegistry registry;

    // 异常访问计数器
    private final Counter anomalousAccessCounter;

    // 权限拒绝计数器
    private final Counter authorizationDeniedCounter;

    // 账户锁定计数器
    private final Counter accountLockedCounter;
    private final Counter accountUnlockedCounter;

    // MFA 相关计数器
    private final Counter mfaVerificationSuccessCounter;
    private final Counter mfaVerificationFailureCounter;
    private final Counter mfaEnabledCounter;
    private final Counter mfaDisabledCounter;

    // 密码相关计数器
    private final Counter passwordChangeCounter;
    private final Counter passwordResetRequestCounter;
    private final Counter passwordResetSuccessCounter;
    private final Counter passwordResetFailureCounter;
    private final Counter passwordExpiryCounter;

    // 会话劫持检测
    private final Counter sessionHijackDetectedCounter;

    // 安全事件计数器
    private final Counter securityEventCounter;

    // 安全响应时间
    private final Timer securityCheckTimer;

    // 锁定账户数
    private final AtomicLong lockedAccountsCount;

    public SecurityMetrics(MeterRegistry registry) {
        this.registry = registry;

        // 异常访问计数器
        this.anomalousAccessCounter = Counter.builder("anomalous_access_total")
                .description("Total anomalous access attempts detected")
                .register(registry);

        // 权限拒绝计数器
        this.authorizationDeniedCounter = Counter.builder("authorization_denied_total")
                .description("Total authorization denials")
                .register(registry);

        // 账户锁定计数器
        this.accountLockedCounter = Counter.builder("account_locked_total")
                .description("Total accounts locked")
                .register(registry);

        this.accountUnlockedCounter = Counter.builder("account_unlocked_total")
                .description("Total accounts unlocked")
                .register(registry);

        // MFA 相关计数器
        this.mfaVerificationSuccessCounter = Counter.builder("mfa_verification_success_total")
                .description("Total successful MFA verifications")
                .register(registry);

        this.mfaVerificationFailureCounter = Counter.builder("mfa_verification_failure_total")
                .description("Total failed MFA verifications")
                .register(registry);

        this.mfaEnabledCounter = Counter.builder("mfa_enabled_total")
                .description("Total MFA enabled")
                .register(registry);

        this.mfaDisabledCounter = Counter.builder("mfa_disabled_total")
                .description("Total MFA disabled")
                .register(registry);

        // 密码相关计数器
        this.passwordChangeCounter = Counter.builder("password_change_total")
                .description("Total password changes")
                .register(registry);

        this.passwordResetRequestCounter = Counter.builder("password_reset_request_total")
                .description("Total password reset requests")
                .register(registry);

        this.passwordResetSuccessCounter = Counter.builder("password_reset_success_total")
                .description("Total successful password resets")
                .register(registry);

        this.passwordResetFailureCounter = Counter.builder("password_reset_failure_total")
                .description("Total failed password resets")
                .register(registry);

        this.passwordExpiryCounter = Counter.builder("password_expiry_total")
                .description("Total password expirations")
                .register(registry);

        // 会话劫持检测
        this.sessionHijackDetectedCounter = Counter.builder("session_hijack_detected_total")
                .description("Total session hijack attempts detected")
                .register(registry);

        // 安全事件计数器
        this.securityEventCounter = Counter.builder("security_event_total")
                .description("Total security events")
                .register(registry);

        // 安全响应时间
        this.securityCheckTimer = Timer.builder("security_check_time_seconds")
                .description("Security check response time")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);

        // 锁定账户数
        this.lockedAccountsCount = new AtomicLong(0);
        Gauge.builder("security_locked_accounts_total", lockedAccountsCount, AtomicLong::get)
                .description("Number of locked accounts")
                .register(registry);
    }

    /**
     * 记录异常访问
     *
     * @param type 异常类型
     * @param userId 用户ID
     */
    public void recordAnomalousAccess(String type, String userId) {
        Counter.builder("anomalous_access_total")
                .description("Total anomalous access attempts detected")
                .tags("type", type, "user_id", userId)
                .register(registry)
                .increment();
        anomalousAccessCounter.increment();
    }

    /**
     * 记录权限拒绝
     *
     * @param resource 资源
     * @param action 操作
     */
    public void recordAuthorizationDenied(String resource, String action) {
        Counter.builder("authorization_denied_total")
                .description("Total authorization denials")
                .tags("resource", resource, "action", action)
                .register(registry)
                .increment();
        authorizationDeniedCounter.increment();
    }

    /**
     * 记录账户锁定
     *
     * @param userId 用户ID
     * @param reason 锁定原因
     */
    public void recordAccountLocked(String userId, String reason) {
        Counter.builder("account_locked_total")
                .description("Total accounts locked")
                .tags("reason", reason)
                .register(registry)
                .increment();
        accountLockedCounter.increment();
        lockedAccountsCount.incrementAndGet();
    }

    /**
     * 记录账户解锁
     *
     * @param userId 用户ID
     */
    public void recordAccountUnlocked(String userId) {
        accountUnlockedCounter.increment();
        lockedAccountsCount.decrementAndGet();
    }

    /**
     * 记录 MFA 验证成功
     *
     * @param type MFA 类型
     */
    public void recordMfaVerificationSuccess(String type) {
        Counter.builder("mfa_verification_success_total")
                .description("Total successful MFA verifications")
                .tags("type", type)
                .register(registry)
                .increment();
        mfaVerificationSuccessCounter.increment();
    }

    /**
     * 记录 MFA 验证失败
     *
     * @param type MFA 类型
     * @param reason 失败原因
     */
    public void recordMfaVerificationFailure(String type, String reason) {
        Counter.builder("mfa_verification_failure_total")
                .description("Total failed MFA verifications")
                .tags("type", type, "reason", reason)
                .register(registry)
                .increment();
        mfaVerificationFailureCounter.increment();
    }

    /**
     * 记录 MFA 启用
     */
    public void recordMfaEnabled(String type) {
        Counter.builder("mfa_enabled_total")
                .description("Total MFA enabled")
                .tags("type", type)
                .register(registry)
                .increment();
        mfaEnabledCounter.increment();
    }

    /**
     * 记录 MFA 禁用
     */
    public void recordMfaDisabled() {
        mfaDisabledCounter.increment();
    }

    /**
     * 记录密码修改
     */
    public void recordPasswordChange() {
        passwordChangeCounter.increment();
    }

    /**
     * 记录密码重置请求
     */
    public void recordPasswordResetRequest() {
        passwordResetRequestCounter.increment();
    }

    /**
     * 记录密码重置成功
     */
    public void recordPasswordResetSuccess() {
        passwordResetSuccessCounter.increment();
    }

    /**
     * 记录密码重置失败
     *
     * @param reason 失败原因
     */
    public void recordPasswordResetFailure(String reason) {
        Counter.builder("password_reset_failure_total")
                .description("Total failed password resets")
                .tags("reason", reason)
                .register(registry)
                .increment();
        passwordResetFailureCounter.increment();
    }

    /**
     * 记录密码过期
     */
    public void recordPasswordExpiry() {
        passwordExpiryCounter.increment();
    }

    /**
     * 记录会话劫持检测
     *
     * @param userId 用户ID
     */
    public void recordSessionHijackDetected(String userId) {
        Counter.builder("session_hijack_detected_total")
                .description("Total session hijack attempts detected")
                .tags("user_id", userId)
                .register(registry)
                .increment();
        sessionHijackDetectedCounter.increment();
    }

    /**
     * 记录安全事件
     *
     * @param type 事件类型
     * @param severity 严重程度
     */
    public void recordSecurityEvent(String type, String severity) {
        Counter.builder("security_event_total")
                .description("Total security events")
                .tags("type", type, "severity", severity)
                .register(registry)
                .increment();
        securityEventCounter.increment();
    }

    /**
     * 记录安全检查时间
     *
     * @param startTime 开始时间（纳秒）
     */
    public void recordSecurityCheckTime(long startTime) {
        long duration = System.nanoTime() - startTime;
        securityCheckTimer.record(duration, java.util.concurrent.TimeUnit.NANOSECONDS);
    }

    /**
     * 设置锁定账户数
     *
     * @param count 账户数
     */
    public void setLockedAccountsCount(long count) {
        lockedAccountsCount.set(count);
    }

    /**
     * 获取 MFA 验证成功率
     *
     * @return 成功率（百分比）
     */
    public double getMfaVerificationSuccessRate() {
        long total = (long) mfaVerificationSuccessCounter.count() + (long) mfaVerificationFailureCounter.count();
        if (total == 0) {
            return 0.0;
        }
        return (mfaVerificationSuccessCounter.count() / total) * 100;
    }

    /**
     * 获取密码重置成功率
     *
     * @return 成功率（百分比）
     */
    public double getPasswordResetSuccessRate() {
        long total = (long) passwordResetRequestCounter.count();
        if (total == 0) {
            return 0.0;
        }
        return (passwordResetSuccessCounter.count() / total) * 100;
    }
}
