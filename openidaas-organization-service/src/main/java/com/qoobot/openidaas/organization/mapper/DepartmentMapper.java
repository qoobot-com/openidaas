package com.qoobot.openidaas.organization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openidaas.organization.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 部门Mapper接口
 *
 * @author QooBot
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    /**
     * 根据父部门ID查询子部门列表
     *
     * @param parentId 父部门ID
     * @return 子部门列表
     */
    List<Department> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 根据部门编码查询部门
     *
     * @param deptCode 部门编码
     * @return 部门信息
     */
    Department selectByCode(@Param("deptCode") String deptCode);

    /**
     * 检查是否存在子部门
     *
     * @param parentId 父部门ID
     * @return 子部门数量
     */
    int countChildren(@Param("parentId") Long parentId);

    /**
     * 查询部门下的用户数量
     *
     * @param deptId 部门ID
     * @return 用户数量
     */
    @Select("SELECT COUNT(*) FROM user_departments WHERE dept_id = #{deptId}")
    int countUsers(@Param("deptId") Long deptId);
}
