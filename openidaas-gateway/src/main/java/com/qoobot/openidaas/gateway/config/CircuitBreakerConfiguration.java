package com.qoobot.openidaas.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 熔断器配置
 * 
 * 基于Resilience4j实现熔断机制
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Configuration
public class CircuitBreakerConfiguration {

    /**
     * 自定义熔断器配置
     */
    @Bean
    public ReactiveResilience4JCircuitBreakerFactory customCircuitBreakerFactory() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // 失败率阈值50%
                .waitDurationInOpenState(Duration.ofSeconds(30)) // 熔断开启持续时间30秒
                .permittedNumberOfCallsInHalfOpenState(5) // 半开状态允许5次调用
                .slidingWindowType(io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                .slidingWindowSize(60) // 滑动窗口60秒
                .minimumNumberOfCalls(10) // 最小调用次数10次
                .slowCallRateThreshold(100) // 慢调用率阈值100%
                .slowCallDurationThreshold(Duration.ofSeconds(3)) // 慢调用阈值3秒
                .build();

        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5)) // 超时时间5秒
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(circuitBreakerConfig);

        ReactiveResilience4JCircuitBreakerFactory factory = new ReactiveResilience4JCircuitBreakerFactory();
        factory.configureCircuitBreakerRegistry(registry);

        // 为不同服务配置不同的熔断器
        factory.configure(builder -> builder
                .circuitBreakerConfig(circuitBreakerConfig)
                .timeLimiterConfig(timeLimiterConfig), "user-service-cb");

        factory.configure(builder -> builder
                .circuitBreakerConfig(circuitBreakerConfig)
                .timeLimiterConfig(timeLimiterConfig), "auth-service-cb");

        factory.configure(builder -> builder
                .circuitBreakerConfig(circuitBreakerConfig)
                .timeLimiterConfig(timeLimiterConfig), "tenant-service-cb");

        factory.configure(builder -> builder
                .circuitBreakerConfig(circuitBreakerConfig)
                .timeLimiterConfig(timeLimiterConfig), "security-service-cb");

        return factory;
    }
}
