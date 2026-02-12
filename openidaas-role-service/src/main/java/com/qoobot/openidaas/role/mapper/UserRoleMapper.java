package com.qoobot.openidaas.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openidaas.role.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色关联Mapper接口
 *
 * @author QooBot
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 根据用户ID查询角色关联列表
     *
     * @param userId 用户ID
     * @return 用户角色关联列表
     */
    List<UserRole> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询用户列表
     *
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    @Select("SELECT DISTINCT user_id FROM user_roles WHERE role_id = #{roleId} AND deleted = 0")
    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除用户的角色
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 删除数量
     */
    int deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 删除用户的所有角色
     *
     * @param userId 用户ID
     * @return 删除数量
     */
    int deleteByUserId(@Param("userId") Long userId);
}
