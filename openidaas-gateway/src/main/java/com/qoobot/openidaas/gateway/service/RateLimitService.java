package com.qoobot.openidaas.gateway.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流服务
 * 
 * 基于Bucket4j实现分布式限流
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    // 本地缓存（简化实现，生产环境应使用Redis分布式Bucket）
    private final ConcurrentHashMap<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    /**
     * 尝试获取令牌
     * 
     * @param key 限流key
     * @param scope 限流范围
     * @param capacity 桶容量
     * @param refillDuration 补充时间间隔
     * @param refillTokens 补充令牌数
     * @return 是否获取成功
     */
    public boolean tryAcquire(
            String key,
            String scope,
            long capacity,
            Duration refillDuration,
            long refillTokens) {

        String bucketKey = buildBucketKey(key, scope);
        Bucket bucket = getBucket(bucketKey, capacity, refillDuration, refillTokens);

        return bucket.tryConsume(1);
    }

    /**
     * 尝试获取多个令牌
     * 
     * @param key 限流key
     * @param scope 限流范围
     * @param tokens 需要的令牌数
     * @param capacity 桶容量
     * @param refillDuration 补充时间间隔
     * @param refillTokens 补充令牌数
     * @return 是否获取成功
     */
    public boolean tryAcquire(
            String key,
            String scope,
            long tokens,
            long capacity,
            Duration refillDuration,
            long refillTokens) {

        String bucketKey = buildBucketKey(key, scope);
        Bucket bucket = getBucket(bucketKey, capacity, refillDuration, refillTokens);

        return bucket.tryConsume(tokens);
    }

    /**
     * 获取令牌桶
     */
    private Bucket getBucket(
            String bucketKey,
            long capacity,
            Duration refillDuration,
            long refillTokens) {

        return bucketCache.computeIfAbsent(bucketKey, k -> 
                Bucket.builder()
                        .addLimit(Bandwidth.classic(
                                capacity,
                                Refill.greedy(refillTokens, refillDuration)
                        ))
                        .build()
        );
    }

    /**
     * 获取剩余令牌数
     * 
     * @param key 限流key
     * @param scope 限流范围
     * @return 剩余令牌数
     */
    public long getRemainingTokens(String key, String scope) {
        String bucketKey = buildBucketKey(key, scope);
        Bucket bucket = bucketCache.get(bucketKey);
        return bucket != null ? bucket.getAvailableTokens() : 0;
    }

    /**
     * 重置限流
     * 
     * @param key 限流key
     * @param scope 限流范围
     */
    public void reset(String key, String scope) {
        String bucketKey = buildBucketKey(key, scope);
        bucketCache.remove(bucketKey);
        log.debug("Reset rate limit for key: {}", bucketKey);
    }

    /**
     * 构建桶Key
     */
    private String buildBucketKey(String key, String scope) {
        return String.format("rate_limit:%s:%s", scope, key);
    }
}
