package com.qoobot.openidaas.role.controller;

import com.qoobot.openidaas.common.dto.role.RoleCreateDTO;
import com.qoobot.openidaas.common.dto.role.RoleUpdateDTO;
import com.qoobot.openidaas.common.vo.ResultVO;
import com.qoobot.openidaas.common.vo.role.RoleVO;
import com.qoobot.openidaas.role.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理Controller
 *
 * @author QooBot
 */
@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "角色管理相关接口")
public class RoleController {

    private final RoleService roleService;

    /**
     * 获取角色列表
     */
    @GetMapping
    @Operation(summary = "获取角色列表", description = "查询角色信息列表")
    public ResultVO<List<RoleVO>> getRoleList(
            @Parameter(description = "角色类型筛选") @RequestParam(required = false) Integer roleType) {
        List<RoleVO> roles = roleService.getRoleList(roleType);
        return ResultVO.success(roles);
    }

    /**
     * 获取角色树
     */
    @GetMapping("/tree")
    @Operation(summary = "获取角色树", description = "获取角色的树形结构")
    public ResultVO<List<RoleVO>> getRoleTree(
            @Parameter(description = "父角色ID") @RequestParam(required = false) Long parentId) {
        List<RoleVO> tree = roleService.getRoleTree(parentId);
        return ResultVO.success(tree);
    }

    /**
     * 创建角色
     */
    @PostMapping
    @Operation(summary = "创建角色", description = "创建新的角色")
    public ResultVO<RoleVO> createRole(@Valid @RequestBody RoleCreateDTO createDTO) {
        RoleVO roleVO = roleService.createRole(createDTO);
        return ResultVO.success(roleVO);
    }

    /**
     * 更新角色 - 符合OpenAPI规范，使用请求体
     */
    @PutMapping
    @Operation(summary = "更新角色", description = "更新角色信息")
    public ResultVO<RoleVO> updateRole(@Valid @RequestBody RoleUpdateDTO updateDTO) {
        RoleVO roleVO = roleService.updateRole(updateDTO);
        return ResultVO.success(roleVO);
    }

    /**
     * 删除角色 - 符合RESTful规范，使用查询参数（与OpenAPI一致）
     */
    @DeleteMapping
    @Operation(summary = "删除角色", description = "删除指定角色（需确保无子角色和用户）")
    public ResultVO<Void> deleteRole(@Parameter(description = "角色ID") @RequestParam Long id) {
        roleService.deleteRole(id);
        return ResultVO.success();
    }

    /**
     * 获取角色详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取角色详情", description = "根据角色ID获取角色详细信息")
    public ResultVO<RoleVO> getRoleById(@Parameter(description = "角色ID") @PathVariable Long id) {
        RoleVO roleVO = roleService.getRoleById(id);
        return ResultVO.success(roleVO);
    }

    /**
     * 分配权限给角色 - 符合OpenAPI规范，使用路径参数{id}
     */
    @PostMapping("/{id}/permissions")
    @Operation(summary = "分配权限给角色", description = "为角色分配权限")
    public ResultVO<Void> assignPermissions(
            @Parameter(description = "角色ID") @PathVariable Long id,
            @RequestBody List<Long> permIds) {
        roleService.assignPermissions(id, permIds);
        return ResultVO.success();
    }

    /**
     * 移除角色的权限
     */
    @DeleteMapping("/{id}/permissions")
    @Operation(summary = "移除角色的权限", description = "移除角色的指定权限")
    public ResultVO<Void> removePermissions(
            @Parameter(description = "角色ID") @PathVariable Long id,
            @RequestBody List<Long> permIds) {
        roleService.removePermissions(id, permIds);
        return ResultVO.success();
    }

    /**
     * 获取角色的权限列表 - 符合OpenAPI规范，使用路径参数{id}
     */
    @GetMapping("/{id}/permissions")
    @Operation(summary = "获取角色的权限列表", description = "获取角色拥有的权限列表")
    public ResultVO<List<Long>> getRolePermissions(@Parameter(description = "角色ID") @PathVariable Long id) {
        List<Long> permIds = roleService.getRolePermissions(id);
        return ResultVO.success(permIds);
    }

    /**
     * 分配角色给用户
     */
    @PostMapping("/users/{userId}/roles/{roleId}")
    @Operation(summary = "分配角色给用户", description = "为用户分配角色")
    public ResultVO<Void> assignRoleToUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "角色ID") @PathVariable Long roleId,
            @RequestParam(required = false) Integer scopeType,
            @RequestParam(required = false) Long scopeId) {
        roleService.assignRoleToUser(userId, roleId, scopeType, scopeId, null);
        return ResultVO.success();
    }

    /**
     * 移除用户的角色
     */
    @DeleteMapping("/users/{userId}/roles/{roleId}")
    @Operation(summary = "移除用户的角色", description = "移除用户的指定角色")
    public ResultVO<Void> removeRoleFromUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "角色ID") @PathVariable Long roleId) {
        roleService.removeRoleFromUser(userId, roleId);
        return ResultVO.success();
    }

    /**
     * 获取用户的角色列表
     */
    @GetMapping("/users/{userId}/roles")
    @Operation(summary = "获取用户的角色列表", description = "获取用户拥有的角色列表")
    public ResultVO<List<RoleVO>> getUserRoles(@Parameter(description = "用户ID") @PathVariable Long userId) {
        List<RoleVO> roles = roleService.getUserRoles(userId);
        return ResultVO.success(roles);
    }
}
