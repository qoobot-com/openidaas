package com.qoobot.openidaas.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 响应压缩过滤器
 * 自动对符合条件的响应进行GZIP压缩，提升传输效率
 *
 * @author QooBot
 */
@Slf4j
@Component
public class ResponseCompressionFilter implements GlobalFilter, Ordered {

    private static final List<String> COMPRESSIBLE_CONTENT_TYPES = List.of(
        "application/json",
        "application/xml",
        "text/html",
        "text/css",
        "text/plain",
        "text/javascript",
        "application/javascript"
    );
    
    private static final int MIN_COMPRESSION_SIZE = 1024; // 1KB
    private static final String GZIP_ENCODING = "gzip";
    private static final String ACCEPT_ENCODING_HEADER = "Accept-Encoding";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        
        // 检查客户端是否支持GZIP压缩
        if (!supportsGzipCompression(request)) {
            return chain.filter(exchange);
        }
        
        // 先执行后续过滤器链
        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    // 检查是否应该压缩响应
                    if (shouldCompressResponse(response)) {
                        // 设置压缩头部
                        response.getHeaders().add(HttpHeaders.CONTENT_ENCODING, GZIP_ENCODING);
                        log.debug("Enabled GZIP compression for response");
                    }
                }));
    }

    /**
     * 检查客户端是否支持GZIP压缩
     */
    private boolean supportsGzipCompression(ServerHttpRequest request) {
        String acceptEncoding = request.getHeaders().getFirst(ACCEPT_ENCODING_HEADER);
        return acceptEncoding != null && acceptEncoding.contains(GZIP_ENCODING);
    }

    /**
     * 判断是否应该压缩响应
     */
    private boolean shouldCompressResponse(ServerHttpResponse response) {
        // 检查内容长度
        String contentLength = response.getHeaders().getFirst(HttpHeaders.CONTENT_LENGTH);
        if (contentLength != null) {
            try {
                long length = Long.parseLong(contentLength);
                if (length < MIN_COMPRESSION_SIZE) {
                    return false;
                }
            } catch (NumberFormatException e) {
                // 如果无法解析内容长度，继续检查内容类型
            }
        }
        
        // 检查内容类型
        String contentType = response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        if (contentType != null) {
            String mimeType = contentType.split(";")[0].trim().toLowerCase();
            return COMPRESSIBLE_CONTENT_TYPES.contains(mimeType);
        }
        
        return false;
    }

    /**
     * 压缩字节数组
     */
    private byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            gzipOut.write(data);
        }
        return baos.toByteArray();
    }

    @Override
    public int getOrder() {
        return -30; // 在追踪过滤器之后，其他过滤器之前执行
    }
}