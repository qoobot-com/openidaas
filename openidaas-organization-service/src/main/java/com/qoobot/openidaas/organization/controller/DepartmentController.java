package com.qoobot.openidaas.organization.controller;

import com.qoobot.openidaas.common.dto.department.DepartmentCreateDTO;
import com.qoobot.openidaas.common.dto.department.DepartmentUpdateDTO;
import com.qoobot.openidaas.common.vo.ResultVO;
import com.qoobot.openidaas.common.vo.department.DepartmentVO;
import com.qoobot.openidaas.organization.entity.Department;
import com.qoobot.openidaas.organization.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理Controller
 *
 * @author QooBot
 */
@Slf4j
@RestController
@RequestMapping("/api/organizations/departments")
@RequiredArgsConstructor
@Tag(name = "部门管理", description = "部门管理相关接口")
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 获取部门树
     */
    @GetMapping
    @Operation(summary = "获取部门树", description = "获取完整的组织架构树形结构")
    public ResultVO<List<DepartmentVO>> getDepartmentTree(
            @Parameter(description = "父部门ID，为空时返回根部门") @RequestParam(required = false) Long parentId) {
        List<DepartmentVO> tree = departmentService.getDepartmentTree(parentId);
        return ResultVO.success(tree);
    }

    /**
     * 创建部门
     */
    @PostMapping
    @Operation(summary = "创建部门", description = "创建新的部门")
    public ResultVO<DepartmentVO> createDepartment(@Valid @RequestBody DepartmentCreateDTO createDTO) {
        DepartmentVO departmentVO = departmentService.createDepartment(createDTO);
        return ResultVO.success(departmentVO);
    }

    /**
     * 更新部门 - 符合RESTful规范，使用请求体
     */
    @PutMapping
    @Operation(summary = "更新部门", description = "更新部门信息")
    public ResultVO<DepartmentVO> updateDepartment(@Valid @RequestBody DepartmentUpdateDTO updateDTO) {
        DepartmentVO departmentVO = departmentService.updateDepartment(updateDTO);
        return ResultVO.success(departmentVO);
    }

    /**
     * 删除部门 - 符合OpenAPI规范，使用查询参数
     */
    @DeleteMapping
    @Operation(summary = "删除部门", description = "删除指定部门（需确保无下属部门和用户）")
    public ResultVO<Void> deleteDepartment(@Parameter(description = "部门ID") @RequestParam Long id) {
        departmentService.deleteDepartment(id);
        return ResultVO.success();
    }

    /**
     * 获取部门详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取部门详情", description = "根据部门ID获取部门详细信息")
    public ResultVO<DepartmentVO> getDepartmentById(
            @Parameter(description = "部门ID") @PathVariable Long id) {
        DepartmentVO departmentVO = departmentService.getDepartmentById(id);
        return ResultVO.success(departmentVO);
    }
}
