package com.qoobot.openidaas.gateway.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 网关指标
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
@Builder
public class GatewayMetrics {

    /**
     * 总请求数
     */
    private Long totalRequests;

    /**
     * 成功请求数
     */
    private Long successRequests;

    /**
     * 失败请求数
     */
    private Long failedRequests;

    /**
     * 错误率（百分比）
     */
    private Double errorRate;

    /**
     * 平均响应时间（毫秒）
     */
    private Double avgResponseTime;

    /**
     * P50响应时间（毫秒）
     */
    private Long p50ResponseTime;

    /**
     * P95响应时间（毫秒）
     */
    private Long p95ResponseTime;

    /**
     * P99响应时间（毫秒）
     */
    private Long p99ResponseTime;

    /**
     * 当前QPS
     */
    private Long currentQps;

    /**
     * 峰值QPS
     */
    private Long peakQps;

    /**
     * 在线连接数
     */
    private Long activeConnections;

    /**
     * API统计
     */
    private Map<String, ApiStats> apiStats;

    /**
     * API统计
     */
    @Data
    @Builder
    public static class ApiStats {
        /**
         * URI
         */
        private String uri;

        /**
         * 请求次数
         */
        private Long requestCount;

        /**
         * 平均响应时间
         */
        private Double avgResponseTime;

        /**
         * 错误率
         */
        private Double errorRate;

        /**
         * 当前QPS
         */
        private Long currentQps;
    }
}
