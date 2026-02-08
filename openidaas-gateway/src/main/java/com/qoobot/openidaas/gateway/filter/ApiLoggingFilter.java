package com.qoobot.openidaas.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * API日志过滤器
 * 
 * 记录API调用的请求和响应信息
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiLoggingFilter extends AbstractGatewayFilterFactory<Object> {

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            Instant startTime = Instant.now();

            String requestId = java.util.UUID.randomUUID().toString();
            String method = request.getMethod().name();
            String uri = request.getPath().value();
            String clientIp = getClientIp(request);
            String userAgent = request.getHeaders().getFirst("User-Agent");

            // 添加请求ID
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-Request-Id", requestId)
                    .build();

            log.info("[{}] {} {} from {}", requestId, method, uri, clientIp);

            return chain.filter(exchange.mutate().request(mutatedRequest).build())
                    .doFinally(signalType -> {
                        Duration duration = Duration.between(startTime, Instant.now());
                        ServerHttpResponse response = exchange.getResponse();
                        int statusCode = response.getStatusCode() != null ? 
                                response.getStatusCode().value() : 0;

                        log.info("[{}] {} {} - {} {} in {}ms", 
                                requestId, method, uri, statusCode, 
                                response.getStatusCode() != null ? response.getStatusCode().getReasonPhrase() : "",
                                duration.toMillis());
                    });
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
        return ip;
    }
}
