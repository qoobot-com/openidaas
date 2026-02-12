package com.qoobot.openidaas.organization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qoobot.openidaas.common.dto.department.DepartmentCreateDTO;
import com.qoobot.openidaas.common.dto.department.DepartmentUpdateDTO;
import com.qoobot.openidaas.common.enumeration.StatusEnum;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.common.vo.department.DepartmentVO;
import com.qoobot.openidaas.organization.entity.Department;
import com.qoobot.openidaas.organization.mapper.DepartmentMapper;
import com.qoobot.openidaas.organization.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门服务实现类
 *
 * @author QooBot
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    @Override
    public List<DepartmentVO> getDepartmentTree(Long parentId) {
        Long actualParentId = parentId == null ? 0L : parentId;
        List<Department> departments = departmentMapper.selectByParentId(actualParentId);
        return buildDepartmentTree(departments);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepartmentVO createDepartment(DepartmentCreateDTO createDTO) {
        log.info("创建部门，部门编码：{}，部门名称：{}", createDTO.getDeptCode(), createDTO.getDeptName());

        // 检查部门编码是否已存在
        Department existDepartment = departmentMapper.selectByCode(createDTO.getDeptCode());
        if (existDepartment != null) {
            throw new BusinessException("部门编码已存在：" + createDTO.getDeptCode());
        }

        // 验证父部门是否存在
        Long parentId = createDTO.getParentId() == null ? 0L : createDTO.getParentId();
        if (parentId != 0L) {
            Department parentDepartment = departmentMapper.selectById(parentId);
            if (parentDepartment == null) {
                throw new BusinessException("父部门不存在");
            }
        }

        // 构建部门实体
        Department department = new Department();
        BeanUtils.copyProperties(createDTO, department);
        department.setParentId(parentId);
        department.setStatus(createDTO.getEnabled() ? StatusEnum.ENABLED.getCode() : StatusEnum.DISABLED.getCode());

        // 计算层级路径和深度
        if (parentId == 0L) {
            department.setLevelPath("/" + createDTO.getDeptCode() + "/");
            department.setLevelDepth(1);
        } else {
            Department parent = departmentMapper.selectById(parentId);
            String levelPath = parent.getLevelPath() + createDTO.getDeptCode() + "/";
            department.setLevelPath(levelPath);
            department.setLevelDepth(parent.getLevelDepth() + 1);
        }

        departmentMapper.insert(department);

        log.info("部门创建成功，部门ID：{}", department.getId());
        return convertToVO(department);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepartmentVO updateDepartment(DepartmentUpdateDTO updateDTO) {
        log.info("更新部门，部门ID：{}", updateDTO.getId());

        // 检查部门是否存在
        Department existDepartment = departmentMapper.selectById(updateDTO.getId());
        if (existDepartment == null) {
            throw new BusinessException("部门不存在");
        }

        // 检查部门编码是否重复（排除自己）
        Department codeDepartment = departmentMapper.selectByCode(updateDTO.getDeptCode());
        if (codeDepartment != null && !codeDepartment.getId().equals(updateDTO.getId())) {
            throw new BusinessException("部门编码已存在：" + updateDTO.getDeptCode());
        }

        // 如果更新父部门，验证新父部门是否存在且不能是子孙部门
        if (updateDTO.getParentId() != null && !updateDTO.getParentId().equals(existDepartment.getParentId())) {
            if (updateDTO.getParentId().equals(existDepartment.getId())) {
                throw new BusinessException("不能将部门设置为自己的父部门");
            }

            // 检查是否是子孙部门
            if (isDescendant(existDepartment.getId(), updateDTO.getParentId())) {
                throw new BusinessException("不能将部门设置为自己的子孙部门");
            }

            if (updateDTO.getParentId() != 0L) {
                Department newParent = departmentMapper.selectById(updateDTO.getParentId());
                if (newParent == null) {
                    throw new BusinessException("父部门不存在");
                }
            }

            // 更新层级路径
            updateTreePath(updateDTO.getId(), updateDTO.getParentId(), updateDTO.getDeptCode());
        }

        // 更新部门信息
        Department department = new Department();
        BeanUtils.copyProperties(updateDTO, department);

        if (updateDTO.getEnabled() != null) {
            department.setStatus(updateDTO.getEnabled() ? StatusEnum.ENABLED.getCode() : StatusEnum.DISABLED.getCode());
        }

        departmentMapper.updateById(department);

        log.info("部门更新成功，部门ID：{}", updateDTO.getId());
        Department updated = departmentMapper.selectById(updateDTO.getId());
        return convertToVO(updated);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDepartment(Long id) {
        log.info("删除部门，部门ID：{}", id);

        // 检查部门是否存在
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException("部门不存在");
        }

        // 检查是否有子部门
        int childrenCount = departmentMapper.countChildren(id);
        if (childrenCount > 0) {
            throw new BusinessException("部门下存在子部门，无法删除");
        }

        // 检查是否有用户
        int userCount = departmentMapper.countUsers(id);
        if (userCount > 0) {
            throw new BusinessException("部门下存在用户，无法删除");
        }

        departmentMapper.deleteById(id);

        log.info("部门删除成功，部门ID：{}", id);
    }

    @Override
    public DepartmentVO getDepartmentById(Long id) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException("部门不存在");
        }
        return convertToVO(department);
    }

    /**
     * 构建部门树
     */
    private List<DepartmentVO> buildDepartmentTree(List<Department> departments) {
        List<DepartmentVO> voList = departments.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 为每个部门递归加载子部门
        for (DepartmentVO vo : voList) {
            List<Department> children = departmentMapper.selectByParentId(vo.getId());
            if (!children.isEmpty()) {
                vo.setChildren(buildDepartmentTree(children));
            }
        }

        return voList;
    }

    /**
     * 转换为VO
     */
    private DepartmentVO convertToVO(Department department) {
        DepartmentVO vo = new DepartmentVO();
        BeanUtils.copyProperties(department, vo);
        vo.setEnabled(department.isEnabled());
        vo.setTreePath(department.getLevelPath());
        return vo;
    }

    /**
     * 检查是否是子孙部门
     */
    private boolean isDescendant(Long ancestorId, Long potentialDescendantId) {
        Department descendant = departmentMapper.selectById(potentialDescendantId);
        if (descendant == null || descendant.getParentId() == 0) {
            return false;
        }
        if (descendant.getParentId().equals(ancestorId)) {
            return true;
        }
        return isDescendant(ancestorId, descendant.getParentId());
    }

    /**
     * 更新树路径
     */
    private void updateTreePath(Long deptId, Long newParentId, String deptCode) {
        if (newParentId == 0L) {
            Department department = departmentMapper.selectById(deptId);
            department.setLevelPath("/" + deptCode + "/");
            department.setLevelDepth(1);
            department.setParentId(0L);
            departmentMapper.updateById(department);
        } else {
            Department newParent = departmentMapper.selectById(newParentId);
            String newLevelPath = newParent.getLevelPath() + deptCode + "/";
            Department department = departmentMapper.selectById(deptId);
            department.setLevelPath(newLevelPath);
            department.setLevelDepth(newParent.getLevelDepth() + 1);
            department.setParentId(newParentId);
            departmentMapper.updateById(department);

            // 递归更新子部门路径
            List<Department> children = departmentMapper.selectByParentId(deptId);
            for (Department child : children) {
                updateTreePath(child.getId(), deptId, child.getDeptCode());
            }
        }
    }
}
