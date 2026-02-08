package com.qoobot.openidaas.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qoobot.openidaas.gateway.service.JwtTokenService;
import com.qoobot.openidaas.gateway.util.WebFluxUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证网关过滤器
 * 
 * 验证JWT Token和OAuth2 Token
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationGatewayFilter extends AbstractGatewayFilterFactory<Object> {

    private final JwtTokenService jwtTokenService;
    private final ObjectMapper objectMapper;

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            // 检查Authorization头
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header");
                return unauthorized(response, "Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);

            try {
                // 验证JWT Token
                if (!jwtTokenService.validateToken(token)) {
                    log.warn("Invalid JWT token");
                    return unauthorized(response, "Invalid or expired token");
                }

                // 解析Token获取用户信息
                Map<String, Object> claims = jwtTokenService.parseToken(token);
                String username = (String) claims.get("username");

                // 添加用户信息到请求头
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Id", claims.get("userId").toString())
                        .header("X-Username", username)
                        .header("X-Roles", String.join(",", (List<String>) claims.get("roles")))
                        .build();

                log.debug("Authentication successful for user: {}", username);

                // 继续过滤器链
                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                log.error("Token validation failed: {}", e.getMessage());
                return unauthorized(response, "Token validation failed: " + e.getMessage());
            }
        };
    }

    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("code", 401);
        body.put("message", message);
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
