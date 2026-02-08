package com.qoobot.openidaas.auth.repository;

import com.qoobot.openidaas.core.entity.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * AuthToken Repository
 * 
 * 认证令牌数据访问层
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    /**
     * 根据 Access Token 查找
     */
    Optional<AuthToken> findByAccessToken(String accessToken);

    /**
     * 根据 Refresh Token 查找
     */
    Optional<AuthToken> findByRefreshToken(String refreshToken);

    /**
     * 根据用户 ID 查找所有 Token
     */
    List<AuthToken> findByUserId(Long userId);

    /**
     * 根据租户 ID 查找所有 Token
     */
    List<AuthToken> findByTenantId(Long tenantId);

    /**
     * 根据用户 ID 和设备 ID 查找 Token
     */
    List<AuthToken> findByUserIdAndDeviceId(Long userId, String deviceId);

    /**
     * 查找已撤销且过期的 Token
     */
    List<AuthToken> findRevokedTokensBefore(LocalDateTime date);

    /**
     * 删除已撤销的 Token
     */
    void deleteByRevokedTrue();

    /**
     * 删除过期的 Token
     */
    void deleteByAccessTokenExpiredAtBefore(LocalDateTime date);
}
