package com.qoobot.openidaas.organization.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openidaas.common.vo.ResultVO;
import com.qoobot.openidaas.organization.entity.Organization;
import com.qoobot.openidaas.organization.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 组织管理控制器
 *
 * @author Qoobot
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Tag(name = "组织管理", description = "组织管理API")
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    @Operation(summary = "创建组织")
    public ResultVO<Long> createOrganization(@Valid @RequestBody Organization organization) {
        try {
            Long id = organizationService.createOrganization(organization);
            return ResultVO.success(id);
        } catch (Exception e) {
            log.error("创建组织失败", e);
            return ResultVO.error("500", e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新组织")
    public ResultVO<Boolean> updateOrganization(
            @Parameter(description = "组织ID") @PathVariable Long id,
            @Valid @RequestBody Organization organization) {
        try {
            organization.setId(id);
            boolean result = organizationService.updateOrganization(organization);
            return ResultVO.success(result);
        } catch (Exception e) {
            log.error("更新组织失败", e);
            return ResultVO.error("500", e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除组织")
    public ResultVO<Boolean> deleteOrganization(
            @Parameter(description = "组织ID") @PathVariable Long id) {
        try {
            boolean result = organizationService.deleteOrganization(id);
            return ResultVO.success(result);
        } catch (Exception e) {
            log.error("删除组织失败", e);
            return ResultVO.error("500", e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取组织详情")
    public ResultVO<Organization> getOrganization(
            @Parameter(description = "组织ID") @PathVariable Long id) {
        try {
            Organization organization = organizationService.getOrganizationById(id);
            return ResultVO.success(organization);
        } catch (Exception e) {
            log.error("获取组织详情失败", e);
            return ResultVO.error("500", e.getMessage());
        }
    }

    @GetMapping("/tree")
    @Operation(summary = "获取组织树")
    public ResultVO<List<Organization>> getOrganizationTree(
            @Parameter(description = "父组织ID") @RequestParam(required = false) Long parentId) {
        try {
            List<Organization> tree = organizationService.getOrganizationTree(parentId);
            return ResultVO.success(tree);
        } catch (Exception e) {
            log.error("获取组织树失败", e);
            return ResultVO.error("500", e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户所属组织")
    public ResultVO<List<Organization>> getUserOrganizations(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        try {
            List<Organization> organizations = organizationService.getUserOrganizations(userId);
            return ResultVO.success(organizations);
        } catch (Exception e) {
            log.error("获取用户所属组织失败", e);
            return ResultVO.error("500", e.getMessage());
        }
    }

    @PutMapping("/{id}/move")
    @Operation(summary = "移动组织")
    public ResultVO<Boolean> moveOrganization(
            @Parameter(description = "组织ID") @PathVariable Long id,
            @Parameter(description = "新父组织ID") @RequestParam Long newParentId) {
        try {
            boolean result = organizationService.moveOrganization(id, newParentId);
            return ResultVO.success(result);
        } catch (Exception e) {
            log.error("移动组织失败", e);
            return ResultVO.error("500", e.getMessage());
        }
    }

    @GetMapping("/{id}/path")
    @Operation(summary = "获取组织路径")
    public ResultVO<String> getOrganizationPath(
            @Parameter(description = "组织ID") @PathVariable Long id) {
        try {
            String path = organizationService.getOrganizationPath(id);
            return ResultVO.success(path);
        } catch (Exception e) {
            log.error("获取组织路径失败", e);
            return ResultVO.error("500", e.getMessage());
        }
    }

    @GetMapping("/check-code")
    @Operation(summary = "检查组织编码唯一性")
    public ResultVO<Boolean> checkCodeUnique(
            @Parameter(description = "组织编码") @RequestParam String code,
            @Parameter(description = "排除的组织ID") @RequestParam(required = false) Long excludeId) {
        try {
            boolean unique = organizationService.isCodeUnique(code, excludeId);
            return ResultVO.success(unique);
        } catch (Exception e) {
            log.error("检查组织编码唯一性失败", e);
            return ResultVO.error("500", e.getMessage());
        }
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除组织")
    public ResultVO<Boolean> batchDeleteOrganizations(
            @Parameter(description = "组织ID列表") @RequestBody List<Long> ids) {
        try {
            boolean result = organizationService.batchDeleteOrganizations(ids);
            return ResultVO.success(result);
        } catch (Exception e) {
            log.error("批量删除组织失败", e);
            return ResultVO.error("500", e.getMessage());
        }
    }

    @PutMapping("/{id}/enable")
    @Operation(summary = "启用组织")
    public ResultVO<Boolean> enableOrganization(
            @Parameter(description = "组织ID") @PathVariable Long id) {
        try {
            boolean result = organizationService.enableOrganization(id);
            return ResultVO.success(result);
        } catch (Exception e) {
            log.error("启用组织失败", e);
            return ResultVO.error("500", e.getMessage());
        }
    }

    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用组织")
    public ResultVO<Boolean> disableOrganization(
            @Parameter(description = "组织ID") @PathVariable Long id) {
        try {
            boolean result = organizationService.disableOrganization(id);
            return ResultVO.success(result);
        } catch (Exception e) {
            log.error("禁用组织失败", e);
            return ResultVO.error("500", e.getMessage());
        }
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询组织")
    public ResultVO<Page<Organization>> getOrganizations(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "组织名称") @RequestParam(required = false) String name,
            @Parameter(description = "组织编码") @RequestParam(required = false) String code,
            @Parameter(description = "状态") @RequestParam(required = false) String status) {
        try {
            Page<Organization> page = new Page<>(current, size);
            Page<Organization> result = organizationService.getOrganizations(page, name, code, status);
            return ResultVO.success(result);
        } catch (Exception e) {
            log.error("分页查询组织失败", e);
            return ResultVO.error("500", e.getMessage());
        }
    }

}