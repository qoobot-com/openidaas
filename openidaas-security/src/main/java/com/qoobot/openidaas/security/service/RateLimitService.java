package com.qoobot.openidaas.security.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * 限流服务
 * 
 * 基于Bucket4j实现分布式限流，支持令牌桶算法
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;

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

        // 简化实现：使用本地Bucket，生产环境应使用分布式Bucket
        // TODO: 集成Redis分布式Bucket
        
        Supplier<BucketConfiguration> configSupplier = getConfigSupplier(
                capacity,
                refillDuration,
                refillTokens
        );

        return Bucket.builder()
                .addLimit(Bandwidth.classic(
                        capacity,
                        Refill.greedy(refillTokens, refillDuration)
                ))
                .build();
    }

    /**
     * 获取配置
     */
    private Supplier<BucketConfiguration> getConfigSupplier(
            long capacity,
            Duration refillDuration,
            long refillTokens) {

        return () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(
                        capacity,
                        Refill.greedy(refillTokens, refillDuration)
                ))
                .build();
    }

    /**
     * 构建桶Key
     */
    private String buildBucketKey(String key, String scope) {
        return String.format("rate_limit:%s:%s", scope, key);
    }

    /**
     * 重置限流
     */
    public void reset(String key, String scope) {
        String bucketKey = buildBucketKey(key, scope);
        redisTemplate.delete(bucketKey);
        log.debug("Reset rate limit for key: {}", bucketKey);
    }

    /**
     * 获取剩余令牌数
     */
    public long getAvailableTokens(
            String key,
            String scope,
            long capacity,
            Duration refillDuration,
            long refillTokens) {

        String bucketKey = buildBucketKey(key, scope);
        Bucket bucket = getBucket(bucketKey, capacity, refillDuration, refillTokens);
        return bucket.getAvailableTokens();
    }
}
