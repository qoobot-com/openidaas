package com.qoobot.openidaas.gateway.filter;

import com.qoobot.openidaas.common.config.ApiVersionConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * API 版本过滤器
 * 检查请求的 API 版本是否支持
 *
 * @author QooBot
 */
@Slf4j
@Component
public class ApiVersionFilter extends AbstractGatewayFilterFactory<Object> implements Ordered {

    @Override
    public GatewayFilter apply(Object config) {
        return new ApiVersionGatewayFilter();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }

    /**
     * API 版本网关过滤器
     */
    private static class ApiVersionGatewayFilter implements GatewayFilter {

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();

            // 检查是否为 API 请求
            if (!path.startsWith("/api/")) {
                return chain.filter(exchange);
            }

            // 提取版本号
            String version = extractVersionFromPath(path);
            log.debug("Request path: {}, API version: {}", path, version);

            // 检查版本是否支持
            if (!ApiVersionConfig.isVersionSupported(version)) {
                log.warn("Unsupported API version: {} for path: {}", version, path);
                return buildErrorResponse(exchange, version);
            }

            // 添加版本信息到请求头
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-API-Version", version)
                    .header("X-API-Path", path)
                    .build();

            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();

            return chain.filter(modifiedExchange);
        }

        /**
         * 从路径中提取版本号
         */
        private String extractVersionFromPath(String path) {
            if (path == null || path.isEmpty()) {
                return ApiVersionConfig.DEFAULT_VERSION;
            }

            String[] parts = path.split("/");
            for (String part : parts) {
                if (part.startsWith("v") && part.length() > 1) {
                    if (ApiVersionConfig.isVersionSupported(part)) {
                        return part;
                    }
                }
            }

            return ApiVersionConfig.DEFAULT_VERSION;
        }

        /**
         * 构建错误响应
         */
        private Mono<Void> buildErrorResponse(ServerWebExchange exchange, String version) {
            String responseBody = String.format(
                    "{\"code\":%d,\"message\":\"Unsupported API version: %s\",\"data\":null," +
                            "\"meta\":{\"version\":\"%s\",\"supportedVersions\":[\"v1\",\"v2\"]}}",
                    HttpStatus.BAD_REQUEST.value(),
                    version,
                    ApiVersionConfig.DEFAULT_VERSION
            );

            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");

            return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory().wrap(responseBody.getBytes()))
            );
        }
    }
}
