package com.qoobot.openidaas.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 响应头增强过滤器
 *
 * @author QooBot
 */
@Component
public class ResponseHeaderFilter implements GlobalFilter, Ordered {

    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.RFC_1123_DATE_TIME;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(
            Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();
                HttpHeaders headers = response.getHeaders();
                
                // 添加安全相关头部
                headers.add("X-Content-Type-Options", "nosniff");
                headers.add("X-Frame-Options", "DENY");
                headers.add("X-XSS-Protection", "1; mode=block");
                headers.add("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                
                // 添加服务器信息
                headers.add("Server", "OpenIDaaS-Gateway");
                
                // 添加时间戳
                headers.add("Date", DATE_FORMATTER.format(ZonedDateTime.now()));
                
                // 添加请求ID（如果存在）
                String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-ID");
                if (requestId != null) {
                    headers.add("X-Response-ID", requestId);
                }
                
                // 添加CORS相关头部（如果需要）
                if (!headers.containsKey("Access-Control-Allow-Origin")) {
                    headers.add("Access-Control-Allow-Origin", "*");
                }
            })
        );
    }

    @Override
    public int getOrder() {
        return -70; // 较低优先级，在其他过滤器之后执行
    }
}