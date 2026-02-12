package com.qoobot.openidaas.core.repository;

import com.qoobot.openidaas.core.domain.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 应用仓储接口
 *
 * @author QooBot
 */
public interface ApplicationRepository extends BaseRepository<Application, Long> {

    /**
     * 根据客户端ID查找应用
     */
    Optional<Application> findByClientId(String clientId);

    /**
     * 根据应用名称查找应用
     */
    Optional<Application> findByAppName(String appName);

    /**
     * 根据租户ID查找应用列表
     */
    Page<Application> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * 根据启用状态查找应用列表
     */
    List<Application> findByEnabled(Boolean enabled);

    /**
     * 根据租户ID和启用状态查找应用列表
     */
    List<Application> findByTenantIdAndEnabledTrue(Long tenantId);

    /**
     * 根据应用状态查找应用列表
     */
    List<Application> findByStatus(String status);

    /**
     * 根据应用类型查找应用列表
     */
    List<Application> findByAppType(String appType);

    /**
     * 根据应用名称模糊查询
     */
    @Query("SELECT a FROM Application a WHERE a.appName LIKE %:appName% AND a.tenantId = :tenantId")
    Page<Application> findByAppNameContainingAndTenantId(@Param("appName") String appName, @Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * 根据客户端ID模糊查询
     */
    @Query("SELECT a FROM Application a WHERE a.clientId LIKE %:clientId% AND a.tenantId = :tenantId")
    Page<Application> findByClientIdContainingAndTenantId(@Param("clientId") String clientId, @Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * 统计租户应用数量
     */
    long countByTenantId(Long tenantId);

    /**
     * 统计启用应用数量
     */
    long countByTenantIdAndEnabledTrue(Long tenantId);

    /**
     * 检查客户端ID是否已存在
     */
    boolean existsByClientId(String clientId);

    /**
     * 根据权限ID查找应用列表
     */
    @Query("SELECT a FROM Application a JOIN a.permissions p WHERE p.id = :permId")
    List<Application> findByPermissionId(@Param("permId") Long permId);

    /**
     * 查找需要审核的应用
     */
    @Query("SELECT a FROM Application a WHERE a.status = 'pending' AND a.tenantId = :tenantId")
    List<Application> findPendingApplications(@Param("tenantId") Long tenantId);
}