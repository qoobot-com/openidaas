package com.qoobot.openidaas.core.service;

import com.qoobot.openidaas.common.dto.role.RoleCreateDTO;
import com.qoobot.openidaas.common.dto.role.RoleQueryDTO;
import com.qoobot.openidaas.common.dto.role.RoleUpdateDTO;
import com.qoobot.openidaas.common.vo.PageResultVO;
import com.qoobot.openidaas.common.vo.role.RoleVO;
import com.qoobot.openidaas.core.domain.Role;

import java.util.List;
import java.util.Set;

/**
 * 角色服务接口
 *
 * @author QooBot
 */
public interface RoleService {

    /**
     * 创建角色
     */
    Role createRole(RoleCreateDTO roleCreateDTO);

    /**
     * 更新角色
     */
    Role updateRole(Long roleId, RoleUpdateDTO roleUpdateDTO);

    /**
     * 删除角色
     */
    void deleteRole(Long roleId);

    /**
     * 批量删除角色
     */
    void deleteRoles(Set<Long> roleIds);

    /**
     * 根据ID获取角色
     */
    Role getRoleById(Long roleId);

    /**
     * 根据角色编码获取角色
     */
    Role getRoleByCode(String roleCode);

    /**
     * 查询角色列表
     */
    PageResultVO<RoleVO> queryRoles(RoleQueryDTO queryDTO);

    /**
     * 获取角色拥有的权限
     */
    List<Long> getRolePermissionIds(Long roleId);

    /**
     * 分配权限给角色
     */
    void assignPermissionsToRole(Long roleId, Set<Long> permIds);

    /**
     * 获取拥有指定角色的用户列表
     */
    List<Long> getUsersByRoleId(Long roleId);

    /**
     * 启用角色
     */
    void enableRole(Long roleId);

    /**
     * 禁用角色
     */
    void disableRole(Long roleId);

    /**
     * 检查角色是否可以删除
     */
    boolean canDeleteRole(Long roleId);

    /**
     * 获取默认角色
     */
    List<Role> getDefaultRoles(Long tenantId);

    /**
     * 复制角色
     */
    Role copyRole(Long roleId, String newRoleCode, String newRoleName);
}