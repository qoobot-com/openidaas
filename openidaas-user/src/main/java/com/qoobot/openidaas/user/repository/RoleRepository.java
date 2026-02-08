package com.qoobot.openidaas.user.repository;

import com.qoobot.openidaas.user.entity.Role;
import com.qoobot.openidaas.user.entity.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色数据访问接口
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * 根据编码查找角色
     */
    Optional<Role> findByCode(String code);

    /**
     * 根据名称查找角色
     */
    Optional<Role> findByName(String name);

    /**
     * 根据租户ID查找角色
     */
    Page<Role> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * 根据类型查找角色
     */
    List<Role> findByType(RoleType type);

    /**
     * 查找系统角色
     */
    List<Role> findByIsSystemTrue();

    /**
     * 查找非系统角色
     */
    List<Role> findByIsSystemFalse();

    /**
     * 根据权限ID查找角色
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.id = :permissionId")
    List<Role> findByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 根据用户ID查找角色
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.userRoles ur WHERE ur.user.id = :userId")
    List<Role> findByUserId(@Param("userId") Long userId);

    /**
     * 搜索角色
     */
    @Query("SELECT r FROM Role r WHERE " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Role> searchRoles(@Param("keyword") String keyword, Pageable pageable);
}
