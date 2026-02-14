package com.qoobot.openidaas.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openidaas.core.domain.Permission;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper接口
 *
 * @author QooBot
 */
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据权限编码查找权限
     */
    @Select("SELECT * FROM permissions WHERE perm_code = #{permCode}")
    Permission findByPermCode(@Param("permCode") String permCode);

    /**
     * 根据权限名称查找权限
     */
    @Select("SELECT * FROM permissions WHERE perm_name = #{permName}")
    Permission findByPermName(@Param("permName") String permName);

    /**
     * 根据租户ID查找权限列表（分页）
     */
    IPage<Permission> findByTenantId(Page<Permission> page, @Param("tenantId") Long tenantId);

    /**
     * 根据父权限ID查找子权限列表
     */
    @Select("SELECT * FROM permissions WHERE parent_id = #{parentId}")
    List<Permission> findByParentId(@Param("parentId") Long parentId);

    /**
     * 根据权限类型查找权限列表
     */
    @Select("SELECT * FROM permissions WHERE perm_type = #{permType}")
    List<Permission> findByPermType(@Param("permType") String permType);

    /**
     * 根据租户ID查找顶级权限列表
     */
    @Select("SELECT * FROM permissions WHERE parent_id IS NULL AND tenant_id = #{tenantId} ORDER BY sort_order")
    List<Permission> findTopPermissionsByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据角色ID查找权限列表
     * 【需在XML中实现】涉及中间表关联查询
     */
    List<Permission> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查找权限列表
     * 【需在XML中实现】涉及多表关联查询
     */
    List<Permission> findByUserId(@Param("userId") Long userId);

    /**
     * 根据权限编码模糊查询（分页）
     */
    IPage<Permission> findByPermCodeContainingAndTenantId(Page<Permission> page, @Param("permCode") String permCode, @Param("tenantId") Long tenantId);

    /**
     * 根据权限名称模糊查询（分页）
     */
    IPage<Permission> findByPermNameContainingAndTenantId(Page<Permission> page, @Param("permName") String permName, @Param("tenantId") Long tenantId);

    /**
     * 统计租户权限数量
     */
    @Select("SELECT COUNT(*) FROM permissions WHERE tenant_id = #{tenantId}")
    long countByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 检查权限编码是否已存在
     */
    @Select("SELECT COUNT(*) > 0 FROM permissions WHERE perm_code = #{permCode} AND tenant_id = #{tenantId}")
    boolean existsByPermCodeAndTenantId(@Param("permCode") String permCode, @Param("tenantId") Long tenantId);

    /**
     * 根据路径和方法查找API权限
     */
    @Select("SELECT * FROM permissions WHERE path = #{path} AND method = #{method} AND perm_type = 'api' AND tenant_id = #{tenantId}")
    Permission findByPathAndMethodAndTenantId(@Param("path") String path, @Param("method") String method, @Param("tenantId") Long tenantId);

    /**
     * 查找菜单权限
     */
    @Select("SELECT * FROM permissions WHERE perm_type = 'menu' AND tenant_id = #{tenantId} ORDER BY sort_order")
    List<Permission> findMenuPermissionsByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 查找按钮权限
     */
    @Select("SELECT * FROM permissions WHERE perm_type = 'button' AND tenant_id = #{tenantId}")
    List<Permission> findButtonPermissionsByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据ID集合查询权限
     */
    @Select("<script>" +
            "SELECT * FROM permissions WHERE id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Permission> findByIdIn(@Param("ids") List<Long> ids);
}
