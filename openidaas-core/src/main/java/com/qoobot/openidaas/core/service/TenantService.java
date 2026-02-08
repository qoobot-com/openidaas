package com.qoobot.openidaas.core.service;

import com.qoobot.openidaas.core.dto.TenantDTO;
import com.qoobot.openidaas.core.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 租户服务接口
 * 
 * 提供租户管理、查询等功能
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public interface TenantService {
    
    /**
     * 创建租户
     * 
     * @param tenant 租户实体
     * @return 创建后的租户
     */
    Tenant createTenant(Tenant tenant);
    
    /**
     * 更新租户
     * 
     * @param tenant 租户实体
     * @return 更新后的租户
     */
    Tenant updateTenant(Tenant tenant);
    
    /**
     * 删除租户
     * 
     * @param tenantId 租户ID
     */
    void deleteTenant(Long tenantId);
    
    /**
     * 根据ID查找租户
     * 
     * @param tenantId 租户ID
     * @return 租户实体
     */
    Optional<Tenant> findTenantById(Long tenantId);
    
    /**
     * 根据租户编码查找租户
     * 
     * @param code 租户编码
     * @return 租户实体
     */
    Optional<Tenant> findTenantByCode(String code);
    
    /**
     * 分页查询租户
     * 
     * @param pageable 分页参数
     * @return 租户分页结果
     */
    Page<Tenant> findAllTenants(Pageable pageable);
    
    /**
     * 查询所有激活的租户
     * 
     * @return 租户列表
     */
    List<Tenant> findActiveTenants();
    
    /**
     * 更新租户状态
     * 
     * @param tenantId 租户ID
     * @param status 租户状态
     */
    void updateTenantStatus(Long tenantId, Tenant.TenantStatus status);
    
    /**
     * 增加租户用户数量
     * 
     * @param tenantId 租户ID
     */
    void incrementUserCount(Long tenantId);
    
    /**
     * 减少租户用户数量
     * 
     * @param tenantId 租户ID
     */
    void decrementUserCount(Long tenantId);
    
    /**
     * 检查租户是否达到用户数量限制
     * 
     * @param tenantId 租户ID
     * @return 是否达到限制
     */
    boolean isUserLimitReached(Long tenantId);
    
    /**
     * 转换为DTO
     * 
     * @param tenant 租户实体
     * @return 租户DTO
     */
    TenantDTO toDTO(Tenant tenant);
}
