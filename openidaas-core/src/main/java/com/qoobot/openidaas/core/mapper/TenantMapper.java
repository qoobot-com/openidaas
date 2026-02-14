package com.qoobot.openidaas.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openidaas.core.domain.Tenant;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 租户Mapper接口
 *
 * @author QooBot
 */
public interface TenantMapper extends BaseMapper<Tenant> {

    /**
     * 根据租户编码查找租户
     */
    @Select("SELECT * FROM tenants WHERE tenant_code = #{tenantCode}")
    Tenant findByTenantCode(@Param("tenantCode") String tenantCode);

    /**
     * 根据租户名称查找租户
     */
    @Select("SELECT * FROM tenants WHERE tenant_name = #{tenantName}")
    Tenant findByTenantName(@Param("tenantName") String tenantName);

    /**
     * 根据域名查找租户
     */
    @Select("SELECT * FROM tenants WHERE domain = #{domain}")
    Tenant findByDomain(@Param("domain") String domain);

    /**
     * 根据启用状态查找租户列表（分页）
     */
    IPage<Tenant> findByEnabled(Page<Tenant> page, @Param("enabled") Boolean enabled);

    /**
     * 根据租户状态查找租户列表
     */
    @Select("SELECT * FROM tenants WHERE status = #{status}")
    List<Tenant> findByStatus(@Param("status") String status);

    /**
     * 根据过期时间查找即将过期的租户
     */
    @Select("SELECT * FROM tenants WHERE expire_time BETWEEN #{startTime} AND #{endTime} AND enabled = 1")
    List<Tenant> findExpiringTenants(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据租户名称模糊查询（分页）
     */
    IPage<Tenant> findByTenantNameContaining(Page<Tenant> page, @Param("tenantName") String tenantName);

    /**
     * 根据租户编码模糊查询（分页）
     */
    IPage<Tenant> findByTenantCodeContaining(Page<Tenant> page, @Param("tenantCode") String tenantCode);

    /**
     * 统计启用租户数量
     */
    @Select("SELECT COUNT(*) FROM tenants WHERE enabled = 1")
    long countByEnabledTrue();

    /**
     * 统计各状态租户数量
     * 【需在XML中实现】涉及GROUP BY聚合
     */
    List<Object[]> countByStatus();

    /**
     * 检查租户编码是否已存在
     */
    @Select("SELECT COUNT(*) > 0 FROM tenants WHERE tenant_code = #{tenantCode}")
    boolean existsByTenantCode(@Param("tenantCode") String tenantCode);

    /**
     * 检查域名是否已存在
     */
    @Select("SELECT COUNT(*) > 0 FROM tenants WHERE domain = #{domain}")
    boolean existsByDomain(@Param("domain") String domain);

    /**
     * 查找超出租户限制的租户
     */
    @Select("SELECT * FROM tenants WHERE used_users >= max_users OR used_apps >= max_apps")
    List<Tenant> findOverLimitTenants();

    /**
     * 更新租户用户使用数量
     */
    @Update("UPDATE tenants SET used_users = #{usedUsers} WHERE id = #{tenantId}")
    void updateUsedUsers(@Param("tenantId") Long tenantId, @Param("usedUsers") Integer usedUsers);

    /**
     * 更新租户应用使用数量
     */
    @Update("UPDATE tenants SET used_apps = #{usedApps} WHERE id = #{tenantId}")
    void updateUsedApps(@Param("tenantId") Long tenantId, @Param("usedApps") Integer usedApps);
}
