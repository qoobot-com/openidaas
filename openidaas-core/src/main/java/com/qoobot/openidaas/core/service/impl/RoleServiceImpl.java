package com.qoobot.openidaas.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openidaas.core.domain.Role;
import com.qoobot.openidaas.core.domain.RolePermission;
import com.qoobot.openidaas.core.mapper.RoleMapper;
import com.qoobot.openidaas.core.mapper.RolePermissionMapper;
import com.qoobot.openidaas.core.mapper.UserRoleMapper;
import com.qoobot.openidaas.core.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色服务实现类（MyBatis-Plus版本）
 *
 * @author QooBot
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    @Transactional
    public Role createRole(Role role) {
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        roleMapper.insert(role);
        return role;
    }

    @Override
    @Transactional
    public Role updateRole(Role role) {
        role.setUpdatedAt(LocalDateTime.now());
        roleMapper.updateById(role);
        return role;
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        rolePermissionMapper.deleteByRoleId(roleId);
        roleMapper.deleteById(roleId);
    }

    @Override
    public Role getRoleById(Long roleId) {
        return roleMapper.selectById(roleId);
    }

    @Override
    public Role getRoleByCode(String roleCode) {
        return roleMapper.findByRoleCode(roleCode);
    }

    @Override
    public List<Role> getRolesByTenantId(Long tenantId) {
        return roleMapper.findByTenantIdAndEnabledTrue(tenantId);
    }

    @Override
    public List<Role> getRolesByUserId(Long userId) {
        return roleMapper.findByUserId(userId);
    }

    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        return rolePermissionMapper.findPermissionIdsByRoleId(roleId);
    }

    @Override
    @Transactional
    public void assignPermissionsToRole(Long roleId, Set<Long> permIds) {
        rolePermissionMapper.deleteByRoleId(roleId);
        if (permIds != null && !permIds.isEmpty()) {
            List<RolePermission> rolePermissions = permIds.stream()
                    .map(permId -> {
                        RolePermission rp = new RolePermission();
                        rp.setRoleId(roleId);
                        rp.setPermissionId(permId);
                        return rp;
                    })
                    .collect(Collectors.toList());
            rolePermissionMapper.batchInsert(rolePermissions);
        }
        log.info("为角色{}分配权限: {}", roleId, permIds);
    }
}
