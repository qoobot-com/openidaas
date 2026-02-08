package com.qoobot.openidaas.user.controller;

import com.qoobot.openidaas.user.dto.DepartmentDTO;
import com.qoobot.openidaas.user.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 创建部门
     */
    @PostMapping
    @PreAuthorize("hasAuthority('department:create')")
    public ResponseEntity<ApiResponse<DepartmentDTO>> createDepartment(@Valid @RequestBody DepartmentDTO dto) {
        DepartmentDTO department = departmentService.createDepartment(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(department, "部门创建成功"));
    }

    /**
     * 更新部门
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('department:update')")
    public ResponseEntity<ApiResponse<DepartmentDTO>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentDTO dto) {
        DepartmentDTO department = departmentService.updateDepartment(id, dto);
        return ResponseEntity.ok(ApiResponse.success(department, "部门更新成功"));
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('department:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success(null, "部门删除成功"));
    }

    /**
     * 获取部门详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('department:view')")
    public ResponseEntity<ApiResponse<DepartmentDTO>> getDepartment(@PathVariable Long id) {
        DepartmentDTO department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.success(department));
    }

    /**
     * 获取部门树
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('department:view')")
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> getDepartmentTree(
            @RequestParam Long tenantId) {
        List<DepartmentDTO> tree = departmentService.getDepartmentTree(tenantId);
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    /**
     * 移动部门
     */
    @PutMapping("/{id}/move")
    @PreAuthorize("hasAuthority('department:move')")
    public ResponseEntity<ApiResponse<Void>> moveDepartment(
            @PathVariable Long id,
            @RequestParam Long newParentId) {
        departmentService.moveDepartment(id, newParentId);
        return ResponseEntity.ok(ApiResponse.success(null, "部门移动成功"));
    }
}
