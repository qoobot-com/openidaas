package com.qoobot.openidaas.common.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTagsProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Micrometer监控配置
 *
 * @author QooBot
 */
@Configuration
public class MetricsConfig {

    /**
     * 自定义Metrics注册器
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
            "application", "openidaas"
        );
    }

    /**
     * JVM内存指标
     */
    @Bean
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    /**
     * JVM线程指标
     */
    @Bean
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }

    /**
     * CPU指标
     */
    @Bean
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }

    /**
     * 运行时间指标
     */
    @Bean
    public UptimeMetrics uptimeMetrics() {
        return new UptimeMetrics();
    }

    /**
     * 自定义Web MVC标签
     */
    @Bean
    public WebMvcTagsProvider webMvcTagsProvider() {
        return (request, response, exception) -> {
            // 默认标签
            io.micrometer.core.instrument.Tags tags = io.micrometer.core.instrument.Tags.of(
                "uri", request.getRequestURI(),
                "method", request.getMethod(),
                "status", String.valueOf(response.getStatus())
            );

            // 添加异常标签
            if (exception != null) {
                tags = tags.and("exception", exception.getClass().getSimpleName());
            }

            return tags;
        };
    }
}
