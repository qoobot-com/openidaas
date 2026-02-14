package com.qoobot.openidaas.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openidaas.core.domain.RolePermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色权限关联Mapper接口
 *
 * @author QooBot
 */
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    /**
     * 根据角色ID查找权限ID列表
     */
    @Select("SELECT permission_id FROM role_permissions WHERE role_id = #{roleId}")
    List<Long> findPermissionIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限ID查找角色ID列表
     */
    @Select("SELECT role_id FROM role_permissions WHERE permission_id = #{permissionId}")
    List<Long> findRoleIdsByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 根据角色ID删除角色权限关联
     */
    @Delete("DELETE FROM role_permissions WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限ID删除角色权限关联
     */
    @Delete("DELETE FROM role_permissions WHERE permission_id = #{permissionId}")
    int deleteByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 删除指定角色和权限的关联
     */
    @Delete("DELETE FROM role_permissions WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    int deleteByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * 批量插入角色权限关联
     * 【需在XML中实现】
     */
    int batchInsert(@Param("list") List<RolePermission> rolePermissions);
}
