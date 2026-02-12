package com.qoobot.openidaas.role.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qoobot.openidaas.common.dto.role.RoleCreateDTO;
import com.qoobot.openidaas.common.dto.role.RoleUpdateDTO;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.common.vo.role.RoleVO;
import com.qoobot.openidaas.role.entity.Role;
import com.qoobot.openidaas.role.entity.RolePermission;
import com.qoobot.openidaas.role.entity.UserRole;
import com.qoobot.openidaas.role.mapper.RoleMapper;
import com.qoobot.openidaas.role.mapper.RolePermissionMapper;
import com.qoobot.openidaas.role.mapper.UserRoleMapper;
import com.qoobot.openidaas.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 *
 * @author QooBot
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public List<RoleVO> getRoleList(Integer roleType) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (roleType != null) {
            wrapper.eq(Role::getRoleType, roleType);
        }
        wrapper.orderByAsc(Role::getSortOrder, Role::getId);
        List<Role> roles = roleMapper.selectList(wrapper);
        return roles.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<RoleVO> getRoleTree(Long parentId) {
        Long actualParentId = parentId == null ? 0L : parentId;
        List<Role> roles = roleMapper.selectList(new LambdaQueryWrapper<Role>()
                .eq(Role::getParentId, actualParentId)
                .orderByAsc(Role::getSortOrder, Role::getId));
        return buildRoleTree(roles);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleVO createRole(RoleCreateDTO createDTO) {
        log.info("创建角色，角色编码：{}，角色名称：{}", createDTO.getRoleCode(), createDTO.getRoleName());

        // 检查角色编码是否已存在
        Role existRole = roleMapper.selectByCode(createDTO.getRoleCode());
        if (existRole != null) {
            throw new BusinessException("角色编码已存在：" + createDTO.getRoleCode());
        }

        // 验证父角色是否存在
        Long parentId = createDTO.getParentId() == null ? 0L : createDTO.getParentId();
        if (parentId != 0L) {
            Role parentRole = roleMapper.selectById(parentId);
            if (parentRole == null) {
                throw new BusinessException("父角色不存在");
            }
        }

        // 构建角色实体
        Role role = new Role();
        BeanUtils.copyProperties(createDTO, role);
        role.setParentId(parentId);

        roleMapper.insert(role);

        log.info("角色创建成功，角色ID：{}", role.getId());
        return convertToVO(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleVO updateRole(RoleUpdateDTO updateDTO) {
        log.info("更新角色，角色ID：{}", updateDTO.getId());

        // 检查角色是否存在
        Role existRole = roleMapper.selectById(updateDTO.getId());
        if (existRole == null) {
            throw new BusinessException("角色不存在");
        }

        // 检查角色编码是否重复（排除自己）
        Role codeRole = roleMapper.selectByCode(updateDTO.getRoleCode());
        if (codeRole != null && !codeRole.getId().equals(updateDTO.getId())) {
            throw new BusinessException("角色编码已存在：" + updateDTO.getRoleCode());
        }

        // 更新角色信息
        Role role = new Role();
        BeanUtils.copyProperties(updateDTO, role);
        roleMapper.updateById(role);

        log.info("角色更新成功，角色ID：{}", updateDTO.getId());
        return convertToVO(roleMapper.selectById(updateDTO.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        log.info("删除角色，角色ID：{}", id);

        // 检查角色是否存在
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 检查是否为内置角色
        if (role.isBuiltin()) {
            throw new BusinessException("内置角色不能删除");
        }

        // 检查是否有子角色
        int childrenCount = roleMapper.countChildren(id);
        if (childrenCount > 0) {
            throw new BusinessException("角色下存在子角色，无法删除");
        }

        // 检查是否有用户使用该角色
        int userCount = roleMapper.countUsers(id);
        if (userCount > 0) {
            throw new BusinessException("角色下存在用户，无法删除");
        }

        // 删除角色权限关联
        rolePermissionMapper.deleteByRoleId(id);

        // 删除角色
        roleMapper.deleteById(id);

        log.info("角色删除成功，角色ID：{}", id);
    }

    @Override
    public RoleVO getRoleById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return convertToVO(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permIds) {
        log.info("分配权限给角色，roleId: {}, permIds: {}", roleId, permIds);

        // 检查角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 删除原有权限
        rolePermissionMapper.deleteByRoleId(roleId);

        // 批量插入新权限
        if (permIds != null && !permIds.isEmpty()) {
            for (Long permId : permIds) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermId(permId);
                rolePermission.setCreatedAt(LocalDateTime.now());
                rolePermissionMapper.insert(rolePermission);
            }
        }

        log.info("权限分配成功，roleId: {}, count: {}", roleId, permIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removePermissions(Long roleId, List<Long> permIds) {
        log.info("移除角色的权限，roleId: {}, permIds: {}", roleId, permIds);

        // 检查角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 删除指定权限
        for (Long permId : permIds) {
            rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>()
                    .eq(RolePermission::getRoleId, roleId)
                    .eq(RolePermission::getPermId, permId));
        }

        log.info("权限移除成功，roleId: {}, count: {}", roleId, permIds.size());
    }

    @Override
    public List<Long> getRolePermissions(Long roleId) {
        return rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, roleId))
                .stream()
                .map(RolePermission::getPermId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoleToUser(Long userId, Long roleId, Integer scopeType, Long scopeId, LocalDateTime expireTime) {
        log.info("分配角色给用户，userId: {}, roleId: {}", userId, roleId);

        // 检查角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 检查是否已存在
        List<UserRole> existRoles = userRoleMapper.selectByUserId(userId);
        for (UserRole userRole : existRoles) {
            if (userRole.getRoleId().equals(roleId)) {
                throw new BusinessException("用户已拥有该角色");
            }
        }

        // 创建用户角色关联
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setScopeType(scopeType);
        userRole.setScopeId(scopeId);
        userRole.setGrantedBy(null); // 可从上下文获取当前操作人
        userRole.setGrantReason("管理员分配");
        userRole.setGrantTime(LocalDateTime.now());
        userRole.setExpireTime(expireTime);
        userRole.setIsTemporary(expireTime != null ? 1 : 0);
        userRoleMapper.insert(userRole);

        log.info("角色分配成功，userId: {}, roleId: {}", userId, roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRoleFromUser(Long userId, Long roleId) {
        log.info("移除用户的角色，userId: {}, roleId: {}", userId, roleId);

        userRoleMapper.deleteByUserIdAndRoleId(userId, roleId);

        log.info("角色移除成功，userId: {}, roleId: {}", userId, roleId);
    }

    @Override
    public List<RoleVO> getUserRoles(Long userId) {
        List<Role> roles = roleMapper.selectByUserId(userId);
        return roles.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableRole(Long id) {
        log.info("启用角色，角色ID：{}", id);

        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        role.setEnabled(1);
        roleMapper.updateById(role);

        log.info("角色启用成功，角色ID：{}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableRole(Long id) {
        log.info("禁用角色，角色ID：{}", id);

        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        role.setEnabled(0);
        roleMapper.updateById(role);

        log.info("角色禁用成功，角色ID：{}", id);
    }

    /**
     * 构建角色树
     */
    private List<RoleVO> buildRoleTree(List<Role> roles) {
        List<RoleVO> voList = roles.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 为每个角色递归加载子角色
        for (RoleVO vo : voList) {
            List<Role> children = roleMapper.selectList(new LambdaQueryWrapper<Role>()
                    .eq(Role::getParentId, vo.getId())
                    .orderByAsc(Role::getId));
            if (!children.isEmpty()) {
                vo.setChildren(buildRoleTree(children));
            }
        }

        return voList;
    }

    /**
     * 转换为VO
     */
    private RoleVO convertToVO(Role role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        return vo;
    }
}
