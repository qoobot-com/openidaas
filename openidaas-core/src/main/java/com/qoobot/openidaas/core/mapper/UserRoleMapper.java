package com.qoobot.openidaas.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openidaas.core.domain.UserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色关联Mapper接口
 *
 * @author QooBot
 */
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 根据用户ID查找角色ID列表
     */
    @Select("SELECT role_id FROM user_roles WHERE user_id = #{userId}")
    List<Long> findRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查找用户ID列表
     */
    @Select("SELECT user_id FROM user_roles WHERE role_id = #{roleId}")
    List<Long> findUserIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID删除用户角色关联
     */
    @Delete("DELETE FROM user_roles WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID删除用户角色关联
     */
    @Delete("DELETE FROM user_roles WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除指定用户和角色的关联
     */
    @Delete("DELETE FROM user_roles WHERE user_id = #{userId} AND role_id = #{roleId}")
    int deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 批量插入用户角色关联
     * 【需在XML中实现】
     */
    int batchInsert(@Param("list") List<UserRole> userRoles);
}
