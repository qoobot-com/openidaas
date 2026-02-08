package com.qoobot.openidaas.gateway.service;

import com.qoobot.openidaas.gateway.model.ApiCallStat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * API统计服务
 * 
 * 记录API调用统计信息，包括QPS、响应时间、错误率等
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiStatService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    private static final String API_STAT_PREFIX = "api_stat:";
    private static final String API_QPS_PREFIX = "api_qps:";
    private static final String API_ERROR_PREFIX = "api_error:";

    /**
     * 记录API调用
     * 
     * @param stat 统计信息
     */
    public Mono<Void> recordApiCall(ApiCallStat stat) {
        String key = buildStatKey(stat.getUri());
        String dateKey = getDateKey();

        Map<String, Object> data = new HashMap<>();
        data.put("uri", stat.getUri());
        data.put("method", stat.getMethod());
        data.put("statusCode", stat.getStatusCode());
        data.put("duration", stat.getDuration());
        data.put("clientIp", stat.getClientIp());
        data.put("timestamp", stat.getTimestamp());
        data.put("userId", stat.getUserId());
        data.put("requestId", stat.getRequestId());

        return redisTemplate.opsForHash()
                .put(key, dateKey, data)
                .then(redisTemplate.expire(key, Duration.ofDays(7)).then())
                .doOnSuccess(v -> log.debug("Recorded API call stat: {}", stat.getUri()));
    }

    /**
     * 增加QPS计数
     * 
     * @param uri URI
     */
    public Mono<Void> incrementQps(String uri) {
        String key = buildQpsKey(uri);
        String minuteKey = getMinuteKey();
        
        return redisTemplate.opsForHash()
                .increment(key, minuteKey, 1)
                .then(redisTemplate.expire(key, Duration.ofHours(1)).then())
                .doOnSuccess(v -> log.debug("Incremented QPS for: {}", uri));
    }

    /**
     * 增加错误计数
     * 
     * @param uri URI
     * @param statusCode 状态码
     */
    public Mono<Void> incrementError(String uri, int statusCode) {
        String key = buildErrorKey(uri);
        String minuteKey = getMinuteKey();
        String errorKey = minuteKey + ":" + statusCode;
        
        return redisTemplate.opsForHash()
                .increment(key, errorKey, 1)
                .then(redisTemplate.expire(key, Duration.ofHours(1)).then())
                .doOnSuccess(v -> log.debug("Incremented error for: {}, status: {}", uri, statusCode));
    }

    /**
     * 获取QPS统计
     * 
     * @param uri URI
     * @return QPS统计
     */
    public Mono<Long> getQps(String uri) {
        String key = buildQpsKey(uri);
        String currentMinute = getMinuteKey();
        
        return redisTemplate.opsForHash()
                .get(key, currentMinute)
                .map(obj -> obj != null ? Long.parseLong(obj.toString()) : 0L)
                .defaultIfEmpty(0L);
    }

    /**
     * 获取错误率
     * 
     * @param uri URI
     * @return 错误率（百分比）
     */
    public Mono<Double> getErrorRate(String uri) {
        String qpsKey = buildQpsKey(uri);
        String errorKey = buildErrorKey(uri);
        String currentMinute = getMinuteKey();
        
        Mono<Long> qpsMono = redisTemplate.opsForHash()
                .get(qpsKey, currentMinute)
                .map(obj -> obj != null ? Long.parseLong(obj.toString()) : 0L)
                .defaultIfEmpty(0L);
        
        Mono<Long> errorMono = redisTemplate.opsForHash()
                .get(errorKey, currentMinute)
                .map(obj -> obj != null ? Long.parseLong(obj.toString()) : 0L)
                .defaultIfEmpty(0L);
        
        return Mono.zip(qpsMono, errorMono, (qps, errors) -> {
            return qps > 0 ? (errors * 100.0 / qps) : 0.0;
        });
    }

    /**
     * 获取API调用统计
     * 
     * @param uri URI
     * @param minutes 最近N分钟
     * @return 统计列表
     */
    public Flux<Map<String, Object>> getApiStats(String uri, int minutes) {
        String key = buildStatKey(uri);
        
        return redisTemplate.opsForHash()
                .values(key)
                .map(obj -> (Map<String, Object>) obj)
                .take(minutes);
    }

    /**
     * 清理过期数据
     * 
     * @param days 保留天数
     */
    public Mono<Void> cleanupOldData(int days) {
        // Redis会自动清理过期key，这里可以添加额外的清理逻辑
        return Mono.empty();
    }

    /**
     * 构建统计Key
     */
    private String buildStatKey(String uri) {
        return API_STAT_PREFIX + uri.replaceAll("/", "_");
    }

    /**
     * 构建QPS Key
     */
    private String buildQpsKey(String uri) {
        return API_QPS_PREFIX + uri.replaceAll("/", "_");
    }

    /**
     * 构建错误Key
     */
    private String buildErrorKey(String uri) {
        return API_ERROR_PREFIX + uri.replaceAll("/", "_");
    }

    /**
     * 获取日期Key
     */
    private String getDateKey() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * 获取分钟Key
     */
    private String getMinuteKey() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"));
    }
}
