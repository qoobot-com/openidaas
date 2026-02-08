package com.qoobot.openidaas.user.repository;

import com.qoobot.openidaas.user.entity.Department;
import com.qoobot.openidaas.user.entity.DepartmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 部门数据访问接口
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * 根据编码查找部门
     */
    Optional<Department> findByCode(String code);

    /**
     * 根据租户ID查找部门
     */
    List<Department> findByTenantId(Long tenantId);

    /**
     * 查找子部门
     */
    List<Department> findByParentId(Long parentId);

    /**
     * 查找所有根部门
     */
    @Query("SELECT d FROM Department d WHERE d.parentId IS NULL")
    List<Department> findRootDepartments();

    /**
     * 根据租户ID查找根部门
     */
    @Query("SELECT d FROM Department d WHERE d.tenantId = :tenantId AND d.parentId IS NULL")
    List<Department> findRootDepartmentsByTenant(@Param("tenantId") Long tenantId);

    /**
     * 根据状态查找部门
     */
    List<Department> findByStatus(DepartmentStatus status);

    /**
     * 根据部门名称模糊查找
     */
    @Query("SELECT d FROM Department d WHERE d.name LIKE CONCAT('%', :name, '%')")
    List<Department> findByNameContaining(@Param("name") String name);

    /**
     * 获取部门树
     */
    @Query("SELECT d FROM Department d WHERE d.tenantId = :tenantId ORDER BY d.level, d.sortOrder")
    List<Department> findDepartmentTree(@Param("tenantId") Long tenantId);

    /**
     * 检查是否有子部门
     */
    boolean existsByParentId(Long parentId);

    /**
     * 检查部门是否有用户
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.departmentId = :deptId")
    boolean hasUsers(@Param("deptId") Long deptId);
}
