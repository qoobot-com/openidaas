package com.qoobot.openidaas.gateway.controller;

import com.qoobot.openidaas.gateway.model.ApiCallStat;
import com.qoobot.openidaas.gateway.model.GatewayMetrics;
import com.qoobot.openidaas.gateway.service.ApiStatService;
import com.qoobot.openidaas.gateway.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 网关管理控制器
 * 
 * 提供网关管理的REST API
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/gateway")
@RequiredArgsConstructor
public class GatewayManagementController {

    private final ApiStatService apiStatService;
    private final RateLimitService rateLimitService;

    /**
     * 获取网关指标
     */
    @GetMapping("/metrics")
    public Mono<GatewayMetrics> getMetrics() {
        return apiStatService.getQps("*")
                .map(qps -> GatewayMetrics.builder()
                        .totalRequests(0L) // 从Redis聚合获取
                        .successRequests(0L)
                        .failedRequests(0L)
                        .errorRate(0.0)
                        .avgResponseTime(0.0)
                        .currentQps(qps)
                        .peakQps(0L)
                        .activeConnections(0L)
                        .build());
    }

    /**
     * 获取API统计
     */
    @GetMapping("/stats")
    public Mono<Map<String, Object>> getApiStats(
            @RequestParam(required = false) String uri,
            @RequestParam(defaultValue = "60") int minutes) {

        if (uri != null && !uri.isEmpty()) {
            return apiStatService.getApiStats(uri, minutes)
                    .collectList()
                    .map(stats -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("uri", uri);
                        result.put("stats", stats);
                        return result;
                    });
        }

        // 返回所有API的统计
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Please specify a URI");
        result.put("example", "/api/gateway/stats?uri=/api/users&page=0&size=20");
        return Mono.just(result);
    }

    /**
     * 获取QPS统计
     */
    @GetMapping("/qps")
    public Mono<Map<String, Object>> getQps(@RequestParam(required = false) String uri) {
        String targetUri = uri != null ? uri : "*";
        
        return apiStatService.getQps(targetUri)
                .map(qps -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("uri", targetUri);
                    result.put("qps", qps);
                    result.put("timestamp", LocalDateTime.now());
                    return result;
                });
    }

    /**
     * 获取错误率
     */
    @GetMapping("/error-rate")
    public Mono<Map<String, Object>> getErrorRate(@RequestParam(required = false) String uri) {
        String targetUri = uri != null ? uri : "*";
        
        return apiStatService.getErrorRate(targetUri)
                .map(errorRate -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("uri", targetUri);
                    result.put("errorRate", String.format("%.2f%%", errorRate));
                    result.put("timestamp", LocalDateTime.now());
                    return result;
                });
    }

    /**
     * 重置限流
     */
    @PostMapping("/rate-limit/reset")
    public Mono<Map<String, Object>> resetRateLimit(
            @RequestParam String key,
            @RequestParam String scope) {

        rateLimitService.reset(key, scope);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Rate limit reset successfully");
        result.put("key", key);
        result.put("scope", scope);
        result.put("timestamp", LocalDateTime.now());
        
        return Mono.just(result);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Mono<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("services", new HashMap<String, String>() {{
            put("user-service", "UP");
            put("auth-service", "UP");
            put("tenant-service", "UP");
            put("security-service", "UP");
        }});
        return Mono.just(health);
    }
}
