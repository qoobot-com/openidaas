package com.qoobot.openidaas.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openidaas.core.domain.Department;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 部门Mapper接口
 *
 * @author QooBot
 */
public interface DepartmentMapper extends BaseMapper<Department> {

    /**
     * 根据部门编码查找部门
     */
    @Select("SELECT * FROM departments WHERE dept_code = #{deptCode}")
    Department findByDeptCode(@Param("deptCode") String deptCode);

    /**
     * 根据部门名称查找部门
     */
    @Select("SELECT * FROM departments WHERE dept_name = #{deptName}")
    Department findByDeptName(@Param("deptName") String deptName);

    /**
     * 根据租户ID查找部门列表（分页）
     */
    IPage<Department> findByTenantId(Page<Department> page, @Param("tenantId") Long tenantId);

    /**
     * 根据父部门ID查找子部门列表
     */
    @Select("SELECT * FROM departments WHERE parent_id = #{parentId}")
    List<Department> findByParentId(@Param("parentId") Long parentId);

    /**
     * 根据租户ID查找顶级部门列表
     */
    @Select("SELECT * FROM departments WHERE parent_id IS NULL AND tenant_id = #{tenantId} ORDER BY sort_order")
    List<Department> findTopDepartmentsByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据用户ID查找所属部门列表
     * 【需在XML中实现】涉及中间表关联查询
     */
    List<Department> findByUserId(@Param("userId") Long userId);

    /**
     * 根据部门编码模糊查询（分页）
     */
    IPage<Department> findByDeptCodeContainingAndTenantId(Page<Department> page, @Param("deptCode") String deptCode, @Param("tenantId") Long tenantId);

    /**
     * 根据部门名称模糊查询（分页）
     */
    IPage<Department> findByDeptNameContainingAndTenantId(Page<Department> page, @Param("deptName") String deptName, @Param("tenantId") Long tenantId);

    /**
     * 统计租户部门数量
     */
    @Select("SELECT COUNT(*) FROM departments WHERE tenant_id = #{tenantId}")
    long countByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 检查部门编码是否已存在
     */
    @Select("SELECT COUNT(*) > 0 FROM departments WHERE dept_code = #{deptCode} AND tenant_id = #{tenantId}")
    boolean existsByDeptCodeAndTenantId(@Param("deptCode") String deptCode, @Param("tenantId") Long tenantId);

    /**
     * 根据层级路径查找部门
     */
    @Select("SELECT * FROM departments WHERE tree_path LIKE CONCAT(#{treePath}, '%') AND tenant_id = #{tenantId}")
    List<Department> findByTreePathStartingWithAndTenantId(@Param("treePath") String treePath, @Param("tenantId") Long tenantId);

    /**
     * 查找叶子节点部门
     * 【需在XML中实现】涉及子查询
     */
    List<Department> findLeafDepartments(@Param("tenantId") Long tenantId);

    /**
     * 根据ID集合查询部门
     */
    @Select("<script>" +
            "SELECT * FROM departments WHERE id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Department> findByIdIn(@Param("ids") List<Long> ids);
}
