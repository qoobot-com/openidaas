package com.qoobot.openidaas.gateway.util;

import org.springframework.web.server.ServerWebExchange;

/**
 * WebFlux工具类
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public class WebFluxUtils {

    /**
     * 获取请求ID
     * 
     * @param exchange ServerWebExchange
     * @return 请求ID
     */
    public static String getRequestId(ServerWebExchange exchange) {
        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-Id");
        return requestId != null ? requestId : java.util.UUID.randomUUID().toString();
    }

    /**
     * 获取用户ID
     * 
     * @param exchange ServerWebExchange
     * @return 用户ID
     */
    public static Long getUserId(ServerWebExchange exchange) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        return userId != null ? Long.parseLong(userId) : null;
    }

    /**
     * 获取用户名
     * 
     * @param exchange ServerWebExchange
     * @return 用户名
     */
    public static String getUsername(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst("X-Username");
    }

    /**
     * 获取客户端IP
     * 
     * @param exchange ServerWebExchange
     * @return 客户端IP
     */
    public static String getClientIp(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = exchange.getRequest().getRemoteAddress() != null ? 
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
        }
        return ip;
    }
}
