package com.qoobot.openidaas.role.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qoobot.openidaas.common.dto.permission.PermissionCreateDTO;
import com.qoobot.openidaas.common.dto.permission.PermissionUpdateDTO;
import com.qoobot.openidaas.common.vo.permission.PermissionVO;
import com.qoobot.openidaas.role.entity.Permission;

import java.util.List;

/**
 * 权限服务接口
 *
 * @author QooBot
 */
public interface PermissionService extends IService<Permission> {

    /**
     * 获取权限列表
     *
     * @param permType 权限类型（可选）
     * @return 权限列表
     */
    List<PermissionVO> getPermissionList(String permType);

    /**
     * 获取权限树
     *
     * @param parentId 父权限ID
     * @return 权限树列表
     */
    List<PermissionVO> getPermissionTree(Long parentId);

    /**
     * 创建权限
     *
     * @param createDTO 创建权限DTO
     * @return 权限VO
     */
    PermissionVO createPermission(PermissionCreateDTO createDTO);

    /**
     * 更新权限
     *
     * @param updateDTO 更新权限DTO
     * @return 权限VO
     */
    PermissionVO updatePermission(PermissionUpdateDTO updateDTO);

    /**
     * 删除权限
     *
     * @param id 权限ID
     */
    void deletePermission(Long id);

    /**
     * 获取权限详情
     *
     * @param id 权限ID
     * @return 权限VO
     */
    PermissionVO getPermissionById(Long id);

    /**
     * 获取用户的权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<PermissionVO> getUserPermissions(Long userId);

    /**
     * 检查用户是否有指定权限
     *
     * @param userId 用户ID
     * @param permCode 权限编码
     * @return 是否有权限
     */
    boolean hasPermission(Long userId, String permCode);

    /**
     * 获取用户的权限菜单树
     *
     * @param userId 用户ID
     * @return 菜单树
     */
    List<PermissionVO> getUserMenuTree(Long userId);
}
