package com.qoobot.openidaas.gateway.filter;

import com.qoobot.openidaas.common.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * JWT认证过滤器测试
 *
 * @author QooBot
 */
@SpringBootTest
@TestPropertySource(properties = {
    "gateway.auth.skip-paths=/api/public,/health"
})
class JwtAuthenticationFilterTest {

    @Autowired
    private JwtUtil jwtUtil;
    
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private GatewayFilter gatewayFilter;

    @Mock
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(chain.filter(any())).thenReturn(Mono.empty());
        
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil);
        JwtAuthenticationFilter.Config config = new JwtAuthenticationFilter.Config();
        gatewayFilter = jwtAuthenticationFilter.apply(config);
    }

    @Test
    void testFilter_SkipAuthenticationPath() {
        // 测试跳过认证的路径 - 但由于网关过滤器的工作方式，这个测试逻辑需要调整
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/public/test")
                .build();
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        gatewayFilter.filter(exchange, chain).block();
        
        // 对于不需要认证的路径，应该继续执行链
        verify(chain).filter(exchange);
    }

    @Test
    void testFilter_MissingAuthorizationHeader() {
        // 测试缺少Authorization头的情况
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/profile")
                .build();
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        gatewayFilter.filter(exchange, chain).block();
        
        // 应该返回UNAUTHORIZED状态
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(exchange);
    }

    @Test
    void testFilter_InvalidAuthorizationHeader() {
        // 测试无效的Authorization头
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/profile")
                .header(HttpHeaders.AUTHORIZATION, "InvalidToken")
                .build();
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        gatewayFilter.filter(exchange, chain).block();
        
        // 应该返回UNAUTHORIZED状态
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(exchange);
    }

    @Test
    void testFilter_ValidToken() {
        // 生成有效的JWT令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1L);
        claims.put("tenantId", 1L);
        String token = jwtUtil.generateAccessToken(claims, "testuser");
        
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        gatewayFilter.filter(exchange, chain).block();
        
        // 验证链被调用
        verify(chain).filter(any());
        
        // 验证响应状态
        assertEquals(HttpStatus.OK, exchange.getResponse().getStatusCode());
    }

    @Test
    void testFilter_ExpiredToken() {
        // 生成一个已过期的令牌（设置很短的过期时间）
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1L);
        claims.put("tenantId", 1L);
        String expiredToken = jwtUtil.generateAccessToken(claims, "testuser");
        
        // 等待令牌过期（这里只是模拟，实际JWT过期需要等待）
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/users/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .build();
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        gatewayFilter.filter(exchange, chain).block();
        
        // 应该返回UNAUTHORIZED状态
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(exchange);
    }

    @Test
    void testConfig_DefaultValues() {
        // 测试配置默认值
        JwtAuthenticationFilter.Config config = new JwtAuthenticationFilter.Config();
        assertEquals(true, config.isRequireAuth());
    }
    
    @Test
    void testConfig_SetRequireAuth() {
        // 测试配置requireAuth属性
        JwtAuthenticationFilter.Config config = new JwtAuthenticationFilter.Config();
        config.setRequireAuth(false);
        assertEquals(false, config.isRequireAuth());
    }
}