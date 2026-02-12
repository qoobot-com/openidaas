package com.qoobot.openidaas.core.repository;

import com.qoobot.openidaas.core.domain.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 部门仓储接口
 *
 * @author QooBot
 */
public interface DepartmentRepository extends BaseRepository<Department, Long> {

    /**
     * 根据部门编码查找部门
     */
    Optional<Department> findByDeptCode(String deptCode);

    /**
     * 根据部门名称查找部门
     */
    Optional<Department> findByDeptName(String deptName);

    /**
     * 根据租户ID查找部门列表
     */
    Page<Department> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * 根据父部门ID查找子部门列表
     */
    List<Department> findByParentId(Long parentId);

    /**
     * 根据租户ID查找顶级部门列表
     */
    @Query("SELECT d FROM Department d WHERE d.parentId IS NULL AND d.tenantId = :tenantId ORDER BY d.sortOrder")
    List<Department> findTopDepartmentsByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据用户ID查找所属部门列表
     */
    @Query("SELECT d FROM Department d JOIN d.users u WHERE u.id = :userId")
    List<Department> findByUserId(@Param("userId") Long userId);

    /**
     * 根据部门ID集合查找部门列表
     */
    List<Department> findByIdIn(Set<Long> deptIds);

    /**
     * 根据部门编码模糊查询
     */
    @Query("SELECT d FROM Department d WHERE d.deptCode LIKE %:deptCode% AND d.tenantId = :tenantId")
    Page<Department> findByDeptCodeContainingAndTenantId(@Param("deptCode") String deptCode, @Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * 根据部门名称模糊查询
     */
    @Query("SELECT d FROM Department d WHERE d.deptName LIKE %:deptName% AND d.tenantId = :tenantId")
    Page<Department> findByDeptNameContainingAndTenantId(@Param("deptName") String deptName, @Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * 统计租户部门数量
     */
    long countByTenantId(Long tenantId);

    /**
     * 检查部门编码是否已存在
     */
    boolean existsByDeptCodeAndTenantId(String deptCode, Long tenantId);

    /**
     * 根据层级路径查找部门
     */
    @Query("SELECT d FROM Department d WHERE d.treePath LIKE :treePath% AND d.tenantId = :tenantId")
    List<Department> findByTreePathStartingWithAndTenantId(@Param("treePath") String treePath, @Param("tenantId") Long tenantId);

    /**
     * 查找叶子节点部门
     */
    @Query("SELECT d FROM Department d WHERE d.id NOT IN (SELECT DISTINCT d2.parentId FROM Department d2 WHERE d2.parentId IS NOT NULL) AND d.tenantId = :tenantId")
    List<Department> findLeafDepartments(@Param("tenantId") Long tenantId);
}