package com.qoobot.openidaas.common.feign;

import com.qoobot.openidaas.common.dto.role.RoleCreateDTO;
import com.qoobot.openidaas.common.dto.role.RoleQueryDTO;
import com.qoobot.openidaas.common.dto.role.RoleUpdateDTO;
import com.qoobot.openidaas.common.vo.PageResultVO;
import com.qoobot.openidaas.common.vo.ResultVO;
import com.qoobot.openidaas.common.vo.role.RoleVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色服务 Feign 客户端
 *
 * @author QooBot
 */
@FeignClient(
        name = "role-service",
        configuration = com.qoobot.openidaas.common.config.FeignConfig.class
)
public interface RoleClient {

    /**
     * 获取角色列表
     */
    @GetMapping("/api/roles")
    ResultVO<List<RoleVO>> getRoleList(@RequestParam(required = false) Integer roleType);

    /**
     * 获取角色树
     */
    @GetMapping("/api/roles/tree")
    ResultVO<List<RoleVO>> getRoleTree(@RequestParam(required = false) Long parentId);

    /**
     * 根据角色 ID 获取角色信息
     */
    @GetMapping("/api/roles/{id}")
    ResultVO<RoleVO> getRoleById(@PathVariable("id") Long id);

    /**
     * 创建角色
     */
    @PostMapping("/api/roles")
    ResultVO<RoleVO> createRole(@RequestBody RoleCreateDTO createDTO);

    /**
     * 更新角色信息
     */
    @PutMapping("/api/roles")
    ResultVO<RoleVO> updateRole(@RequestBody RoleUpdateDTO updateDTO);

    /**
     * 删除角色
     */
    @DeleteMapping("/api/roles")
    ResultVO<Void> deleteRole(@RequestParam Long id);

    /**
     * 分配权限给角色
     */
    @PostMapping("/api/roles/{roleId}/permissions")
    ResultVO<Void> assignPermissions(@PathVariable("roleId") Long roleId, @RequestBody List<Long> permIds);

    /**
     * 获取角色的权限列表
     */
    @GetMapping("/api/roles/{roleId}/permissions")
    ResultVO<List<Long>> getRolePermissions(@PathVariable("roleId") Long roleId);

    /**
     * 移除角色的权限
     */
    @DeleteMapping("/api/roles/{roleId}/permissions")
    ResultVO<Void> removePermissions(@PathVariable("roleId") Long roleId, @RequestBody List<Long> permIds);

    /**
     * 分配角色给用户
     */
    @PostMapping("/api/roles/users/{userId}/roles/{roleId}")
    ResultVO<Void> assignRoleToUser(
            @PathVariable("userId") Long userId,
            @PathVariable("roleId") Long roleId,
            @RequestParam(required = false) Integer scopeType,
            @RequestParam(required = false) Long scopeId);

    /**
     * 移除用户的角色
     */
    @DeleteMapping("/api/roles/users/{userId}/roles/{roleId}")
    ResultVO<Void> removeRoleFromUser(@PathVariable("userId") Long userId, @PathVariable("roleId") Long roleId);

    /**
     * 获取用户的角色列表
     */
    @GetMapping("/api/roles/users/{userId}/roles")
    ResultVO<List<RoleVO>> getUserRoles(@PathVariable("userId") Long userId);
}
