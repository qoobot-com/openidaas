package com.qoobot.openidaas.role.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qoobot.openidaas.common.dto.role.RoleCreateDTO;
import com.qoobot.openidaas.common.dto.role.RoleUpdateDTO;
import com.qoobot.openidaas.common.vo.role.RoleVO;
import com.qoobot.openidaas.role.entity.Role;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author QooBot
 */
public interface RoleService extends IService<Role> {

    /**
     * 获取角色列表
     *
     * @param roleType 角色类型（可选）
     * @return 角色列表
     */
    List<RoleVO> getRoleList(Integer roleType);

    /**
     * 获取角色树
     *
     * @param parentId 父角色ID
     * @return 角色树列表
     */
    List<RoleVO> getRoleTree(Long parentId);

    /**
     * 创建角色
     *
     * @param createDTO 创建角色DTO
     * @return 角色VO
     */
    RoleVO createRole(RoleCreateDTO createDTO);

    /**
     * 更新角色
     *
     * @param updateDTO 更新角色DTO
     * @return 角色VO
     */
    RoleVO updateRole(RoleUpdateDTO updateDTO);

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    void deleteRole(Long id);

    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色VO
     */
    RoleVO getRoleById(Long id);

    /**
     * 分配权限给角色
     *
     * @param roleId 角色ID
     * @param permIds 权限ID列表
     */
    void assignPermissions(Long roleId, List<Long> permIds);

    /**
     * 移除角色的权限
     *
     * @param roleId 角色ID
     * @param permIds 权限ID列表
     */
    void removePermissions(Long roleId, List<Long> permIds);

    /**
     * 获取角色的权限列表
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> getRolePermissions(Long roleId);

    /**
     * 分配角色给用户
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @param scopeType 作用域类型
     * @param scopeId 作用域ID
     * @param expireTime 过期时间
     */
    void assignRoleToUser(Long userId, Long roleId, Integer scopeType, Long scopeId, java.time.LocalDateTime expireTime);

    /**
     * 移除用户的角色
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    void removeRoleFromUser(Long userId, Long roleId);

    /**
     * 获取用户的角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<RoleVO> getUserRoles(Long userId);

    /**
     * 启用角色
     *
     * @param id 角色ID
     */
    void enableRole(Long id);

    /**
     * 禁用角色
     *
     * @param id 角色ID
     */
    void disableRole(Long id);
}
