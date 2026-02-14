package com.qoobot.openidaas.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qoobot.openidaas.core.domain.Permission;
import com.qoobot.openidaas.core.mapper.PermissionMapper;
import com.qoobot.openidaas.core.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限服务实现类（MyBatis-Plus版本）
 *
 * @author QooBot
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {

    private final PermissionMapper permissionMapper;

    @Override
    @Transactional
    public Permission createPermission(Permission permission) {
        permission.setCreatedAt(LocalDateTime.now());
        permission.setUpdatedAt(LocalDateTime.now());
        permissionMapper.insert(permission);
        return permission;
    }

    @Override
    @Transactional
    public Permission updatePermission(Permission permission) {
        permission.setUpdatedAt(LocalDateTime.now());
        permissionMapper.updateById(permission);
        return permission;
    }

    @Override
    @Transactional
    public void deletePermission(Long permId) {
        permissionMapper.deleteById(permId);
    }

    @Override
    public Permission getPermissionById(Long permId) {
        return permissionMapper.selectById(permId);
    }

    @Override
    public Permission getPermissionByCode(String permCode) {
        return permissionMapper.findByPermCode(permCode);
    }

    @Override
    public List<Permission> getPermissionsByTenantId(Long tenantId) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getTenantId, tenantId);
        return permissionMapper.selectList(wrapper);
    }

    @Override
    public List<Permission> getPermissionsByUserId(Long userId) {
        return permissionMapper.findByUserId(userId);
    }

    @Override
    public List<Permission> getPermissionsByRoleId(Long roleId) {
        return permissionMapper.findByRoleId(roleId);
    }

    @Override
    public List<Permission> getMenuPermissions(Long tenantId) {
        return permissionMapper.findMenuPermissionsByTenantId(tenantId);
    }

    @Override
    public List<Permission> getApiPermissions(Long tenantId) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getPermType, "api")
               .eq(Permission::getTenantId, tenantId);
        return permissionMapper.selectList(wrapper);
    }

    @Override
    public boolean hasPermission(Long userId, String permCode) {
        List<Permission> permissions = permissionMapper.findByUserId(userId);
        return permissions.stream()
                .anyMatch(p -> permCode.equals(p.getPermCode()));
    }

    @Override
    @Transactional
    public void enablePermission(Long permId) {
        Permission permission = getPermissionById(permId);
        if (permission != null) {
            permission.setEnabled(true);
            permission.setUpdatedAt(LocalDateTime.now());
            permissionMapper.updateById(permission);
        }
    }

    @Override
    @Transactional
    public void disablePermission(Long permId) {
        Permission permission = getPermissionById(permId);
        if (permission != null) {
            permission.setEnabled(false);
            permission.setUpdatedAt(LocalDateTime.now());
            permissionMapper.updateById(permission);
        }
    }
}
