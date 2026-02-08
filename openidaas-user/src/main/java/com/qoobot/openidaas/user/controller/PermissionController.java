package com.qoobot.openidaas.user.controller;

import com.qoobot.openidaas.user.dto.PermissionDTO;
import com.qoobot.openidaas.user.entity.ResourceType;
import com.qoobot.openidaas.user.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制器
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 创建权限
     */
    @PostMapping
    @PreAuthorize("hasAuthority('permission:create')")
    public ResponseEntity<ApiResponse<PermissionDTO>> createPermission(@Valid @RequestBody PermissionDTO dto) {
        PermissionDTO permission = permissionService.createPermission(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(permission, "权限创建成功"));
    }

    /**
     * 更新权限
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:update')")
    public ResponseEntity<ApiResponse<PermissionDTO>> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody PermissionDTO dto) {
        PermissionDTO permission = permissionService.updatePermission(id, dto);
        return ResponseEntity.ok(ApiResponse.success(permission, "权限更新成功"));
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:delete')")
    public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok(ApiResponse.success(null, "权限删除成功"));
    }

    /**
     * 获取权限详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:view')")
    public ResponseEntity<ApiResponse<PermissionDTO>> getPermission(@PathVariable Long id) {
        PermissionDTO permission = permissionService.getPermissionById(id);
        return ResponseEntity.ok(ApiResponse.success(permission));
    }

    /**
     * 获取所有权限
     */
    @GetMapping
    @PreAuthorize("hasAuthority('permission:view')")
    public ResponseEntity<ApiResponse<List<PermissionDTO>>> getAllPermissions() {
        List<PermissionDTO> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    /**
     * 获取权限树
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('permission:view')")
    public ResponseEntity<ApiResponse<List<PermissionDTO>>> getPermissionTree() {
        List<PermissionDTO> tree = permissionService.getPermissionTree();
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    /**
     * 获取菜单权限
     */
    @GetMapping("/menu")
    @PreAuthorize("hasAuthority('permission:view')")
    public ResponseEntity<ApiResponse<List<PermissionDTO>>> getMenuPermissions() {
        List<PermissionDTO> permissions = permissionService.getMenuPermissions();
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    /**
     * 根据资源类型获取权限
     */
    @GetMapping("/type/{resourceType}")
    @PreAuthorize("hasAuthority('permission:view')")
    public ResponseEntity<ApiResponse<List<PermissionDTO>>> getPermissionsByType(
            @PathVariable ResourceType resourceType) {
        List<PermissionDTO> permissions = permissionService.getPermissionsByResourceType(resourceType);
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }
}
