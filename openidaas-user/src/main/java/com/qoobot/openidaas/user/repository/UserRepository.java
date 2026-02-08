package com.qoobot.openidaas.user.repository;

import com.qoobot.openidaas.user.entity.User;
import com.qoobot.openidaas.user.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据电话查找用户
     */
    Optional<User> findByPhone(String phone);

    /**
     * 根据员工ID查找用户
     */
    Optional<User> findByEmployeeId(String employeeId);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 根据状态查找用户
     */
    List<User> findByStatus(UserStatus status);

    /**
     * 根据租户ID查找用户
     */
    Page<User> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * 根据部门ID查找用户
     */
    Page<User> findByDepartmentId(Long departmentId, Pageable pageable);

    /**
     * 查找某部门的下属用户
     */
    @Query("SELECT u FROM User u WHERE u.departmentId IN " +
           "(SELECT d.id FROM Department d WHERE d.id = :deptId OR d.parentId = :deptId)")
    Page<User> findByDepartmentOrChildren(@Param("deptId") Long deptId, Pageable pageable);

    /**
     * 查找上级为指定用户的用户
     */
    Page<User> findByManagerId(Long managerId, Pageable pageable);

    /**
     * 全文搜索用户（PostgreSQL全文搜索）
     */
    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "100"))
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.fullname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.employeeId) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 高级搜索用户
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:tenantId IS NULL OR u.tenantId = :tenantId) AND " +
           "(:departmentId IS NULL OR u.departmentId = :departmentId) AND " +
           "(:keyword IS NULL OR " +
           " LOWER(u.fullname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> advancedSearch(
        @Param("status") UserStatus status,
        @Param("tenantId") Long tenantId,
        @Param("departmentId") Long departmentId,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    /**
     * 查找即将过期的密码用户
     */
    @Query("SELECT u FROM User u WHERE u.passwordExpiresAt BETWEEN :startDate AND :endDate")
    List<User> findUsersWithExpiringPasswords(
        @Param("startDate") java.time.LocalDateTime startDate,
        @Param("endDate") java.time.LocalDateTime endDate
    );

    /**
     * 查找已锁定的用户
     */
    @Query("SELECT u FROM User u WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil > CURRENT_TIMESTAMP")
    List<User> findLockedUsers();

    /**
     * 查找需要修改密码的用户
     */
    List<User> findByMustChangePasswordTrue();

    /**
     * 统计租户用户数
     */
    long countByTenantId(Long tenantId);

    /**
     * 统计部门用户数
     */
    long countByDepartmentId(Long departmentId);

    /**
     * 批量更新用户状态
     */
    @Query("UPDATE User u SET u.status = :status WHERE u.id IN :ids")
    int bulkUpdateStatus(@Param("status") UserStatus status, @Param("ids") List<Long> ids);

    /**
     * 查找活跃用户
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt >= :since")
    List<User> findActiveUsers(@Param("since") java.time.LocalDateTime since);

    /**
     * 查找不活跃用户
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt IS NULL OR u.lastLoginAt < :before")
    List<User> findInactiveUsers(@Param("before") java.time.LocalDateTime before);
    
    /**
     * 检查部门是否有用户
     */
    boolean existsByDepartmentId(Long departmentId);
    
    /**
     * 检查部门或其子部门是否有用户
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.departmentId = :deptId OR u.departmentId IN " +
           "(SELECT d.id FROM Department d WHERE d.parentId = :deptId)")
    boolean hasUsers(@Param("deptId") Long deptId);
}
