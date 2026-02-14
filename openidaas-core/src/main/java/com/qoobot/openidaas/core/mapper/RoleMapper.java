package com.qoobot.openidaas.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openidaas.core.domain.Role;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色Mapper接口
 *
 * @author QooBot
 */
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据角色编码查找角色
     */
    @Select("SELECT * FROM roles WHERE role_code = #{roleCode}")
    Role findByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 根据角色名称查找角色
     */
    @Select("SELECT * FROM roles WHERE role_name = #{roleName}")
    Role findByRoleName(@Param("roleName") String roleName);

    /**
     * 根据租户ID查找角色列表（分页）
     */
    IPage<Role> findByTenantId(Page<Role> page, @Param("tenantId") Long tenantId);

    /**
     * 根据角色类型查找角色列表
     */
    @Select("SELECT * FROM roles WHERE role_type = #{roleType}")
    List<Role> findByRoleType(@Param("roleType") String roleType);

    /**
     * 根据租户ID和启用状态查找角色列表
     */
    @Select("SELECT * FROM roles WHERE tenant_id = #{tenantId} AND status = 'ACTIVE'")
    List<Role> findByTenantIdAndEnabledTrue(@Param("tenantId") Long tenantId);

    /**
     * 根据用户ID查找角色列表
     * 【需在XML中实现】涉及中间表关联查询
     */
    List<Role> findByUserId(@Param("userId") Long userId);

    /**
     * 根据权限ID查找角色列表
     * 【需在XML中实现】涉及中间表关联查询
     */
    List<Role> findByPermissionId(@Param("permId") Long permId);

    /**
     * 根据角色编码模糊查询（分页）
     */
    IPage<Role> findByRoleCodeContainingAndTenantId(Page<Role> page, @Param("roleCode") String roleCode, @Param("tenantId") Long tenantId);

    /**
     * 根据角色名称模糊查询（分页）
     */
    IPage<Role> findByRoleNameContainingAndTenantId(Page<Role> page, @Param("roleName") String roleName, @Param("tenantId") Long tenantId);

    /**
     * 统计租户角色数量
     */
    @Select("SELECT COUNT(*) FROM roles WHERE tenant_id = #{tenantId}")
    long countByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 检查角色编码是否已存在
     */
    @Select("SELECT COUNT(*) > 0 FROM roles WHERE role_code = #{roleCode} AND tenant_id = #{tenantId}")
    boolean existsByRoleCodeAndTenantId(@Param("roleCode") String roleCode, @Param("tenantId") Long tenantId);

    /**
     * 查找默认角色
     */
    @Select("SELECT * FROM roles WHERE role_type = 'DEFAULT' AND tenant_id = #{tenantId}")
    List<Role> findDefaultRoles(@Param("tenantId") Long tenantId);

    /**
     * 根据ID集合查询角色
     */
    @Select("<script>" +
            "SELECT * FROM roles WHERE id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Role> findByIdIn(@Param("ids") List<Long> ids);
}
