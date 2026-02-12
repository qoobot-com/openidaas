package com.qoobot.openidaas.organization.converter;

import com.qoobot.openidaas.common.dto.department.DepartmentCreateDTO;
import com.qoobot.openidaas.common.dto.department.DepartmentUpdateDTO;
import com.qoobot.openidaas.common.vo.department.DepartmentVO;
import com.qoobot.openidaas.organization.entity.Department;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * 部门转换器
 *
 * @author QooBot
 */
@Component
public class DepartmentConverter {

    /**
     * 实体转VO
     */
    public DepartmentVO toVO(Department department) {
        if (department == null) {
            return null;
        }

        DepartmentVO vo = new DepartmentVO();
        BeanUtils.copyProperties(department, vo);
        vo.setEnabled(department.isEnabled());
        vo.setTreePath(department.getLevelPath());
        return vo;
    }

    /**
     * 创建DTO转实体
     */
    public Department toEntity(DepartmentCreateDTO dto) {
        if (dto == null) {
            return null;
        }

        Department department = new Department();
        BeanUtils.copyProperties(dto, department);
        return department;
    }

    /**
     * 更新DTO转实体
     */
    public Department toEntity(DepartmentUpdateDTO dto) {
        if (dto == null) {
            return null;
        }

        Department department = new Department();
        BeanUtils.copyProperties(dto, department);
        return department;
    }
}
