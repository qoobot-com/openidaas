package com.qoobot.openidaas.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 请求追踪过滤器
 * 为每个请求生成唯一的追踪ID，便于分布式链路追踪
 *
 * @author QooBot
 */
@Slf4j
@Component
public class RequestTracingFilter implements GlobalFilter, Ordered {

    private static final String TRACE_ID_HEADER = "X-Trace-ID";
    private static final String SPAN_ID_HEADER = "X-Span-ID";
    private static final String PARENT_SPAN_ID_HEADER = "X-Parent-Span-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 生成追踪ID
        String traceId = generateTraceId(request);
        String spanId = generateSpanId();
        String parentSpanId = request.getHeaders().getFirst(SPAN_ID_HEADER);

        // 将追踪信息添加到请求头中
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(TRACE_ID_HEADER, traceId)
                .header(SPAN_ID_HEADER, spanId)
                .header(PARENT_SPAN_ID_HEADER, parentSpanId != null ? parentSpanId : "root")
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        // 记录请求信息
        logRequestInfo(mutatedExchange, traceId, spanId, parentSpanId);

        return chain.filter(mutatedExchange)
                .doFinally(signalType -> {
                    // 记录响应信息
                    logResponseInfo(mutatedExchange, traceId, spanId);
                });
    }

    /**
     * 生成追踪ID
     */
    private String generateTraceId(ServerHttpRequest request) {
        // 优先使用请求中已有的追踪ID
        String existingTraceId = request.getHeaders().getFirst(TRACE_ID_HEADER);
        if (existingTraceId != null && !existingTraceId.isEmpty()) {
            return existingTraceId;
        }
        
        // 生成新的UUID作为追踪ID
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成跨度ID
     */
    private String generateSpanId() {
        return UUID.randomUUID().toString().substring(0, 16);
    }

    /**
     * 记录请求信息
     */
    private void logRequestInfo(ServerWebExchange exchange, String traceId, String spanId, String parentSpanId) {
        ServerHttpRequest request = exchange.getRequest();
        
        log.info("[TraceID: {}] [SpanID: {}] [ParentSpanID: {}] Incoming request: {} {} from {}",
                traceId,
                spanId,
                parentSpanId != null ? parentSpanId : "root",
                request.getMethod(),
                request.getURI().getPath(),
                getClientIpAddress(request));
    }

    /**
     * 记录响应信息
     */
    private void logResponseInfo(ServerWebExchange exchange, String traceId, String spanId) {
        int statusCode = exchange.getResponse().getStatusCode() != null ? 
                exchange.getResponse().getStatusCode().value() : 0;
        
        log.info("[TraceID: {}] [SpanID: {}] Outgoing response: {} {} with status {}",
                traceId,
                spanId,
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI().getPath(),
                statusCode);
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

    @Override
    public int getOrder() {
        return -200; // 最高优先级执行，确保在其他过滤器之前设置追踪信息
    }
}