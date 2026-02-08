package com.qoobot.openidaas.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qoobot.openidaas.security.config.SecurityProperties;
import com.qoobot.openidaas.security.service.RateLimitService;
import com.qoobot.openidaas.security.util.IpUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 限流过滤器
 * 
 * 基于令牌桶算法的限流实现，支持不同接口的不同限流策略
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final SecurityProperties securityProperties;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 检查是否启用限流
        if (!securityProperties.getRateLimit().getEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = IpUtils.getClientIp(request);
        String requestUri = request.getRequestURI();

        log.debug("Rate limiting check for IP: {}, URI: {}", clientIp, requestUri);

        // 登录接口特殊限流
        if (requestUri.contains("/api/auth/login")) {
            if (!checkLoginRateLimit(clientIp, request, response)) {
                return;
            }
        } else {
            // 其他接口通用限流
            if (!checkApiRateLimit(clientIp, request, response)) {
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 检查登录限流
     */
    private boolean checkLoginRateLimit(
            String ip,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        SecurityProperties.RateLimit rateLimit = securityProperties.getRateLimit();
        
        boolean allowed = rateLimitService.tryAcquire(
                ip,
                "login",
                rateLimit.getLoginCapacity(),
                Duration.ofMinutes(1),
                rateLimit.getLoginRefillRate()
        );

        if (!allowed) {
            log.warn("Login rate limit exceeded for IP: {}", ip);
            writeRateLimitExceededResponse(response, "登录过于频繁，请稍后再试");
            return false;
        }

        return true;
    }

    /**
     * 检查API限流
     */
    private boolean checkApiRateLimit(
            String ip,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        SecurityProperties.RateLimit rateLimit = securityProperties.getRateLimit();

        boolean allowed = rateLimitService.tryAcquire(
                ip,
                request.getRequestURI(),
                rateLimit.getApiCapacity(),
                Duration.ofSeconds(1),
                rateLimit.getApiRefillRate()
        );

        if (!allowed) {
            log.warn("API rate limit exceeded for IP: {}, URI: {}", ip, request.getRequestURI());
            writeRateLimitExceededResponse(response, "请求过于频繁，请稍后再试");
            return false;
        }

        return true;
    }

    /**
     * 写入限流响应
     */
    private void writeRateLimitExceededResponse(
            HttpServletResponse response,
            String message) throws IOException {

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new HashMap<>();
        body.put("code", 429);
        body.put("message", message);
        body.put("timestamp", System.currentTimeMillis());

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 不对健康检查、静态资源等限流
        return path.startsWith("/actuator/health") ||
               path.startsWith("/static/") ||
               path.endsWith(".css") ||
               path.endsWith(".js") ||
               path.endsWith(".png") ||
               path.endsWith(".jpg") ||
               path.endsWith(".svg") ||
               path.endsWith(".ico");
    }
}
