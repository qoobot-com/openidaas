package com.qoobot.openidaas.user.service;

import com.qoobot.openidaas.user.dto.PermissionDTO;
import com.qoobot.openidaas.user.entity.Permission;
import com.qoobot.openidaas.user.entity.ResourceType;
import com.qoobot.openidaas.user.exception.*;
import com.qoobot.openidaas.user.mapper.UserMapper;
import com.qoobot.openidaas.user.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限服务类
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final UserMapper userMapper;

    /**
     * 创建权限
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "permissions", allEntries = true)
    public PermissionDTO createPermission(PermissionDTO dto) {
        log.info("Creating permission: {}", dto.getName());

        // 检查编码是否已存在
        if (permissionRepository.findByCode(dto.getCode()).isPresent()) {
            throw new PermissionAlreadyExistsException("Permission code already exists: " + dto.getCode());
        }

        Permission permission = userMapper.toEntity(dto);
        Permission saved = permissionRepository.save(permission);

        log.info("Permission created successfully: {}", saved.getId());
        return userMapper.toDTO(saved);
    }

    /**
     * 更新权限
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "permissions", allEntries = true)
    public PermissionDTO updatePermission(Long id, PermissionDTO dto) {
        log.info("Updating permission: {}", id);

        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new PermissionNotFoundException("Permission not found: " + id));

        permission.setName(dto.getName());
        permission.setCode(dto.getCode());
        permission.setDescription(dto.getDescription());
        permission.setResourceType(dto.getResourceType());
        permission.setResourcePath(dto.getResourcePath());
        permission.setHttpMethod(dto.getHttpMethod());
        permission.setSortOrder(dto.getSortOrder());
        permission.setIcon(dto.getIcon());
        permission.setIsVisible(dto.getIsVisible());

        Permission updated = permissionRepository.save(permission);
        return userMapper.toDTO(updated);
    }

    /**
     * 删除权限
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "permissions", allEntries = true)
    public void deletePermission(Long id) {
        log.info("Deleting permission: {}", id);

        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new PermissionNotFoundException("Permission not found: " + id));

        // 检查是否有子权限
        if (!permissionRepository.findByParentId(id).isEmpty()) {
            throw new PermissionHasChildrenException("Cannot delete permission with children");
        }

        permissionRepository.deleteById(id);
    }

    /**
     * 获取权限详情
     */
    @Cacheable(value = "permissions", key = "#id")
    public PermissionDTO getPermissionById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new PermissionNotFoundException("Permission not found: " + id));
        return userMapper.toDTO(permission);
    }

    /**
     * 获取所有权限
     */
    public List<PermissionDTO> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findAll();
        return permissions.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取权限树
     */
    public List<PermissionDTO> getPermissionTree() {
        List<Permission> allPermissions = permissionRepository.findAll();
        List<PermissionDTO> rootPermissions = permissionRepository.findRootPermissions().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());

        buildPermissionTree(rootPermissions, allPermissions);
        return rootPermissions;
    }

    /**
     * 获取菜单权限
     */
    public List<PermissionDTO> getMenuPermissions() {
        List<Permission> permissions = permissionRepository.findVisibleMenus();
        return permissions.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据资源类型获取权限
     */
    public List<PermissionDTO> getPermissionsByResourceType(ResourceType resourceType) {
        List<Permission> permissions = permissionRepository.findByResourceType(resourceType.name());
        return permissions.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 构建权限树
     */
    private void buildPermissionTree(List<PermissionDTO> parentPermissions, List<Permission> allPermissions) {
        for (PermissionDTO parent : parentPermissions) {
            List<PermissionDTO> children = allPermissions.stream()
                    .filter(p -> parent.getId().equals(p.getParentId()))
                    .map(userMapper::toDTO)
                    .collect(Collectors.toList());

            if (!children.isEmpty()) {
                parent.setChildren(children);
                buildPermissionTree(children, allPermissions);
            }
        }
    }
}
