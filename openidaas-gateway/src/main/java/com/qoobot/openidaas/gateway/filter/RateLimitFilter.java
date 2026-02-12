package com.qoobot.openidaas.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 增强版限流过滤器
 * 支持多种限流算法：令牌桶、漏桶、固定窗口、滑动窗口
 *
 * @author QooBot
 */
@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;
    
    @Value("${gateway.rate-limit.algorithm:token-bucket}")
    private String algorithm;
    
    @Value("${gateway.rate-limit.requests-per-second:10}")
    private int requestsPerSecond;
    
    @Value("${gateway.rate-limit.burst-capacity:20}")
    private int burstCapacity;
    
    @Value("${gateway.rate-limit.window-size-seconds:60}")
    private int windowSizeSeconds;
    
    @Value("${gateway.rate-limit.whitelist-clients:}")
    private List<String> whitelistClients;
    
    private static final String TOKEN_BUCKET_KEY_PREFIX = "rate_limit:token_bucket:";
    private static final String LEAKY_BUCKET_KEY_PREFIX = "rate_limit:leaky_bucket:";
    private static final String FIXED_WINDOW_KEY_PREFIX = "rate_limit:fixed_window:";
    private static final String SLIDING_WINDOW_KEY_PREFIX = "rate_limit:sliding_window:";
    
    private static final List<String> SUPPORTED_ALGORITHMS = Arrays.asList(
        "token-bucket", "leaky-bucket", "fixed-window", "sliding-window"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String clientId = getClientIdentifier(request);
        
        // 检查白名单
        if (whitelistClients.contains(clientId)) {
            log.debug("Client {} is in whitelist, bypassing rate limit", clientId);
            return chain.filter(exchange);
        }
        
        // 验证算法
        if (!SUPPORTED_ALGORITHMS.contains(algorithm)) {
            log.warn("Unsupported rate limiting algorithm: {}, using token-bucket as fallback", algorithm);
            algorithm = "token-bucket";
        }
        
        switch (algorithm) {
            case "token-bucket":
                return handleTokenBucketRateLimit(exchange, chain, clientId);
            case "leaky-bucket":
                return handleLeakyBucketRateLimit(exchange, chain, clientId);
            case "fixed-window":
                return handleFixedWindowRateLimit(exchange, chain, clientId);
            case "sliding-window":
                return handleSlidingWindowRateLimit(exchange, chain, clientId);
            default:
                return handleTokenBucketRateLimit(exchange, chain, clientId);
        }
    }

    /**
     * 令牌桶算法限流
     */
    private Mono<Void> handleTokenBucketRateLimit(ServerWebExchange exchange, GatewayFilterChain chain, String clientId) {
        String key = TOKEN_BUCKET_KEY_PREFIX + clientId;
        ReactiveValueOperations<String, String> ops = redisTemplate.opsForValue();
        
        return ops.get(key)
                .switchIfEmpty(Mono.just(burstCapacity + ":" + Instant.now().getEpochSecond()))
                .flatMap(value -> {
                    String[] parts = value.split(":");
                    int tokens = Integer.parseInt(parts[0]);
                    long lastRefillTime = Long.parseLong(parts[1]);
                    
                    // 计算应该补充的令牌数
                    long currentTime = Instant.now().getEpochSecond();
                    long timePassed = currentTime - lastRefillTime;
                    int tokensToAdd = (int) (timePassed * requestsPerSecond);
                    
                    // 补充令牌，但不超过桶容量
                    tokens = Math.min(burstCapacity, tokens + tokensToAdd);
                    
                    if (tokens > 0) {
                        // 消耗一个令牌
                        tokens--;
                        String newValue = tokens + ":" + currentTime;
                        return ops.set(key, newValue, Duration.ofHours(1))
                                .then(chain.filter(exchange));
                    } else {
                        // 没有令牌可用
                        return handleRateLimitExceeded(exchange, clientId, "Token bucket empty");
                    }
                });
    }

    /**
     * 漏桶算法限流
     */
    private Mono<Void> handleLeakyBucketRateLimit(ServerWebExchange exchange, GatewayFilterChain chain, String clientId) {
        String key = LEAKY_BUCKET_KEY_PREFIX + clientId;
        ReactiveValueOperations<String, String> ops = redisTemplate.opsForValue();
        
        return ops.get(key)
                .switchIfEmpty(Mono.just("0:" + Instant.now().getEpochSecond()))
                .flatMap(value -> {
                    String[] parts = value.split(":");
                    double waterLevel = Double.parseDouble(parts[0]);
                    long lastLeakTime = Long.parseLong(parts[1]);
                    
                    long currentTime = Instant.now().getEpochSecond();
                    double timePassed = currentTime - lastLeakTime;
                    
                    // 漏水：waterLevel -= leakRate * timePassed
                    double leakAmount = (requestsPerSecond / (double) windowSizeSeconds) * timePassed;
                    waterLevel = Math.max(0, waterLevel - leakAmount);
                    
                    if (waterLevel < burstCapacity) {
                        // 水桶未满，可以加水
                        waterLevel += 1.0;
                        String newValue = waterLevel + ":" + currentTime;
                        return ops.set(key, newValue, Duration.ofHours(1))
                                .then(chain.filter(exchange));
                    } else {
                        // 水桶已满，拒绝请求
                        return handleRateLimitExceeded(exchange, clientId, "Leaky bucket overflow");
                    }
                });
    }

    /**
     * 固定窗口算法限流
     */
    private Mono<Void> handleFixedWindowRateLimit(ServerWebExchange exchange, GatewayFilterChain chain, String clientId) {
        long currentWindow = Instant.now().getEpochSecond() / windowSizeSeconds;
        String key = FIXED_WINDOW_KEY_PREFIX + clientId + ":" + currentWindow;
        ReactiveValueOperations<String, String> ops = redisTemplate.opsForValue();
        
        return ops.increment(key, 1)
                .cast(Long.class) // 明确类型转换
                .flatMap(count -> {
                    if (count == 1) {
                        // 使用expire命令设置过期时间
                        return redisTemplate.expire(key, Duration.ofSeconds(windowSizeSeconds))
                                .thenReturn(count);
                    }
                    return Mono.just(count);
                })
                .flatMap(count -> {
                    if (count > requestsPerSecond * windowSizeSeconds) {
                        return handleRateLimitExceeded(exchange, clientId, "Fixed window limit exceeded");
                    }
                    return chain.filter(exchange);
                });
    }

    /**
     * 滑动窗口算法限流
     */
    private Mono<Void> handleSlidingWindowRateLimit(ServerWebExchange exchange, GatewayFilterChain chain, String clientId) {
        long currentTime = Instant.now().getEpochSecond();
        String key = SLIDING_WINDOW_KEY_PREFIX + clientId;
        ReactiveValueOperations<String, String> ops = redisTemplate.opsForValue();
        
        return ops.get(key)
                .defaultIfEmpty("")
                .flatMap(existingRequests -> {
                    // 解析现有的请求时间戳
                    String[] timestamps = existingRequests.isEmpty() ? new String[0] : existingRequests.split(",");
                    StringBuilder newTimestamps = new StringBuilder();
                    int validRequests = 0;
                    
                    // 过滤掉窗口外的请求
                    long windowStart = currentTime - windowSizeSeconds;
                    for (String timestamp : timestamps) {
                        try {
                            long reqTime = Long.parseLong(timestamp);
                            if (reqTime >= windowStart) {
                                newTimestamps.append(reqTime).append(",");
                                validRequests++;
                            }
                        } catch (NumberFormatException e) {
                            // 忽略无效的时间戳
                        }
                    }
                    
                    // 检查是否超过限制
                    if (validRequests >= requestsPerSecond * windowSizeSeconds) {
                        return handleRateLimitExceeded(exchange, clientId, "Sliding window limit exceeded");
                    }
                    
                    // 添加当前请求
                    newTimestamps.append(currentTime);
                    
                    // 保存更新后的时间戳列表
                    return ops.set(key, newTimestamps.toString(), Duration.ofHours(1))
                            .then(chain.filter(exchange));
                });
    }

    /**
     * 获取客户端标识符
     */
    private String getClientIdentifier(ServerHttpRequest request) {
        // 优先使用API Key
        String apiKey = request.getHeaders().getFirst("X-API-Key");
        if (apiKey != null && !apiKey.isEmpty()) {
            return "api_key:" + apiKey;
        }
        
        // 使用用户ID
        String userId = request.getHeaders().getFirst("X-User-ID");
        if (userId != null && !userId.isEmpty()) {
            return "user:" + userId;
        }
        
        // 使用租户ID
        String tenantId = request.getHeaders().getFirst("X-Tenant-ID");
        if (tenantId != null && !tenantId.isEmpty()) {
            return "tenant:" + tenantId;
        }
        
        // 使用IP地址
        String ipAddress = getClientIpAddress(request);
        return "ip:" + ipAddress;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null ? 
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    /**
     * 处理限流超限情况
     */
    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange, String clientId, String reason) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("Retry-After", String.valueOf(ThreadLocalRandom.current().nextInt(1, 5)));
        response.getHeaders().add("X-RateLimit-Limit", String.valueOf(requestsPerSecond));
        response.getHeaders().add("X-RateLimit-Remaining", "0");
        response.getHeaders().add("X-RateLimit-Reset", String.valueOf(Instant.now().getEpochSecond() + windowSizeSeconds));
        
        String message = String.format("{\"error\": \"Rate limit exceeded\", \"message\": \"Too many requests from %s: %s\"}", 
                clientId, reason);
        
        log.warn("Rate limit exceeded for client {}: {}", clientId, reason);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(message.getBytes())));
    }

    @Override
    public int getOrder() {
        return -50; // 较高优先级执行
    }
}