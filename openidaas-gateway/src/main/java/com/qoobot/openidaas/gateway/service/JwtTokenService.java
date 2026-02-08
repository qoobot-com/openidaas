package com.qoobot.openidaas.gateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * JWT Token服务
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class JwtTokenService {

    private static final String SECRET_KEY = "openidaas-secret-key-change-in-production";

    /**
     * 验证Token
     * 
     * @param token Token字符串
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token);
            
            // 检查是否过期
            if (claims.getExpiration().before(new java.util.Date())) {
                log.warn("Token expired");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 解析Token
     * 
     * @param token Token字符串
     * @return Claims对象
     */
    public Map<String, Object> parseToken(String token) {
        Claims claims = parseClaims(token);
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("username", claims.getSubject());
        result.put("userId", claims.get("userId"));
        result.put("roles", claims.get("roles"));
        result.put("exp", claims.getExpiration().getTime());
        result.put("iat", claims.getIssuedAt().getTime());
        
        return result;
    }

    /**
     * 解析Claims
     * 
     * @param token Token字符串
     * @return Claims对象
     */
    private Claims parseClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
