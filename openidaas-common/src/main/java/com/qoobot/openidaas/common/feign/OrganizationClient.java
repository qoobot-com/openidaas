package com.qoobot.openidaas.common.feign;

import com.qoobot.openidaas.common.dto.department.DepartmentCreateDTO;
import com.qoobot.openidaas.common.dto.department.DepartmentUpdateDTO;
import com.qoobot.openidaas.common.dto.position.PositionCreateDTO;
import com.qoobot.openidaas.common.dto.position.PositionUpdateDTO;
import com.qoobot.openidaas.common.vo.ResultVO;
import com.qoobot.openidaas.common.vo.department.DepartmentVO;
import com.qoobot.openidaas.common.vo.position.PositionVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织服务 Feign 客户端
 *
 * @author QooBot
 */
@FeignClient(
        name = "organization-service",
        configuration = com.qoobot.openidaas.common.config.FeignConfig.class
)
public interface OrganizationClient {

    // ==================== 部门管理 ====================

    /**
     * 获取部门树
     */
    @GetMapping("/api/organizations/departments")
    ResultVO<List<DepartmentVO>> getDepartmentTree(@RequestParam(required = false) Long parentId);

    /**
     * 根据部门 ID 获取部门信息
     */
    @GetMapping("/api/organizations/departments/{id}")
    ResultVO<DepartmentVO> getDepartmentById(@PathVariable("id") Long id);

    /**
     * 创建部门
     */
    @PostMapping("/api/organizations/departments")
    ResultVO<DepartmentVO> createDepartment(@RequestBody DepartmentCreateDTO createDTO);

    /**
     * 更新部门信息
     */
    @PutMapping("/api/organizations/departments")
    ResultVO<DepartmentVO> updateDepartment(@RequestBody DepartmentUpdateDTO updateDTO);

    /**
     * 删除部门
     */
    @DeleteMapping("/api/organizations/departments")
    ResultVO<Void> deleteDepartment(@RequestParam Long id);

    // ==================== 职位管理 ====================

    /**
     * 获取职位列表
     */
    @GetMapping("/api/organizations/positions")
    ResultVO<List<PositionVO>> getPositions(@RequestParam(required = false) Long deptId);

    /**
     * 根据职位 ID 获取职位信息
     */
    @GetMapping("/api/organizations/positions/{id}")
    ResultVO<PositionVO> getPositionById(@PathVariable("id") Long id);

    /**
     * 创建职位
     */
    @PostMapping("/api/organizations/positions")
    ResultVO<PositionVO> createPosition(@RequestBody PositionCreateDTO createDTO);

    /**
     * 更新职位信息
     */
    @PutMapping("/api/organizations/positions")
    ResultVO<PositionVO> updatePosition(@RequestBody PositionUpdateDTO updateDTO);

    /**
     * 删除职位
     */
    @DeleteMapping("/api/organizations/positions/{id}")
    ResultVO<Void> deletePosition(@PathVariable("id") Long id);
}
