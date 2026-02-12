package com.qoobot.openidaas.authorization.service;

import java.util.List;

/**
 * 授权服务接口
 *
 * @author QooBot
 */
public interface AuthorizationService {

    /**
     * 检查用户是否拥有指定权限
     *
     * @param userId 用户ID
     * @param permCode 权限编码
     * @return 是否拥有权限
     */
    boolean hasPermission(Long userId, String permCode);

    /**
     * 检查用户是否拥有任意一个指定权限
     *
     * @param userId 用户ID
     * @param permCodes 权限编码列表
     * @return 是否拥有任意一个权限
     */
    boolean hasAnyPermission(Long userId, List<String> permCodes);

    /**
     * 检查用户是否拥有所有指定权限
     *
     * @param userId 用户ID
     * @param permCodes 权限编码列表
     * @return 是否拥有所有权限
     */
    boolean hasAllPermissions(Long userId, List<String> permCodes);

    /**
     * 检查用户是否拥有指定角色
     *
     * @param userId 用户ID
     * @param roleCode 角色编码
     * @return 是否拥有角色
     */
    boolean hasRole(Long userId, String roleCode);

    /**
     * 检查用户是否拥有任意一个指定角色
     *
     * @param userId 用户ID
     * @param roleCodes 角色编码列表
     * @return 是否拥有任意一个角色
     */
    boolean hasAnyRole(Long userId, List<String> roleCodes);

    /**
     * 检查用户是否拥有所有指定角色
     *
     * @param userId 用户ID
     * @param roleCodes 角色编码列表
     * @return 是否拥有所有角色
     */
    boolean hasAllRoles(Long userId, List<String> roleCodes);

    /**
     * 检查用户是否有权访问指定资源
     *
     * @param userId 用户ID
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param action 操作类型
     * @return 是否有权访问
     */
    boolean hasResourceAccess(Long userId, String resourceType, String resourceId, String action);

    /**
     * 获取用户的所有权限编码列表
     *
     * @param userId 用户ID
     * @return 权限编码列表
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 获取用户的所有角色编码列表
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> getUserRoles(Long userId);

    /**
     * 清除用户权限缓存
     *
     * @param userId 用户ID
     */
    void clearUserPermissionCache(Long userId);
}
