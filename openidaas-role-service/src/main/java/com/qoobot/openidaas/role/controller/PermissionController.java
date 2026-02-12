package com.qoobot.openidaas.role.controller;

import com.qoobot.openidaas.common.dto.permission.PermissionCreateDTO;
import com.qoobot.openidaas.common.dto.permission.PermissionUpdateDTO;
import com.qoobot.openidaas.common.vo.ResultVO;
import com.qoobot.openidaas.common.vo.permission.PermissionVO;
import com.qoobot.openidaas.role.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理Controller
 *
 * @author QooBot
 */
@Slf4j
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Tag(name = "权限管理", description = "权限管理相关接口")
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 获取权限列表
     */
    @GetMapping
    @Operation(summary = "获取权限列表", description = "查询权限信息列表")
    public ResultVO<List<PermissionVO>> getPermissionList(
            @Parameter(description = "权限类型筛选") @RequestParam(required = false) String permType) {
        List<PermissionVO> permissions = permissionService.getPermissionList(permType);
        return ResultVO.success(permissions);
    }

    /**
     * 获取权限树
     */
    @GetMapping("/tree")
    @Operation(summary = "获取权限树", description = "获取权限的树形结构")
    public ResultVO<List<PermissionVO>> getPermissionTree(
            @Parameter(description = "父权限ID") @RequestParam(required = false) Long parentId) {
        List<PermissionVO> tree = permissionService.getPermissionTree(parentId);
        return ResultVO.success(tree);
    }

    /**
     * 创建权限
     */
    @PostMapping
    @Operation(summary = "创建权限", description = "创建新的权限")
    public ResultVO<PermissionVO> createPermission(@Valid @RequestBody PermissionCreateDTO createDTO) {
        PermissionVO permissionVO = permissionService.createPermission(createDTO);
        return ResultVO.success(permissionVO);
    }

    /**
     * 更新权限
     */
    @PutMapping
    @Operation(summary = "更新权限", description = "更新权限信息")
    public ResultVO<PermissionVO> updatePermission(@Valid @RequestBody PermissionUpdateDTO updateDTO) {
        PermissionVO permissionVO = permissionService.updatePermission(updateDTO);
        return ResultVO.success(permissionVO);
    }

    /**
     * 删除权限
     */
    @DeleteMapping
    @Operation(summary = "删除权限", description = "删除指定权限（需确保无子权限）")
    public ResultVO<Void> deletePermission(@Parameter(description = "权限ID") @RequestParam Long id) {
        permissionService.deletePermission(id);
        return ResultVO.success();
    }

    /**
     * 获取权限详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取权限详情", description = "根据权限ID获取权限详细信息")
    public ResultVO<PermissionVO> getPermissionById(@Parameter(description = "权限ID") @PathVariable Long id) {
        PermissionVO permissionVO = permissionService.getPermissionById(id);
        return ResultVO.success(permissionVO);
    }

    /**
     * 获取用户的权限列表
     */
    @GetMapping("/users/{userId}")
    @Operation(summary = "获取用户的权限列表", description = "获取用户拥有的权限列表")
    public ResultVO<List<PermissionVO>> getUserPermissions(@Parameter(description = "用户ID") @PathVariable Long userId) {
        List<PermissionVO> permissions = permissionService.getUserPermissions(userId);
        return ResultVO.success(permissions);
    }

    /**
     * 检查用户权限
     */
    @GetMapping("/users/{userId}/check")
    @Operation(summary = "检查用户权限", description = "检查用户是否拥有指定权限")
    public ResultVO<Boolean> hasPermission(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "权限编码") @RequestParam String permCode) {
        boolean hasPermission = permissionService.hasPermission(userId, permCode);
        return ResultVO.success(hasPermission);
    }

    /**
     * 获取用户的菜单树
     */
    @GetMapping("/users/{userId}/menu-tree")
    @Operation(summary = "获取用户的菜单树", description = "获取用户的菜单权限树形结构")
    public ResultVO<List<PermissionVO>> getUserMenuTree(@Parameter(description = "用户ID") @PathVariable Long userId) {
        List<PermissionVO> menuTree = permissionService.getUserMenuTree(userId);
        return ResultVO.success(menuTree);
    }
}
