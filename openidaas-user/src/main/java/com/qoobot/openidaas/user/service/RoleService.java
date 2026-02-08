package com.qoobot.openidaas.user.service;

import com.qoobot.openidaas.user.dto.RoleDTO;
import com.qoobot.openidaas.user.entity.*;
import com.qoobot.openidaas.user.exception.*;
import com.qoobot.openidaas.user.mapper.UserMapper;
import com.qoobot.openidaas.user.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色服务类
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * 创建角色
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "roles", allEntries = true)
    public RoleDTO createRole(RoleDTO dto) {
        log.info("Creating role: {}", dto.getName());

        // 检查编码是否已存在
        if (roleRepository.findByCode(dto.getCode()).isPresent()) {
            throw new RoleAlreadyExistsException("Role code already exists: " + dto.getCode());
        }

        Role role = userMapper.toEntity(dto);
        Role saved = roleRepository.save(role);

        log.info("Role created successfully: {}", saved.getId());
        return userMapper.toDTO(saved);
    }

    /**
     * 更新角色
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "roles", allEntries = true)
    public RoleDTO updateRole(Long id, RoleDTO dto) {
        log.info("Updating role: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role not found: " + id));

        if (role.getIsSystem()) {
            throw new SystemRoleCannotBeModifiedException("System role cannot be modified");
        }

        role.setName(dto.getName());
        role.setCode(dto.getCode());
        role.setDescription(dto.getDescription());
        role.setLevel(dto.getLevel());
        role.setDataScope(dto.getDataScope());

        Role updated = roleRepository.save(role);
        return userMapper.toDTO(updated);
    }

    /**
     * 删除角色
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "roles", allEntries = true)
    public void deleteRole(Long id) {
        log.info("Deleting role: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role not found: " + id));

        if (role.getIsSystem()) {
            throw new SystemRoleCannotBeModifiedException("System role cannot be deleted");
        }

        roleRepository.deleteById(id);
    }

    /**
     * 获取角色详情
     */
    @Cacheable(value = "roles", key = "#id")
    public RoleDTO getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role not found: " + id));
        return userMapper.toDTO(role);
    }

    /**
     * 获取所有角色
     */
    public List<RoleDTO> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据租户获取角色
     */
    public Page<RoleDTO> getRolesByTenant(Long tenantId, Pageable pageable) {
        Page<Role> roles = roleRepository.findByTenantId(tenantId, pageable);
        return roles.map(userMapper::toDTO);
    }

    /**
     * 搜索角色
     */
    public Page<RoleDTO> searchRoles(String keyword, Pageable pageable) {
        Page<Role> roles = roleRepository.searchRoles(keyword, pageable);
        return roles.map(userMapper::toDTO);
    }

    /**
     * 分配权限
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"roles", "permissions"}, allEntries = true)
    public void assignPermissions(Long roleId, Set<Long> permissionIds) {
        log.info("Assigning permissions to role: {}, permissions: {}", roleId, permissionIds);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not found: " + roleId));

        role.getPermissions().clear();

        for (Long permissionId : permissionIds) {
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new PermissionNotFoundException("Permission not found: " + permissionId));
            role.getPermissions().add(permission);
        }

        roleRepository.save(role);
    }

    /**
     * 获取角色权限
     */
    public Set<PermissionDTO> getRolePermissions(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not found: " + roleId));
        return userMapper.toPermissionDTOSet(role.getPermissions());
    }

    /**
     * 根据用户获取角色
     */
    public List<RoleDTO> getRolesByUser(Long userId) {
        List<Role> roles = roleRepository.findByUserId(userId);
        return roles.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }
}
