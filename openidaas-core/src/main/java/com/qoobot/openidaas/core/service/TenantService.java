package com.qoobot.openidaas.core.service;

import com.qoobot.openidaas.core.domain.Tenant;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 租户服务接口
 *
 * @author QooBot
 */
public interface TenantService {

    /**
     * 创建租户
     */
    Tenant createTenant(Tenant tenant);

    /**
     * 更新租户
     */
    Tenant updateTenant(Tenant tenant);

    /**
     * 删除租户
     */
    void deleteTenant(Long tenantId);

    /**
     * 根据ID获取租户
     */
    Tenant getTenantById(Long tenantId);

    /**
     * 根据租户编码获取租户
     */
    Tenant getTenantByCode(String tenantCode);

    /**
     * 根据域名获取租户
     */
    Tenant getTenantByDomain(String domain);

    /**
     * 获取所有租户列表
     */
    List<Tenant> getAllTenants();

    /**
     * 获取启用的租户列表
     */
    List<Tenant> getEnabledTenants();

    /**
     * 启用租户
     */
    void enableTenant(Long tenantId);

    /**
     * 禁用租户
     */
    void disableTenant(Long tenantId);

    /**
     * 检查租户编码是否唯一
     */
    boolean isTenantCodeUnique(String tenantCode, Long excludeTenantId);

    /**
     * 检查域名是否唯一
     */
    boolean isDomainUnique(String domain, Long excludeTenantId);

    /**
     * 检查租户是否过期
     */
    boolean isTenantExpired(Long tenantId);

    /**
     * 获取即将过期的租户
     */
    List<Tenant> getExpiringTenants(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 更新租户用户使用数量
     */
    void updateUsedUsers(Long tenantId, Integer usedUsers);

    /**
     * 更新租户应用使用数量
     */
    void updateUsedApps(Long tenantId, Integer usedApps);

    /**
     * 检查租户是否超出限制
     */
    boolean isTenantOverLimit(Long tenantId);

    /**
     * 获取超出限制的租户列表
     */
    List<Tenant> getOverLimitTenants();

    /**
     * 初始化租户数据
     */
    void initializeTenantData(Long tenantId);

    /**
     * 验证租户状态
     */
    boolean validateTenantStatus(Long tenantId);
}