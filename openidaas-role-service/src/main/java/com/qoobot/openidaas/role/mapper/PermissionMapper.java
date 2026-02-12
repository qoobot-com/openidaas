package com.qoobot.openidaas.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openidaas.role.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper接口
 *
 * @author QooBot
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据权限编码查询权限
     *
     * @param permCode 权限编码
     * @return 权限信息
     */
    Permission selectByCode(@Param("permCode") String permCode);

    /**
     * 根据角色ID查询权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查询权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<Permission> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据父权限ID查询子权限列表
     *
     * @param parentId 父权限ID
     * @return 权限列表
     */
    List<Permission> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 检查权限下是否有子权限
     *
     * @param permId 权限ID
     * @return 子权限数量
     */
    @Select("SELECT COUNT(*) FROM permissions WHERE parent_id = #{permId} AND deleted = 0")
    int countChildren(@Param("permId") Long permId);

    /**
     * 检查权限关联的角色数量
     *
     * @param permId 权限ID
     * @return 角色数量
     */
    @Select("SELECT COUNT(DISTINCT role_id) FROM role_permissions WHERE perm_id = #{permId} AND deleted = 0")
    int countRoles(@Param("permId") Long permId);
}
