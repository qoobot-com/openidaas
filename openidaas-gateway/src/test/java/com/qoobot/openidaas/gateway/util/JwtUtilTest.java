package com.qoobot.openidaas.gateway.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT工具类测试
 *
 * @author QooBot
 */
@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=testSecretKey123456789012345678901234567890",
    "jwt.expiration=3600000"
})
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    private String validToken;
    private String username = "testuser";
    private Long userId = 1L;
    private Long tenantId = 1L;

    @BeforeEach
    void setUp() {
        validToken = jwtUtil.generateToken(username, userId, tenantId);
    }

    @Test
    void testGenerateToken() {
        String token = jwtUtil.generateToken(username, userId, tenantId);
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertNotEquals(validToken, token); // 每次生成的token应该不同
    }

    @Test
    void testValidateToken_ValidToken() {
        assertTrue(jwtUtil.validateToken(validToken));
    }

    @Test
    void testValidateToken_InvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid.token.string"));
    }

    @Test
    void testValidateToken_NullToken() {
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    void testValidateToken_EmptyToken() {
        assertFalse(jwtUtil.validateToken(""));
    }

    @Test
    void testGetUsernameFromToken() {
        String extractedUsername = jwtUtil.getUsernameFromToken(validToken);
        assertEquals(username, extractedUsername);
    }

    @Test
    void testGetUsernameFromToken_InvalidToken() {
        String extractedUsername = jwtUtil.getUsernameFromToken("invalid.token");
        assertNull(extractedUsername);
    }

    @Test
    void testGetUserIdFromToken() {
        Long extractedUserId = jwtUtil.getUserIdFromToken(validToken);
        assertEquals(userId, extractedUserId);
    }

    @Test
    void testGetUserIdFromToken_InvalidToken() {
        Long extractedUserId = jwtUtil.getUserIdFromToken("invalid.token");
        assertNull(extractedUserId);
    }

    @Test
    void testGetTenantIdFromToken() {
        Long extractedTenantId = jwtUtil.getTenantIdFromToken(validToken);
        assertEquals(tenantId, extractedTenantId);
    }

    @Test
    void testGetTenantIdFromToken_InvalidToken() {
        Long extractedTenantId = jwtUtil.getTenantIdFromToken("invalid.token");
        assertNull(extractedTenantId);
    }

    @Test
    void testGetExpirationDateFromToken() {
        assertNotNull(jwtUtil.getExpirationDateFromToken(validToken));
    }

    @Test
    void testGetExpirationDateFromToken_InvalidToken() {
        assertNull(jwtUtil.getExpirationDateFromToken("invalid.token"));
    }

    @Test
    void testIsTokenExpired_ValidToken() {
        assertFalse(jwtUtil.isTokenExpired(validToken));
    }

    @Test
    void testRefreshToken() {
        String refreshedToken = jwtUtil.refreshToken(validToken);
        assertNotNull(refreshedToken);
        assertNotEquals(validToken, refreshedToken);
        
        // 验证刷新后的token仍然有效
        assertTrue(jwtUtil.validateToken(refreshedToken));
        
        // 验证用户信息一致
        assertEquals(username, jwtUtil.getUsernameFromToken(refreshedToken));
        assertEquals(userId, jwtUtil.getUserIdFromToken(refreshedToken));
        assertEquals(tenantId, jwtUtil.getTenantIdFromToken(refreshedToken));
    }

    @Test
    void testRefreshToken_InvalidToken() {
        String refreshedToken = jwtUtil.refreshToken("invalid.token");
        assertNull(refreshedToken);
    }

    @Test
    void testTokenClaimsConsistency() {
        String token1 = jwtUtil.generateToken(username, userId, tenantId);
        String token2 = jwtUtil.generateToken(username, userId, tenantId);
        
        // 同样的用户信息应该能从不同token中提取出来
        assertEquals(jwtUtil.getUsernameFromToken(token1), jwtUtil.getUsernameFromToken(token2));
        assertEquals(jwtUtil.getUserIdFromToken(token1), jwtUtil.getUserIdFromToken(token2));
        assertEquals(jwtUtil.getTenantIdFromToken(token1), jwtUtil.getTenantIdFromToken(token2));
    }
}