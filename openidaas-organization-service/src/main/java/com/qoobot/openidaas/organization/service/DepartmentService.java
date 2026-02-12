package com.qoobot.openidaas.organization.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qoobot.openidaas.common.dto.department.DepartmentCreateDTO;
import com.qoobot.openidaas.common.dto.department.DepartmentUpdateDTO;
import com.qoobot.openidaas.common.vo.department.DepartmentVO;

import java.util.List;

/**
 * 部门服务接口
 *
 * @author QooBot
 */
public interface DepartmentService extends IService<com.qoobot.openidaas.organization.entity.Department> {

    /**
     * 获取部门树
     *
     * @param parentId 父部门ID，为空时返回根部门
     * @return 部门树列表
     */
    List<DepartmentVO> getDepartmentTree(Long parentId);

    /**
     * 创建部门
     *
     * @param createDTO 创建部门DTO
     * @return 部门VO
     */
    DepartmentVO createDepartment(DepartmentCreateDTO createDTO);

    /**
     * 更新部门
     *
     * @param updateDTO 更新部门DTO
     * @return 部门VO
     */
    DepartmentVO updateDepartment(DepartmentUpdateDTO updateDTO);

    /**
     * 删除部门
     *
     * @param id 部门ID
     */
    void deleteDepartment(Long id);

    /**
     * 获取部门详情
     *
     * @param id 部门ID
     * @return 部门VO
     */
    DepartmentVO getDepartmentById(Long id);
}
