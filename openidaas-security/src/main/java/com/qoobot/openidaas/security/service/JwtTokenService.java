package com.qoobot.openidaas.security.service;

import com.qoobot.openidaas.security.config.SecurityProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JWT Token服务
 * 
 * 提供JWT Token的生成、验证、刷新等功能
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final SecurityProperties securityProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 生成Token
     * 
     * @param userDetails 用户详情
     * @return Token字符串
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", userDetails.getUsername());
        claims.put("authorities", userDetails.getAuthorities());

        return createToken(claims, userDetails.getUsername(),
                securityProperties.getJwt().getExpirationHours() * 3600 * 1000);
    }

    /**
     * 生成Refresh Token
     * 
     * @param userDetails 用户详情
     * @return Refresh Token字符串
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("username", userDetails.getUsername());

        return createToken(claims, userDetails.getUsername(),
                securityProperties.getJwt().getRefreshExpirationDays() * 24 * 3600 * 1000);
    }

    /**
     * 创建Token
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setIssuer(securityProperties.getJwt().getIssuer())
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从Token中提取用户名
     * 
     * @param token Token字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * 从Token中提取过期时间
     * 
     * @param token Token字符串
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

    /**
     * 验证Token是否有效
     * 
     * @param token Token字符串
     * @param userDetails 用户详情
     * @return 是否有效
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            String username = getUsernameFromToken(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查Token是否过期
     * 
     * @param token Token字符串
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 刷新Token
     * 
     * @param token 旧Token
     * @return 新Token
     */
    public String refreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            String username = claims.getSubject();

            // 移除旧Token到黑名单
            addToBlacklist(token, getExpirationDateFromToken(token));

            // 生成新Token
            UserDetails userDetails = getUserDetails(username);
            return generateToken(userDetails);
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new JwtException("Token refresh failed");
        }
    }

    /**
     * 将Token添加到黑名单
     * 
     * @param token Token字符串
     */
    public void addToBlacklist(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            addToBlacklist(token, expiration);
        } catch (Exception e) {
            log.error("Failed to add token to blacklist: {}", e.getMessage());
        }
    }

    /**
     * 将Token添加到黑名单
     * 
     * @param token Token字符串
     * @param expiration 过期时间
     */
    private void addToBlacklist(String token, Date expiration) {
        long ttl = expiration.getTime() - System.currentTimeMillis();
        if (ttl > 0) {
            String key = "token_blacklist:" + token;
            redisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.MILLISECONDS);
            log.debug("Added token to blacklist: {}", key);
        }
    }

    /**
     * 检查Token是否在黑名单中
     * 
     * @param token Token字符串
     * @return 是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        String key = "token_blacklist:" + token;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 从Token中提取Claims
     * 
     * @param token Token字符串
     * @return Claims对象
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 获取签名密钥
     * 
     * @return 签名密钥
     */
    private Key getSigningKey() {
        byte[] keyBytes = securityProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 获取用户详情（简化实现）
     */
    private UserDetails getUserDetails(String username) {
        // TODO: 从UserDetailsService加载
        return org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password("")
                .roles("USER")
                .build();
    }
}
