package com.qoobot.openidaas.core.service;

import com.qoobot.openidaas.common.dto.permission.PermissionCreateDTO;
import com.qoobot.openidaas.common.vo.PageResultVO;
import com.qoobot.openidaas.common.vo.permission.PermissionVO;
import com.qoobot.openidaas.core.domain.Permission;

import java.util.List;
import java.util.Set;

/**
 * 权限服务接口
 *
 * @author QooBot
 */
public interface PermissionService {

    /**
     * 创建权限
     */
    Permission createPermission(PermissionCreateDTO permissionCreateDTO);

    /**
     * 更新权限
     */
    Permission updatePermission(Long permId, PermissionCreateDTO permissionUpdateDTO);

    /**
     * 删除权限
     */
    void deletePermission(Long permId);

    /**
     * 批量删除权限
     */
    void deletePermissions(Set<Long> permIds);

    /**
     * 根据ID获取权限
     */
    Permission getPermissionById(Long permId);

    /**
     * 根据权限编码获取权限
     */
    Permission getPermissionByCode(String permCode);

    /**
     * 查询权限列表
     */
    PageResultVO<PermissionVO> queryPermissions(String permType, String keyword, Long tenantId, int page, int size);

    /**
     * 获取权限树结构
     */
    List<PermissionVO> getPermissionTree(Long tenantId);

    /**
     * 获取用户的权限列表
     */
    List<Permission> getUserPermissions(Long userId);

    /**
     * 获取角色的权限列表
     */
    List<Permission> getRolePermissions(Long roleId);

    /**
     * 获取菜单权限列表
     */
    List<Permission> getMenuPermissions(Long tenantId);

    /**
     * 获取API权限列表
     */
    List<Permission> getApiPermissions(Long tenantId);

    /**
     * 检查用户是否有指定权限
     */
    boolean hasPermission(Long userId, String permission);

    /**
     * 检查权限是否可以删除
     */
    boolean canDeletePermission(Long permId);

    /**
     * 启用权限
     */
    void enablePermission(Long permId);

    /**
     * 禁用权限
     */
    void disablePermission(Long permId);

    /**
     * 获取权限路径
     */
    String getPermissionPath(Long permId);
}