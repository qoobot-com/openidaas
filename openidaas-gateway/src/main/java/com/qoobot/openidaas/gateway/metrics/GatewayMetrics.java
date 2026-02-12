package com.qoobot.openidaas.gateway.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 网关指标收集器
 *
 * @author QooBot
 */
@Component
public class GatewayMetrics {

    private final MeterRegistry meterRegistry;
    
    // 请求计数器
    private final Counter totalRequests;
    private final Counter successfulRequests;
    private final Counter failedRequests;
    
    // 认证相关计数器
    private final Counter authSuccess;
    private final Counter authFailed;
    
    // 限流相关计数器
    private final Counter rateLimitedRequests;

    @Autowired
    public GatewayMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // 初始化计数器
        this.totalRequests = Counter.builder("gateway.requests.total")
                .description("Total number of requests")
                .register(meterRegistry);
                
        this.successfulRequests = Counter.builder("gateway.requests.successful")
                .description("Number of successful requests")
                .register(meterRegistry);
                
        this.failedRequests = Counter.builder("gateway.requests.failed")
                .description("Number of failed requests")
                .register(meterRegistry);
                
        this.authSuccess = Counter.builder("gateway.auth.success")
                .description("Number of successful authentications")
                .register(meterRegistry);
                
        this.authFailed = Counter.builder("gateway.auth.failed")
                .description("Number of failed authentications")
                .register(meterRegistry);
                
        this.rateLimitedRequests = Counter.builder("gateway.requests.ratelimited")
                .description("Number of rate limited requests")
                .register(meterRegistry);
    }

    /**
     * 记录总请求数
     */
    public void recordTotalRequest() {
        totalRequests.increment();
    }

    /**
     * 记录成功请求
     */
    public void recordSuccessfulRequest() {
        successfulRequests.increment();
    }

    /**
     * 记录失败请求
     */
    public void recordFailedRequest() {
        failedRequests.increment();
    }

    /**
     * 记录认证成功
     */
    public void recordAuthSuccess() {
        authSuccess.increment();
    }

    /**
     * 记录认证失败
     */
    public void recordAuthFailed() {
        authFailed.increment();
    }

    /**
     * 记录限流请求
     */
    public void recordRateLimitedRequest() {
        rateLimitedRequests.increment();
    }

    /**
     * 记录请求处理时间
     */
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * 停止计时器并记录时间
     */
    public void stopTimer(Timer.Sample sample, String uri, String method, String status) {
        sample.stop(Timer.builder("gateway.request.duration")
                .tag("uri", uri)
                .tag("method", method)
                .tag("status", status)
                .register(meterRegistry));
    }
}