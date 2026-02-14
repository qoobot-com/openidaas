package com.qoobot.openidaas.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openidaas.core.domain.Application;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 应用Mapper接口
 *
 * @author QooBot
 */
public interface ApplicationMapper extends BaseMapper<Application> {

    /**
     * 根据客户端ID查找应用
     */
    @Select("SELECT * FROM applications WHERE client_id = #{clientId}")
    Application findByClientId(@Param("clientId") String clientId);

    /**
     * 根据应用名称查找应用
     */
    @Select("SELECT * FROM applications WHERE app_name = #{appName}")
    Application findByAppName(@Param("appName") String appName);

    /**
     * 根据租户ID查找应用列表（分页）
     */
    IPage<Application> findByTenantId(Page<Application> page, @Param("tenantId") Long tenantId);

    /**
     * 根据启用状态查找应用列表
     */
    @Select("SELECT * FROM applications WHERE enabled = #{enabled}")
    List<Application> findByEnabled(@Param("enabled") Boolean enabled);

    /**
     * 根据租户ID和启用状态查找应用列表
     */
    @Select("SELECT * FROM applications WHERE tenant_id = #{tenantId} AND enabled = 1")
    List<Application> findByTenantIdAndEnabledTrue(@Param("tenantId") Long tenantId);

    /**
     * 根据应用状态查找应用列表
     */
    @Select("SELECT * FROM applications WHERE status = #{status}")
    List<Application> findByStatus(@Param("status") String status);

    /**
     * 根据应用类型查找应用列表
     */
    @Select("SELECT * FROM applications WHERE app_type = #{appType}")
    List<Application> findByAppType(@Param("appType") String appType);

    /**
     * 根据应用名称模糊查询（分页）
     */
    IPage<Application> findByAppNameContainingAndTenantId(Page<Application> page, @Param("appName") String appName, @Param("tenantId") Long tenantId);

    /**
     * 根据客户端ID模糊查询（分页）
     */
    IPage<Application> findByClientIdContainingAndTenantId(Page<Application> page, @Param("clientId") String clientId, @Param("tenantId") Long tenantId);

    /**
     * 统计租户应用数量
     */
    @Select("SELECT COUNT(*) FROM applications WHERE tenant_id = #{tenantId}")
    long countByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 统计启用应用数量
     */
    @Select("SELECT COUNT(*) FROM applications WHERE tenant_id = #{tenantId} AND enabled = 1")
    long countByTenantIdAndEnabledTrue(@Param("tenantId") Long tenantId);

    /**
     * 检查客户端ID是否已存在
     */
    @Select("SELECT COUNT(*) > 0 FROM applications WHERE client_id = #{clientId}")
    boolean existsByClientId(@Param("clientId") String clientId);

    /**
     * 根据权限ID查找应用列表
     * 【需在XML中实现】涉及中间表关联查询
     */
    List<Application> findByPermissionId(@Param("permId") Long permId);

    /**
     * 查找需要审核的应用
     */
    @Select("SELECT * FROM applications WHERE status = 'pending' AND tenant_id = #{tenantId}")
    List<Application> findPendingApplications(@Param("tenantId") Long tenantId);
}
