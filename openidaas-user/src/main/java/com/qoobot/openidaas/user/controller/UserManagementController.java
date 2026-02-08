package com.qoobot.openidaas.user.controller;

import com.qoobot.openidaas.user.dto.*;
import com.qoobot.openidaas.user.entity.*;
import com.qoobot.openidaas.user.exception.*;
import com.qoobot.openidaas.user.model.*;
import com.qoobot.openidaas.user.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 用户管理控制器
 * 
 * 提供完整的用户管理 REST API
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserService userService;
    private final DepartmentService departmentService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final UserImportExportService importExportService;

    // ==================== 用户CRUD操作 ====================

    /**
     * 创建用户
     */
    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDTO user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "用户创建成功"));
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:view') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserDTO>> getUser(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 分页查询用户
     */
    @GetMapping
    @PreAuthorize("hasAuthority('user:view')")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        Pageable pageable = PageRequest.of(
                page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<UserDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * 搜索用户
     */
    @PostMapping("/search")
    @PreAuthorize("hasAuthority('user:view')")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> searchUsers(
            @RequestBody UserSearchRequest request) {
        Page<UserDTO> users = userService.searchUsers(request);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserDTO user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(user, "用户更新成功"));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        userService.deleteUser(id, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "用户删除成功"));
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<ApiResponse<Void>> batchDeleteUsers(
            @RequestBody List<Long> ids,
            @RequestParam(required = false) String reason) {
        for (Long id : ids) {
            userService.deleteUser(id, reason);
        }
        return ResponseEntity.ok(ApiResponse.success(null, "批量删除成功"));
    }

    // ==================== 用户状态管理 ====================

    /**
     * 激活用户
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('user:activate')")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "用户已激活"));
    }

    /**
     * 停用用户
     */
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('user:disable')")
    public ResponseEntity<ApiResponse<Void>> disableUser(@PathVariable Long id) {
        userService.disableUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "用户已停用"));
    }

    /**
     * 锁定用户
     */
    @PutMapping("/{id}/lock")
    @PreAuthorize("hasAuthority('user:lock')")
    public ResponseEntity<ApiResponse<Void>> lockUser(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "30") int lockMinutes) {
        userService.lockUser(id, LocalDateTime.now().plusMinutes(lockMinutes));
        return ResponseEntity.ok(ApiResponse.success(null, "用户已锁定"));
    }

    /**
     * 解锁用户
     */
    @PutMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('user:unlock')")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable Long id) {
        userService.unlockUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "用户已解锁"));
    }

    // ==================== 密码管理 ====================

    /**
     * 修改密码
     */
    @PutMapping("/{id}/password")
    @PreAuthorize("hasAuthority('user:password') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(null, "密码修改成功"));
    }

    /**
     * 重置密码（管理员）
     */
    @PutMapping("/{id}/password/reset")
    @PreAuthorize("hasAuthority('user:password:reset')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id,
            @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(null, "密码重置成功"));
    }

    // ==================== 部门管理 ====================

    /**
     * 创建部门
     */
    @PostMapping("/departments")
    @PreAuthorize("hasAuthority('department:create')")
    public ResponseEntity<ApiResponse<DepartmentDTO>> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setName(request.getName());
        dto.setCode(request.getCode());
        dto.setDescription(request.getDescription());
        dto.setParentId(request.getParentId());
        dto.setLeaderId(request.getLeaderId());
        dto.setLeaderName(request.getLeaderName());
        dto.setPhone(request.getPhone());
        dto.setEmail(request.getEmail());
        dto.setAddress(request.getAddress());
        dto.setSortOrder(request.getSortOrder());
        
        DepartmentDTO department = departmentService.createDepartment(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(department, "部门创建成功"));
    }

    /**
     * 获取部门树
     */
    @GetMapping("/departments/tree")
    @PreAuthorize("hasAuthority('department:view')")
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> getDepartmentTree(
            @RequestParam(required = false) Long tenantId) {
        List<DepartmentDTO> departments = departmentService.getDepartmentTree(tenantId);
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    // ==================== 角色管理 ====================

    /**
     * 创建角色
     */
    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('role:create')")
    public ResponseEntity<ApiResponse<RoleDTO>> createRole(
            @Valid @RequestBody CreateRoleRequest request) {
        RoleDTO dto = new RoleDTO();
        dto.setName(request.getName());
        dto.setCode(request.getCode());
        dto.setDescription(request.getDescription());
        dto.setTenantId(request.getTenantId());
        // Level 和 DataScope 使用默认值
        
        RoleDTO role = roleService.createRole(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(role, "角色创建成功"));
    }

    /**
     * 获取所有角色
     */
    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('role:view')")
    public ResponseEntity<ApiResponse<List<RoleDTO>>> getAllRoles() {
        List<RoleDTO> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    // ==================== 权限管理 ====================

    /**
     * 创建权限
     */
    @PostMapping("/permissions")
    @PreAuthorize("hasAuthority('permission:create')")
    public ResponseEntity<ApiResponse<PermissionDTO>> createPermission(
            @Valid @RequestBody CreatePermissionRequest request) {
        PermissionDTO dto = new PermissionDTO();
        dto.setName(request.getName());
        dto.setCode(request.getCode());
        dto.setDescription(request.getDescription());
        dto.setResourceType(com.qoobot.openidaas.user.entity.ResourceType.valueOf(request.getResource().toUpperCase()));
        dto.setResourcePath(request.getAction());
        dto.setHttpMethod("GET"); // 默认值
        dto.setSortOrder(request.getSortOrder());
        dto.setIcon(request.getIcon());
        dto.setIsVisible(request.getIsVisible());
        dto.setParentId(request.getParentId());
        
        PermissionDTO permission = permissionService.createPermission(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(permission, "权限创建成功"));
    }

    /**
     * 获取权限树
     */
    @GetMapping("/permissions/tree")
    @PreAuthorize("hasAuthority('permission:view')")
    public ResponseEntity<ApiResponse<List<PermissionDTO>>> getPermissionTree() {
        List<PermissionDTO> permissions = permissionService.getPermissionTree();
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    // ==================== 数据导入导出 ====================

    /**
     * 下载用户模板
     */
    @GetMapping("/export/template")
    @PreAuthorize("hasAuthority('user:export')")
    public ResponseEntity<ByteArrayResource> downloadUserTemplate() throws IOException {
        // 创建一个空的用户列表用于生成模板
        List<UserDTO> emptyUsers = new ArrayList<>();
        byte[] templateData = importExportService.exportUsersToExcel(emptyUsers);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", 
                "user_template_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(new ByteArrayResource(templateData));
    }

    /**
     * 导入用户数据
     */
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('user:import')")
    public ResponseEntity<ApiResponse<BatchImportResult>> importUsers(
            @RequestParam("file") MultipartFile file) {
        try {
            List<UserImportResult> importResults = importExportService.importUsersFromExcel(file);
            
            BatchImportResult result = new BatchImportResult();
            result.setTotalCount(importResults.size());
            result.setSuccessCount((int) importResults.stream().filter(UserImportResult::getSuccess).count());
            result.setFailureCount((int) importResults.stream().filter(r -> !r.getSuccess()).count());
            
            for (UserImportResult importResult : importResults) {
                if (importResult.getSuccess()) {
                    result.addUserId(importResult.getUserId());
                } else {
                    result.addError("行" + importResult.getRowNum() + ": " + importResult.getMessage());
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (IOException e) {
            log.error("Failed to import users", e);
            BatchImportResult errorResult = new BatchImportResult();
            errorResult.setFailureCount(1);
            errorResult.addError("文件处理失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("导入失败"));
        }
    }

    /**
     * 导出用户数据
     */
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('user:export')")
    public ResponseEntity<ByteArrayResource> exportUsers(
            @RequestParam(required = false) Set<String> fields) throws IOException {
        // 获取所有用户进行导出
        Page<UserDTO> userPage = userService.getAllUsers(PageRequest.of(0, 10000));
        List<UserDTO> allUsers = userPage.getContent();
        byte[] exportData = importExportService.exportUsersToExcel(allUsers);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", 
                "users_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(new ByteArrayResource(exportData));
    }
}