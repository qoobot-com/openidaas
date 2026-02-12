package com.qoobot.openidaas.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 请求日志过滤器
 *
 * @author QooBot
 */
@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        String requestId = java.util.UUID.randomUUID().toString();
        
        // 获取客户端IP
        String clientIp = getClientIp(request);
        
        // 记录请求信息
        log.info("[{}] Request: {} {} from {} at {}", 
                requestId,
                request.getMethod(), 
                request.getURI(), 
                clientIp,
                LocalDateTime.now().format(DATE_TIME_FORMATTER));

        // 执行后续过滤器链
        return chain.filter(exchange).then(
            Mono.fromRunnable(() -> {
                long duration = System.currentTimeMillis() - startTime;
                
                // 记录响应信息
                log.info("[{}] Response: {} {} in {}ms", 
                        requestId,
                        request.getMethod(),
                        response.getStatusCode(),
                        duration);
            })
        );
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(ServerHttpRequest request) {
        // 优先从X-Forwarded-For头获取
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        // 从X-Real-IP头获取
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // 从远程地址获取
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
    }

    @Override
    public int getOrder() {
        return -90; // 在认证过滤器之后执行
    }
}