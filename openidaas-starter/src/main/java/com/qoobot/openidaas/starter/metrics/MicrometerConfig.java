package com.qoobot.openidaas.starter.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Micrometer 监控配置
 *
 * <p>配置 Prometheus 指标收集和自定义指标</p>
 *
 * @author OpenIDaaS Team
 * @since 1.0.0
 */
@AutoConfiguration(after = CompositeMeterRegistryAutoConfiguration.class)
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnProperty(
    prefix = "openidaas.monitoring",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(MetricsProperties.class)
public class MicrometerConfig {

    /**
     * 配置 JVM 内存指标
     */
    @Bean
    @ConditionalOnClass(JvmMemoryMetrics.class)
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    /**
     * 配置 JVM GC 指标
     */
    @Bean
    @ConditionalOnClass(JvmGcMetrics.class)
    public JvmGcMetrics jvmGcMetrics() {
        return new JvmGcMetrics();
    }

    /**
     * 配置处理器指标
     */
    @Bean
    @ConditionalOnClass(ProcessorMetrics.class)
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }

    /**
     * 配置运行时间指标
     */
    @Bean
    @ConditionalOnClass(UptimeMetrics.class)
    public UptimeMetrics uptimeMetrics() {
        return new UptimeMetrics();
    }

    /**
     * 认证指标收集器
     */
    @Bean
    public AuthMetrics authMetrics(MeterRegistry registry) {
        return new AuthMetrics(registry);
    }

    /**
     * Token 指标收集器
     */
    @Bean
    public TokenMetrics tokenMetrics(MeterRegistry registry) {
        return new TokenMetrics(registry);
    }

    /**
     * 安全指标收集器
     */
    @Bean
    public SecurityMetrics securityMetrics(MeterRegistry registry) {
        return new SecurityMetrics(registry);
    }

    /**
     * 租户指标收集器
     */
    @Bean
    public TenantMetrics tenantMetrics(MeterRegistry registry) {
        return new TenantMetrics(registry);
    }

    /**
     * 性能指标收集器
     */
    @Bean
    public PerformanceMetrics performanceMetrics(MeterRegistry registry) {
        return new PerformanceMetrics(registry);
    }
}
