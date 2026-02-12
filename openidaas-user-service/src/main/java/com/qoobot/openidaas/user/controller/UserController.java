package com.qoobot.openidaas.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qoobot.openidaas.common.dto.user.UserCreateDTO;
import com.qoobot.openidaas.common.dto.user.UserPasswordDTO;
import com.qoobot.openidaas.common.dto.user.UserQueryDTO;
import com.qoobot.openidaas.common.dto.user.UserUpdateDTO;
import com.qoobot.openidaas.common.vo.PageResultVO;
import com.qoobot.openidaas.common.vo.ResultVO;
import com.qoobot.openidaas.common.vo.user.UserVO;
import com.qoobot.openidaas.user.entity.User;
import com.qoobot.openidaas.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理Controller
 *
 * @author QooBot
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户管理相关接口")
public class UserController {

    private final UserService userService;

    /**
     * 查询用户列表
     */
    @GetMapping
    @Operation(summary = "查询用户列表", description = "分页查询用户信息，支持多种筛选条件")
    public ResultVO<PageResultVO<UserVO>> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Integer status) {
        UserQueryDTO queryDTO = new UserQueryDTO();
        queryDTO.setCurrentPage((long) page + 1);
        queryDTO.setPageSize((long) size);
        queryDTO.setUsername(username);
        queryDTO.setEmail(email);
        queryDTO.setStatus(status);
        IPage<User> userPage = userService.selectUserPage(queryDTO);
        PageResultVO<UserVO> result = new PageResultVO<>();
        result.setRecords(userPage.getRecords().stream().map(user -> {
            UserVO vo = new UserVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setEmail(user.getEmail());
            vo.setMobile(user.getMobile());
            vo.setStatus(user.getStatus());
            vo.setCreatedAt(user.getCreatedAt());
            vo.setUpdatedAt(user.getUpdatedAt());
            return vo;
        }).toList());
        result.setTotal(userPage.getTotal());
        result.setTotalPages(userPage.getPages());
        result.setPageSize(userPage.getSize());
        result.setCurrentPage(userPage.getCurrent());
        return ResultVO.success(result);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    public ResultVO<UserVO> getUserById(@Parameter(description = "用户ID") @PathVariable Long id) {
        UserVO userVO = userService.getUserDetail(id);
        return ResultVO.success(userVO);
    }

    /**
     * 创建用户
     */
    @PostMapping
    @Operation(summary = "创建用户", description = "创建新的用户账户")
    public ResultVO<UserVO> createUser(@Valid @RequestBody UserCreateDTO createDTO) {
        UserVO userVO = userService.createUser(createDTO);
        return ResultVO.success(userVO);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息", description = "更新用户基本信息和档案信息")
    public ResultVO<UserVO> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        UserVO userVO = userService.updateUser(id, updateDTO);
        return ResultVO.success(userVO);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "逻辑删除用户账户")
    public ResultVO<Void> deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResultVO.success();
    }

    /**
     * 分配用户部门
     */
    @PostMapping("/{id}/departments")
    @Operation(summary = "分配用户部门", description = "为用户分配部门和职位关系")
    public ResultVO<Void> assignDepartments(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody AssignDepartmentRequest request) {
        userService.assignDepartments(id, request.getDeptIds(), request.getPositionId(), request.getIsPrimary());
        return ResultVO.success();
    }

    /**
     * 分配用户角色
     */
    @PostMapping("/{id}/roles")
    @Operation(summary = "分配用户角色", description = "为用户分配角色和权限")
    public ResultVO<Void> assignRoles(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody AssignRoleRequest request) {
        userService.assignRoles(id, request.getRoleIds(), request.getScopeType(), request.getScopeId());
        return ResultVO.success();
    }

    /**
     * 移除用户角色
     */
    @DeleteMapping("/{id}/roles")
    @Operation(summary = "移除用户角色", description = "移除用户的指定角色")
    public ResultVO<Void> removeRoles(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody List<Long> roleIds) {
        userService.removeRoles(id, roleIds);
        return ResultVO.success();
    }

    /**
     * 锁定用户
     */
    @PostMapping("/{id}/lock")
    @Operation(summary = "锁定用户", description = "锁定用户账户")
    public ResultVO<Void> lockUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.lockUser(id);
        return ResultVO.success();
    }

    /**
     * 解锁用户
     */
    @PostMapping("/{id}/unlock")
    @Operation(summary = "解锁用户", description = "解锁用户账户")
    public ResultVO<Void> unlockUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.unlockUser(id);
        return ResultVO.success();
    }

    /**
     * 停用用户
     */
    @PostMapping("/{id}/disable")
    @Operation(summary = "停用用户", description = "停用用户账户")
    public ResultVO<Void> disableUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.disableUser(id);
        return ResultVO.success();
    }

    /**
     * 启用用户
     */
    @PostMapping("/{id}/enable")
    @Operation(summary = "启用用户", description = "启用用户账户")
    public ResultVO<Void> enableUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.enableUser(id);
        return ResultVO.success();
    }

    /**
     * 部门分配请求
     */
    public static class AssignDepartmentRequest {
        private List<Long> deptIds;
        private Long positionId;
        private Boolean isPrimary = true;

        public List<Long> getDeptIds() {
            return deptIds;
        }

        public void setDeptIds(List<Long> deptIds) {
            this.deptIds = deptIds;
        }

        public Long getPositionId() {
            return positionId;
        }

        public void setPositionId(Long positionId) {
            this.positionId = positionId;
        }

        public Boolean getIsPrimary() {
            return isPrimary;
        }

        public void setIsPrimary(Boolean isPrimary) {
            this.isPrimary = isPrimary;
        }
    }

    /**
     * 角色分配请求
     */
    public static class AssignRoleRequest {
        private List<Long> roleIds;
        private Integer scopeType = 1;
        private Long scopeId;

        public List<Long> getRoleIds() {
            return roleIds;
        }

        public void setRoleIds(List<Long> roleIds) {
            this.roleIds = roleIds;
        }

        public Integer getScopeType() {
            return scopeType;
        }

        public void setScopeType(Integer scopeType) {
            this.scopeType = scopeType;
        }

        public Long getScopeId() {
            return scopeId;
        }

        public void setScopeId(Long scopeId) {
            this.scopeId = scopeId;
        }
    }
}
