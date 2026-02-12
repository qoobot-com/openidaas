package com.qoobot.openidaas.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类
 *
 * @author QooBot
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:mySecretKey12345678901234567890123456789012}")
    private String secret;

    @Value("${jwt.expiration:3600}")
    private Long expiration;

    @Value("${jwt.refresh-expiration:86400}")
    private Long refreshExpiration;

    /**
     * 生成访问令牌
     */
    public String generateAccessToken(Map<String, Object> claims, String subject) {
        return createToken(claims, subject, expiration);
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(String subject) {
        return createToken(new HashMap<>(), subject, refreshExpiration);
    }

    /**
     * 创建JWT令牌
     */
    private String createToken(Map<String, Object> claims, String subject, Long expirationSeconds) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 验证JWT令牌
     */
    public Boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从令牌中获取声明
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 获取所有声明
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    /**
     * 检查令牌是否过期
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getClaimFromToken(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    /**
     * 刷新访问令牌
     */
    public String refreshToken(String refreshToken) {
        if (!validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String username = getUsernameFromToken(refreshToken);
        return generateAccessToken(null, username);
    }
}