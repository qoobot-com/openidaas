package com.qoobot.openidaas.core.repository;

import com.qoobot.openidaas.core.domain.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 租户仓储接口
 *
 * @author QooBot
 */
public interface TenantRepository extends BaseRepository<Tenant, Long> {

    /**
     * 根据租户编码查找租户
     */
    Optional<Tenant> findByTenantCode(String tenantCode);

    /**
     * 根据租户名称查找租户
     */
    Optional<Tenant> findByTenantName(String tenantName);

    /**
     * 根据域名查找租户
     */
    Optional<Tenant> findByDomain(String domain);

    /**
     * 根据启用状态查找租户列表
     */
    Page<Tenant> findByEnabled(Boolean enabled, Pageable pageable);

    /**
     * 根据租户状态查找租户列表
     */
    List<Tenant> findByStatus(String status);

    /**
     * 根据过期时间查找即将过期的租户
     */
    @Query("SELECT t FROM Tenant t WHERE t.expireTime BETWEEN :startTime AND :endTime AND t.enabled = true")
    List<Tenant> findExpiringTenants(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据租户名称模糊查询
     */
    @Query("SELECT t FROM Tenant t WHERE t.tenantName LIKE %:tenantName%")
    Page<Tenant> findByTenantNameContaining(@Param("tenantName") String tenantName, Pageable pageable);

    /**
     * 根据租户编码模糊查询
     */
    @Query("SELECT t FROM Tenant t WHERE t.tenantCode LIKE %:tenantCode%")
    Page<Tenant> findByTenantCodeContaining(@Param("tenantCode") String tenantCode, Pageable pageable);

    /**
     * 统计启用租户数量
     */
    long countByEnabledTrue();

    /**
     * 统计各状态租户数量
     */
    @Query("SELECT t.status, COUNT(t) FROM Tenant t GROUP BY t.status")
    List<Object[]> countByStatus();

    /**
     * 检查租户编码是否已存在
     */
    boolean existsByTenantCode(String tenantCode);

    /**
     * 检查域名是否已存在
     */
    boolean existsByDomain(String domain);

    /**
     * 查找超出租户限制的租户
     */
    @Query("SELECT t FROM Tenant t WHERE t.usedUsers >= t.maxUsers OR t.usedApps >= t.maxApps")
    List<Tenant> findOverLimitTenants();

    /**
     * 更新租户用户使用数量
     */
    @Query("UPDATE Tenant t SET t.usedUsers = :usedUsers WHERE t.id = :tenantId")
    void updateUsedUsers(@Param("tenantId") Long tenantId, @Param("usedUsers") Integer usedUsers);

    /**
     * 更新租户应用使用数量
     */
    @Query("UPDATE Tenant t SET t.usedApps = :usedApps WHERE t.id = :tenantId")
    void updateUsedApps(@Param("tenantId") Long tenantId, @Param("usedApps") Integer usedApps);
}