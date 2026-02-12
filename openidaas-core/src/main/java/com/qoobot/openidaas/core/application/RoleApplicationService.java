package com.qoobot.openidaas.core.application;

import com.qoobot.openidaas.common.dto.role.RoleCreateDTO;
import com.qoobot.openidaas.common.dto.role.RoleQueryDTO;
import com.qoobot.openidaas.common.dto.role.RoleUpdateDTO;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.common.vo.PageResultVO;
import com.qoobot.openidaas.common.vo.ResultVO;
import com.qoobot.openidaas.common.vo.role.RoleVO;
import com.qoobot.openidaas.core.domain.Role;
import com.qoobot.openidaas.core.repository.RoleRepository;
import com.qoobot.openidaas.core.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色应用服务
 *
 * @author QooBot
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleApplicationService {

    private final RoleService roleService;
    private final RoleRepository roleRepository;

    /**
     * 创建角色
     */
    @Transactional
    public ResultVO<RoleVO> createRole(RoleCreateDTO roleCreateDTO) {
        try {
            // 参数校验
            validateRoleCreateDTO(roleCreateDTO);
            
            // 检查角色编码是否已存在
            if (roleRepository.existsByRoleCodeAndTenantId(roleCreateDTO.getRoleCode(), roleCreateDTO.getTenantId())) {
                throw new BusinessException("角色编码已存在");
            }
            
            // 创建角色
            Role role = roleService.createRole(roleCreateDTO);
            
            // 转换为VO
            RoleVO roleVO = convertToVO(role);
            
            log.info("角色创建成功: {}", role.getRoleName());
            return ResultVO.success(roleVO);
            
        } catch (BusinessException e) {
            log.error("创建角色失败: {}", e.getMessage());
            return ResultVO.error("-1", e.getMessage());
        } catch (Exception e) {
            log.error("创建角色异常", e);
            return ResultVO.error("-1", "创建角色失败");
        }
    }

    /**
     * 更新角色
     */
    @Transactional
    public ResultVO<RoleVO> updateRole(Long roleId, RoleUpdateDTO roleUpdateDTO) {
        try {
            // 检查角色是否存在
            Role existingRole = roleService.getRoleById(roleId);
            if (existingRole == null) {
                throw new BusinessException("角色不存在");
            }
            
            // 检查角色编码是否重复（排除自己）
            if (StringUtils.hasText(roleUpdateDTO.getRoleCode()) && 
                !roleUpdateDTO.getRoleCode().equals(existingRole.getRoleCode()) &&
                roleRepository.existsByRoleCodeAndTenantId(roleUpdateDTO.getRoleCode(), existingRole.getTenantId())) {
                throw new BusinessException("角色编码已存在");
            }
            
            // 更新角色
            Role updatedRole = roleService.updateRole(roleId, roleUpdateDTO);
            
            // 转换为VO
            RoleVO roleVO = convertToVO(updatedRole);
            
            log.info("角色更新成功: {}", roleId);
            return ResultVO.success(roleVO);
            
        } catch (BusinessException e) {
            log.error("更新角色失败: {}", e.getMessage());
            return ResultVO.error("-1", e.getMessage());
        } catch (Exception e) {
            log.error("更新角色异常", e);
            return ResultVO.error("-1", "更新角色失败");
        }
    }

    /**
     * 删除角色
     */
    @Transactional
    public ResultVO<Void> deleteRole(Long roleId) {
        try {
            // 检查角色是否可以删除
            if (!roleService.canDeleteRole(roleId)) {
                throw new BusinessException("该角色已被用户使用，无法删除");
            }
            
            roleService.deleteRole(roleId);
            log.info("角色删除成功: {}", roleId);
            return ResultVO.success();
        } catch (BusinessException e) {
            log.error("删除角色失败: {}", e.getMessage());
            return ResultVO.error("-1", e.getMessage());
        } catch (Exception e) {
            log.error("删除角色异常", e);
            return ResultVO.error("-1", "删除角色失败");
        }
    }

    /**
     * 批量删除角色
     */
    @Transactional
    public ResultVO<Void> deleteRoles(Set<Long> roleIds) {
        try {
            for (Long roleId : roleIds) {
                if (!roleService.canDeleteRole(roleId)) {
                    throw new BusinessException("角色ID " + roleId + " 已被用户使用，无法删除");
                }
            }
            
            roleService.deleteRoles(roleIds);
            log.info("批量删除角色成功: {}", roleIds);
            return ResultVO.success();
        } catch (BusinessException e) {
            log.error("批量删除角色失败: {}", e.getMessage());
            return ResultVO.error("-1", e.getMessage());
        } catch (Exception e) {
            log.error("批量删除角色异常", e);
            return ResultVO.error("-1", "批量删除角色失败");
        }
    }

    /**
     * 查询角色列表
     */
    public ResultVO<PageResultVO<RoleVO>> queryRoles(RoleQueryDTO queryDTO) {
        try {
            PageResultVO<RoleVO> result = roleService.queryRoles(queryDTO);
            return ResultVO.success(result);
        } catch (Exception e) {
            log.error("查询角色列表异常", e);
            return ResultVO.error("-1", "查询角色列表失败");
        }
    }

    /**
     * 根据ID获取角色
     */
    public ResultVO<RoleVO> getRoleById(Long roleId) {
        try {
            Role role = roleService.getRoleById(roleId);
            if (role == null) {
                return ResultVO.error("-1", "角色不存在");
            }
            RoleVO roleVO = convertToVO(role);
            return ResultVO.success(roleVO);
        } catch (Exception e) {
            log.error("获取角色信息异常", e);
            return ResultVO.error("-1", "获取角色信息失败");
        }
    }

    /**
     * 分配权限给角色
     */
    @Transactional
    public ResultVO<Void> assignPermissionsToRole(Long roleId, Set<Long> permIds) {
        try {
            roleService.assignPermissionsToRole(roleId, permIds);
            log.info("角色权限分配成功: 角色ID={}, 权限数量={}", roleId, permIds.size());
            return ResultVO.success();
        } catch (BusinessException e) {
            log.error("分配角色权限失败: {}", e.getMessage());
            return ResultVO.error("-1", e.getMessage());
        } catch (Exception e) {
            log.error("分配角色权限异常", e);
            return ResultVO.error("-1", "分配角色权限失败");
        }
    }

    /**
     * 获取角色权限
     */
    public ResultVO<List<Long>> getRolePermissions(Long roleId) {
        try {
            List<Long> permIds = roleService.getRolePermissionIds(roleId);
            return ResultVO.success(permIds);
        } catch (Exception e) {
            log.error("获取角色权限异常", e);
            return ResultVO.error("-1", "获取角色权限失败");
        }
    }

    /**
     * 启用/禁用角色
     */
    @Transactional
    public ResultVO<Void> toggleRoleStatus(Long roleId, Boolean enabled) {
        try {
            if (enabled) {
                roleService.enableRole(roleId);
            } else {
                roleService.disableRole(roleId);
            }
            log.info("角色状态更新成功: {}, 状态: {}", roleId, enabled);
            return ResultVO.success();
        } catch (BusinessException e) {
            log.error("更新角色状态失败: {}", e.getMessage());
            return ResultVO.error("-1", e.getMessage());
        } catch (Exception e) {
            log.error("更新角色状态异常", e);
            return ResultVO.error("-1", "更新角色状态失败");
        }
    }

    /**
     * 获取所有启用的角色
     */
    public ResultVO<List<RoleVO>> getEnabledRoles(Long tenantId) {
        try {
            List<Role> roles = roleRepository.findByTenantIdAndEnabledTrue(tenantId);
            List<RoleVO> roleVOs = roles.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            return ResultVO.success(roleVOs);
        } catch (Exception e) {
            log.error("获取启用角色列表异常", e);
            return ResultVO.error("-1", "获取启用角色列表失败");
        }
    }

    /**
     * 参数校验
     */
    private void validateRoleCreateDTO(RoleCreateDTO roleCreateDTO) {
        if (!StringUtils.hasText(roleCreateDTO.getRoleCode())) {
            throw new BusinessException("角色编码不能为空");
        }
        if (!StringUtils.hasText(roleCreateDTO.getRoleName())) {
            throw new BusinessException("角色名称不能为空");
        }
        if (roleCreateDTO.getTenantId() == null) {
            throw new BusinessException("租户ID不能为空");
        }
    }

    /**
     * 转换为VO对象
     */
    private RoleVO convertToVO(Role role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        vo.setDescription(role.getDescription());
        vo.setRoleType(role.getRoleType() != null ? role.getRoleType().getCode() : null);
        vo.setEnabled(role.getEnabled());
        vo.setSortOrder(role.getSortOrder());
        vo.setCreatedAt(role.getCreatedAt());
        vo.setUpdatedAt(role.getUpdatedAt());
        vo.setTenantId(role.getTenantId());
        return vo;
    }
}
