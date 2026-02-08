package com.qoobot.openidaas.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qoobot.openidaas.gateway.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 限流网关过滤器
 * 
 * 基于令牌桶算法的分布式限流
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitGatewayFilter extends AbstractGatewayFilterFactory<Object> {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            // 获取客户端IP
            String clientIp = getClientIp(request);
            String requestUri = request.getPath().value();

            log.debug("Rate limiting check for IP: {}, URI: {}", clientIp, requestUri);

            try {
                // 检查限流
                boolean allowed = rateLimitService.tryAcquire(
                        clientIp,
                        requestUri,
                        100, // 默认容量
                        Duration.ofSeconds(1), // 1秒
                        50  // 补充速率
                );

                if (!allowed) {
                    log.warn("Rate limit exceeded for IP: {}, URI: {}", clientIp, requestUri);
                    return tooManyRequests(response);
                }

                // 添加限流头
                response.getHeaders().add("X-RateLimit-Limit", "50");
                response.getHeaders().add("X-RateLimit-Remaining", 
                        String.valueOf(rateLimitService.getRemainingTokens(clientIp, requestUri)));

                // 继续过滤器链
                return chain.filter(exchange);

            } catch (Exception e) {
                log.error("Rate limiting check failed: {}", e.getMessage());
                // 限流失败时允许通过
                return chain.filter(exchange);
            }
        };
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress() != null ? 
                    request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
        }
        
        // 处理多个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }

    /**
     * 返回限流响应
     */
    private Mono<Void> tooManyRequests(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("code", 429);
        body.put("message", "Too many requests, please try again later");
        body.put("timestamp", System.currentTimeMillis());

        try {
            byte[] bytes = objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return response.setComplete();
        }
    }
}
