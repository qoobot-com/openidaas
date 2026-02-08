package com.qoobot.openidaas.user.repository;

import com.qoobot.openidaas.user.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 权限数据访问接口
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * 根据编码查找权限
     */
    Optional<Permission> findByCode(String code);

    /**
     * 查找所有根权限
     */
    @Query("SELECT p FROM Permission p WHERE p.parentId IS NULL ORDER BY p.sortOrder")
    List<Permission> findRootPermissions();

    /**
     * 查找子权限
     */
    List<Permission> findByParentId(Long parentId);

    /**
     * 根据资源类型查找权限
     */
    List<Permission> findByResourceType(String resourceType);

    /**
     * 根据用户ID查找权限
     */
    @Query("SELECT DISTINCT p FROM Permission p " +
           "JOIN p.roles r " +
           "JOIN r.userRoles ur " +
           "WHERE ur.user.id = :userId")
    List<Permission> findByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查找权限
     */
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.id = :roleId")
    List<Permission> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 查找可见的菜单权限
     */
    @Query("SELECT p FROM Permission p WHERE p.isVisible = true ORDER BY p.sortOrder")
    List<Permission> findVisibleMenus();
}
