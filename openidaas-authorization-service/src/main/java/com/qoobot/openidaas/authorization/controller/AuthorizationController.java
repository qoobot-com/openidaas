package com.qoobot.openidaas.authorization.controller;

import com.qoobot.openidaas.authorization.service.AuthorizationService;
import com.qoobot.openidaas.common.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 授权服务Controller
 *
 * @author QooBot
 */
@Slf4j
@RestController
@RequestMapping("/api/authorization")
@RequiredArgsConstructor
@Tag(name = "授权服务", description = "授权相关接口")
public class AuthorizationController {

    private final AuthorizationService authorizationService;

    /**
     * 检查用户权限
     */
    @GetMapping("/check-permission")
    @Operation(summary = "检查用户权限", description = "检查用户是否拥有指定权限")
    public ResultVO<Boolean> checkPermission(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "权限编码") @RequestParam String permCode) {
        boolean hasPermission = authorizationService.hasPermission(userId, permCode);
        return ResultVO.success(hasPermission);
    }

    /**
     * 检查用户是否有任意权限
     */
    @PostMapping("/check-any-permission")
    @Operation(summary = "检查用户是否有任意权限", description = "检查用户是否拥有任意一个指定权限")
    public ResultVO<Boolean> checkAnyPermission(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @RequestBody List<String> permCodes) {
        boolean hasPermission = authorizationService.hasAnyPermission(userId, permCodes);
        return ResultVO.success(hasPermission);
    }

    /**
     * 检查用户是否拥有所有权限
     */
    @PostMapping("/check-all-permissions")
    @Operation(summary = "检查用户是否拥有所有权限", description = "检查用户是否拥有所有指定权限")
    public ResultVO<Boolean> checkAllPermissions(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @RequestBody List<String> permCodes) {
        boolean hasPermission = authorizationService.hasAllPermissions(userId, permCodes);
        return ResultVO.success(hasPermission);
    }

    /**
     * 检查用户角色
     */
    @GetMapping("/check-role")
    @Operation(summary = "检查用户角色", description = "检查用户是否拥有指定角色")
    public ResultVO<Boolean> checkRole(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "角色编码") @RequestParam String roleCode) {
        boolean hasRole = authorizationService.hasRole(userId, roleCode);
        return ResultVO.success(hasRole);
    }

    /**
     * 检查用户是否有任意角色
     */
    @PostMapping("/check-any-role")
    @Operation(summary = "检查用户是否有任意角色", description = "检查用户是否拥有任意一个指定角色")
    public ResultVO<Boolean> checkAnyRole(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @RequestBody List<String> roleCodes) {
        boolean hasRole = authorizationService.hasAnyRole(userId, roleCodes);
        return ResultVO.success(hasRole);
    }

    /**
     * 检查用户是否拥有所有角色
     */
    @PostMapping("/check-all-roles")
    @Operation(summary = "检查用户是否拥有所有角色", description = "检查用户是否拥有所有指定角色")
    public ResultVO<Boolean> checkAllRoles(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @RequestBody List<String> roleCodes) {
        boolean hasRole = authorizationService.hasAllRoles(userId, roleCodes);
        return ResultVO.success(hasRole);
    }

    /**
     * 检查资源访问权限
     */
    @GetMapping("/check-resource")
    @Operation(summary = "检查资源访问权限", description = "检查用户是否有权访问指定资源")
    public ResultVO<Boolean> checkResourceAccess(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "资源类型") @RequestParam String resourceType,
            @Parameter(description = "资源ID") @RequestParam String resourceId,
            @Parameter(description = "操作类型") @RequestParam String action) {
        boolean hasAccess = authorizationService.hasResourceAccess(userId, resourceType, resourceId, action);
        return ResultVO.success(hasAccess);
    }

    /**
     * 获取用户权限列表
     */
    @GetMapping("/users/{userId}/permissions")
    @Operation(summary = "获取用户权限列表", description = "获取用户的所有权限编码列表")
    public ResultVO<List<String>> getUserPermissions(@Parameter(description = "用户ID") @PathVariable Long userId) {
        List<String> permissions = authorizationService.getUserPermissions(userId);
        return ResultVO.success(permissions);
    }

    /**
     * 获取用户角色列表
     */
    @GetMapping("/users/{userId}/roles")
    @Operation(summary = "获取用户角色列表", description = "获取用户的所有角色编码列表")
    public ResultVO<List<String>> getUserRoles(@Parameter(description = "用户ID") @PathVariable Long userId) {
        List<String> roles = authorizationService.getUserRoles(userId);
        return ResultVO.success(roles);
    }

    /**
     * 清除用户权限缓存
     */
    @DeleteMapping("/users/{userId}/cache")
    @Operation(summary = "清除用户权限缓存", description = "清除用户的权限缓存")
    public ResultVO<Void> clearUserCache(@Parameter(description = "用户ID") @PathVariable Long userId) {
        authorizationService.clearUserPermissionCache(userId);
        return ResultVO.success();
    }

    /**
     * 权限检查 (批量) - 符合OpenAPI规范
     */
    @PostMapping("/check")
    @Operation(summary = "权限检查", description = "检查用户是否具有指定权限（支持批量检查）")
    public ResultVO<Map<String, Boolean>> checkPermissions(@RequestBody PermissionCheckRequest request) {
        Long userId = request.getUserId();
        Map<String, Boolean> results = new HashMap<>();
        for (String permission : request.getPermissions()) {
            boolean hasPermission = authorizationService.hasPermission(userId, permission);
            results.put(permission, hasPermission);
        }
        return ResultVO.success(results);
    }

    /**
     * 权限检查请求
     */
    public static class PermissionCheckRequest {
        private Long userId;
        private List<String> permissions;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public List<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(List<String> permissions) {
            this.permissions = permissions;
        }
    }
}
