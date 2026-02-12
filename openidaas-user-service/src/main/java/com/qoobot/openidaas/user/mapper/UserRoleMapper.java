package com.qoobot.openidaas.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openidaas.user.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户角色关联Mapper接口
 *
 * @author QooBot
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 查询用户的所有角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<UserRole> selectByUserId(Long userId);

    /**
     * 查询用户的有效角色（未过期）
     *
     * @param userId 用户ID
     * @return 有效角色列表
     */
    List<UserRole> selectValidByUserId(Long userId);

    /**
     * 查询指定作用域的用户角色
     *
     * @param userId 用户ID
     * @param scopeType 作用域类型
     * @param scopeId 作用域ID
     * @return 角色列表
     */
    List<UserRole> selectByScope(Long userId, Integer scopeType, Long scopeId);

    /**
     * 删除用户的所有角色
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(Long userId);

    /**
     * 批量分配角色
     *
     * @param userRoles 角色列表
     * @return 影响行数
     */
    int batchInsert(@org.apache.ibatis.annotations.Param("list") List<UserRole> userRoles);
}
