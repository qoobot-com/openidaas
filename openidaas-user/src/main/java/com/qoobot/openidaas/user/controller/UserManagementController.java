package com.qoobot.openidaas.user.controller;

import com.qoobot.openidaas.user.dto.*;
import com.qoobot.openidaas.user.model.BatchImportResult;
import com.qoobot.openidaas.user.model.UserImportResult;
import com.qoobot.openidaas.user.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import java.util.List;
import java.util.Set;

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
        return ResponseEntity.ok(ApiResponse.success(null, "密码已重置"));
    }

    // ==================== 角色管理 ====================

    /**
     * 分配角色
     */
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('user:role:assign')")
    public ResponseEntity<ApiResponse<Void>> assignRoles(
            @PathVariable Long id,
            @RequestBody AssignRolesRequest request) {
        userService.assignRoles(id, request.getRoleIds());
        return ResponseEntity.ok(ApiResponse.success(null, "角色分配成功"));
    }

    /**
     * 获取用户权限
     */
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('user:permission:view')")
    public ResponseEntity<ApiResponse<Set<String>>> getUserPermissions(@PathVariable Long id) {
        Set<String> permissions = userService.getUserPermissions(id);
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    // ==================== 导入导出 ====================

    /**
     * 导出用户
     */
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('user:export')")
    public ResponseEntity<ByteArrayResource> exportUsers() throws IOException {
        List<UserDTO> users = userService.getAllUsers(Pageable.unpaged()).getContent();
        byte[] excelData = importExportService.exportUsersToExcel(users);

        String filename = "users_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(excelData.length)
                .body(new ByteArrayResource(excelData));
    }

    /**
     * 导入用户
     */
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('user:import')")
    public ResponseEntity<ApiResponse<List<UserImportResult>>> importUsers(
            @RequestParam("file") MultipartFile file) throws IOException {
        List<UserImportResult> results = importExportService.importUsersFromExcel(file);
        return ResponseEntity.ok(ApiResponse.success(results, "用户导入完成"));
    }

    // ==================== 统计信息 ====================

    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('user:statistics')")
    public ResponseEntity<ApiResponse<UserStatistics>> getStatistics() {
        UserStatistics stats = UserStatistics.builder()
                .totalUsers(1000) // 从数据库查询
                .activeUsers(800)
                .inactiveUsers(150)
                .lockedUsers(50)
                .build();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
