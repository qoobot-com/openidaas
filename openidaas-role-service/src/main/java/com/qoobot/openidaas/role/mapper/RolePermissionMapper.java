package com.qoobot.openidaas.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openidaas.role.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色权限关联Mapper接口
 *
 * @author QooBot
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    /**
     * 删除角色的所有权限
     *
     * @param roleId 角色ID
     * @return 删除数量
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除权限关联的所有角色
     *
     * @param permId 权限ID
     * @return 删除数量
     */
    int deleteByPermId(@Param("permId") Long permId);
}
