package com.qoobot.openidaas.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openidaas.role.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色Mapper接口
 *
 * @author QooBot
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据角色编码查询角色
     *
     * @param roleCode 角色编码
     * @return 角色信息
     */
    Role selectByCode(@Param("roleCode") String roleCode);

    /**
     * 根据用户ID查询角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> selectByUserId(@Param("userId") Long userId);

    /**
     * 检查角色是否有子角色
     *
     * @param roleId 角色ID
     * @return 子角色数量
     */
    @Select("SELECT COUNT(*) FROM roles WHERE parent_id = #{roleId} AND deleted = 0")
    int countChildren(@Param("roleId") Long roleId);

    /**
     * 检查角色下是否有用户
     *
     * @param roleId 角色ID
     * @return 用户数量
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM user_roles WHERE role_id = #{roleId} AND deleted = 0")
    int countUsers(@Param("roleId") Long roleId);
}
