package com.qoobot.openidaas.auth.service;

import com.qoobot.openidaas.auth.repository.AuthTokenRepository;
import com.qoobot.openidaas.core.entity.AuthToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Token 撤销服务
 * 
 * 实现 Token 的撤销、缓存和审计功能
 * 支持单点登录（所有设备登出）
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRevocationService {

    private final AuthTokenRepository authTokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String REVOKED_TOKENS_KEY = "revoked:tokens:";
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final Duration BLACKLIST_TTL = Duration.ofDays(30);

    /**
     * 撤销 Access Token
     * 
     * @param accessToken Access Token
     * @param revokeReason 撤销原因
     */
    @Transactional
    public void revokeAccessToken(String accessToken, String revokeReason) {
        try {
            // 查找数据库中的 Token
            Optional<AuthToken> authToken = authTokenRepository.findByAccessToken(accessToken);
            
            if (authToken.isPresent()) {
                AuthToken token = authToken.get();
                token.setRevoked(true);
                authTokenRepository.save(token);
                
                // 添加到 Redis 黑名单
                addToBlacklist(accessToken, token.getAccessTokenExpiredAt());
                
                log.info("Access token revoked for user: {}, reason: {}", 
                        token.getUserId(), revokeReason);
            } else {
                log.warn("Access token not found in database");
            }
            
        } catch (Exception e) {
            log.error("Failed to revoke access token", e);
            throw new RuntimeException("Token revocation failed", e);
        }
    }

    /**
     * 撤销 Refresh Token
     * 
     * @param refreshToken Refresh Token
     * @param revokeReason 撤销原因
     */
    @Transactional
    public void revokeRefreshToken(String refreshToken, String revokeReason) {
        try {
            // 查找数据库中的 Token
            Optional<AuthToken> authToken = authTokenRepository.findByRefreshToken(refreshToken);
            
            if (authToken.isPresent()) {
                AuthToken token = authToken.get();
                token.setRevoked(true);
                authTokenRepository.save(token);
                
                // 添加到 Redis 黑名单
                if (token.getRefreshTokenExpiredAt() != null) {
                    addToBlacklist(refreshToken, token.getRefreshTokenExpiredAt());
                }
                
                log.info("Refresh token revoked for user: {}, reason: {}", 
                        token.getUserId(), revokeReason);
            } else {
                log.warn("Refresh token not found in database");
            }
            
        } catch (Exception e) {
            log.error("Failed to revoke refresh token", e);
            throw new RuntimeException("Token revocation failed", e);
        }
    }

    /**
     * 撤销用户的所有 Token（单点登录）
     * 
     * @param userId 用户 ID
     * @param revokeReason 撤销原因
     */
    @Transactional
    public void revokeAllUserTokens(Long userId, String revokeReason) {
        try {
            // 查找用户的所有 Token
            List<AuthToken> tokens = authTokenRepository.findByUserId(userId);
            
            for (AuthToken token : tokens) {
                token.setRevoked(true);
                
                // 添加到黑名单
                if (token.getAccessTokenExpiredAt() != null) {
                    addToBlacklist(token.getAccessToken(), token.getAccessTokenExpiredAt());
                }
                if (token.getRefreshTokenExpiredAt() != null && token.getRefreshToken() != null) {
                    addToBlacklist(token.getRefreshToken(), token.getRefreshTokenExpiredAt());
                }
            }
            
            authTokenRepository.saveAll(tokens);
            
            log.info("All tokens revoked for user: {}, count: {}, reason: {}", 
                    userId, tokens.size(), revokeReason);
            
        } catch (Exception e) {
            log.error("Failed to revoke all user tokens", e);
            throw new RuntimeException("Token revocation failed", e);
        }
    }

    /**
     * 撤销租户的所有 Token
     * 
     * @param tenantId 租户 ID
     * @param revokeReason 撤销原因
     */
    @Transactional
    public void revokeAllTenantTokens(Long tenantId, String revokeReason) {
        try {
            // 查找租户的所有 Token
            List<AuthToken> tokens = authTokenRepository.findByTenantId(tenantId);
            
            for (AuthToken token : tokens) {
                token.setRevoked(true);
                
                // 添加到黑名单
                if (token.getAccessTokenExpiredAt() != null) {
                    addToBlacklist(token.getAccessToken(), token.getAccessTokenExpiredAt());
                }
                if (token.getRefreshTokenExpiredAt() != null && token.getRefreshToken() != null) {
                    addToBlacklist(token.getRefreshToken(), token.getRefreshTokenExpiredAt());
                }
            }
            
            authTokenRepository.saveAll(tokens);
            
            log.info("All tokens revoked for tenant: {}, count: {}, reason: {}", 
                    tenantId, tokens.size(), revokeReason);
            
        } catch (Exception e) {
            log.error("Failed to revoke all tenant tokens", e);
            throw new RuntimeException("Token revocation failed", e);
        }
    }

    /**
     * 撤销特定设备的 Token
     * 
     * @param userId 用户 ID
     * @param deviceId 设备 ID
     * @param revokeReason 撤销原因
     */
    @Transactional
    public void revokeDeviceTokens(Long userId, String deviceId, String revokeReason) {
        try {
            // 查找用户在该设备的所有 Token
            List<AuthToken> tokens = authTokenRepository.findByUserIdAndDeviceId(userId, deviceId);
            
            for (AuthToken token : tokens) {
                token.setRevoked(true);
                
                // 添加到黑名单
                if (token.getAccessTokenExpiredAt() != null) {
                    addToBlacklist(token.getAccessToken(), token.getAccessTokenExpiredAt());
                }
                if (token.getRefreshTokenExpiredAt() != null && token.getRefreshToken() != null) {
                    addToBlacklist(token.getRefreshToken(), token.getRefreshTokenExpiredAt());
                }
            }
            
            authTokenRepository.saveAll(tokens);
            
            log.info("Device tokens revoked for user: {}, device: {}, count: {}, reason: {}", 
                    userId, deviceId, tokens.size(), revokeReason);
            
        } catch (Exception e) {
            log.error("Failed to revoke device tokens", e);
            throw new RuntimeException("Token revocation failed", e);
        }
    }

    /**
     * 检查 Token 是否已被撤销
     * 
     * @param token Token
     * @return 是否已撤销
     */
    public boolean isTokenRevoked(String token) {
        // 检查 Redis 黑名单
        Boolean isRevoked = redisTemplate.hasKey(BLACKLIST_PREFIX + token);
        
        if (isRevoked != null && isRevoked) {
            log.debug("Token found in blacklist: {}", token);
            return true;
        }
        
        // 检查数据库
        Optional<AuthToken> authToken = authTokenRepository.findByAccessToken(token);
        
        if (authToken.isPresent()) {
            return authToken.get().getRevoked();
        }
        
        return false;
    }

    /**
     * 清理过期的撤销 Token
     */
    @Transactional
    public void cleanupExpiredRevokedTokens() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
            List<AuthToken> expiredTokens = authTokenRepository
                    .findRevokedTokensBefore(cutoff);
            
            authTokenRepository.deleteAll(expiredTokens);
            
            log.info("Cleaned up {} expired revoked tokens", expiredTokens.size());
            
        } catch (Exception e) {
            log.error("Failed to cleanup expired revoked tokens", e);
        }
    }

    /**
     * 添加 Token 到黑名单
     */
    private void addToBlacklist(String token, LocalDateTime expiresAt) {
        try {
            String key = BLACKLIST_PREFIX + token;
            long ttl = java.time.Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
            
            if (ttl > 0) {
                redisTemplate.opsForValue().set(key, "revoked", Duration.ofSeconds(ttl));
                log.debug("Token added to blacklist: {}, TTL: {}s", key, ttl);
            }
            
        } catch (Exception e) {
            log.error("Failed to add token to blacklist", e);
        }
    }

    /**
     * 从黑名单移除 Token
     */
    public void removeFromBlacklist(String token) {
        redisTemplate.delete(BLACKLIST_PREFIX + token);
        log.debug("Token removed from blacklist: {}", token);
    }

    /**
     * 获取用户的活跃 Token 列表
     * 
     * @param userId 用户 ID
     * @return 活跃 Token 列表
     */
    public List<AuthToken> getActiveTokens(Long userId) {
        return authTokenRepository.findByUserId(userId).stream()
                .filter(token -> !token.getRevoked())
                .filter(token -> token.getAccessTokenExpiredAt().isAfter(LocalDateTime.now()))
                .toList();
    }

    /**
     * 记录 Token 审计日志
     * 
     * @param token Token
     * @param action 操作类型（CREATE, REFRESH, REVOKE）
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     */
    public void auditToken(String token, String action, Long userId, Long tenantId) {
        try {
            TokenAuditLog auditLog = TokenAuditLog.builder()
                    .tokenId(token)
                    .action(action)
                    .userId(userId)
                    .tenantId(tenantId)
                    .timestamp(LocalDateTime.now())
                    .ipAddress("127.0.0.1") // TODO: 从请求中获取
                    .userAgent("Mozilla/5.0") // TODO: 从请求中获取
                    .build();
            
            // TODO: 保存审计日志到数据库或日志系统
            log.info("Token audit: {} - {} - userId: {}, tenantId: {}", 
                    action, token, userId, tenantId);
            
        } catch (Exception e) {
            log.error("Failed to record token audit log", e);
        }
    }

    /**
     * Token 审计日志
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TokenAuditLog {
        private String tokenId;
        private String action;
        private Long userId;
        private Long tenantId;
        private LocalDateTime timestamp;
        private String ipAddress;
        private String userAgent;
    }
}
