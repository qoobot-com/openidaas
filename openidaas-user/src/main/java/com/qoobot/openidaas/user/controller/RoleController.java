package com.qoobot.openidaas.user.controller;

import com.qoobot.openidaas.user.dto.PermissionDTO;
import com.qoobot.openidaas.user.dto.RoleDTO;
import com.qoobot.openidaas.user.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 角色管理控制器
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 创建角色
     */
    @PostMapping
    @PreAuthorize("hasAuthority('role:create')")
    public ResponseEntity<ApiResponse<RoleDTO>> createRole(@Valid @RequestBody RoleDTO dto) {
        RoleDTO role = roleService.createRole(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(role, "角色创建成功"));
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:update')")
    public ResponseEntity<ApiResponse<RoleDTO>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleDTO dto) {
        RoleDTO role = roleService.updateRole(id, dto);
        return ResponseEntity.ok(ApiResponse.success(role, "角色更新成功"));
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success(null, "角色删除成功"));
    }

    /**
     * 获取角色详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role:view')")
    public ResponseEntity<ApiResponse<RoleDTO>> getRole(@PathVariable Long id) {
        RoleDTO role = roleService.getRoleById(id);
        return ResponseEntity.ok(ApiResponse.success(role));
    }

    /**
     * 获取所有角色
     */
    @GetMapping
    @PreAuthorize("hasAuthority('role:view')")
    public ResponseEntity<ApiResponse<List<RoleDTO>>> getAllRoles() {
        List<RoleDTO> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    /**
     * 搜索角色
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('role:view')")
    public ResponseEntity<ApiResponse<Page<RoleDTO>>> searchRoles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RoleDTO> roles = roleService.searchRoles(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    /**
     * 分配权限
     */
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('role:permission:assign')")
    public ResponseEntity<ApiResponse<Void>> assignPermissions(
            @PathVariable Long id,
            @RequestBody Set<Long> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return ResponseEntity.ok(ApiResponse.success(null, "权限分配成功"));
    }

    /**
     * 获取角色权限
     */
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('role:permission:view')")
    public ResponseEntity<ApiResponse<Set<PermissionDTO>>> getRolePermissions(@PathVariable Long id) {
        Set<PermissionDTO> permissions = roleService.getRolePermissions(id);
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }
}
