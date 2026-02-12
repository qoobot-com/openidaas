package com.qoobot.openidaas.core.repository;

import com.qoobot.openidaas.core.domain.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 权限仓储接口
 *
 * @author QooBot
 */
public interface PermissionRepository extends BaseRepository<Permission, Long> {

    /**
     * 根据权限编码查找权限
     */
    Optional<Permission> findByPermCode(String permCode);

    /**
     * 根据权限名称查找权限
     */
    Optional<Permission> findByPermName(String permName);

    /**
     * 根据租户ID查找权限列表
     */
    Page<Permission> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * 根据父权限ID查找子权限列表
     */
    List<Permission> findByParentId(Long parentId);

    /**
     * 根据权限类型查找权限列表
     */
    List<Permission> findByPermType(String permType);

    /**
     * 根据租户ID查找顶级权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.parentId IS NULL AND p.tenantId = :tenantId ORDER BY p.sortOrder")
    List<Permission> findTopPermissionsByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据角色ID查找权限列表
     */
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.id = :roleId")
    List<Permission> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查找权限列表
     */
    @Query("SELECT DISTINCT p FROM Permission p JOIN p.users u WHERE u.id = :userId")
    List<Permission> findByUserId(@Param("userId") Long userId);

    /**
     * 根据权限ID集合查找权限列表
     */
    List<Permission> findByIdIn(Set<Long> permIds);

    /**
     * 根据权限编码模糊查询
     */
    @Query("SELECT p FROM Permission p WHERE p.permCode LIKE %:permCode% AND p.tenantId = :tenantId")
    Page<Permission> findByPermCodeContainingAndTenantId(@Param("permCode") String permCode, @Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * 根据权限名称模糊查询
     */
    @Query("SELECT p FROM Permission p WHERE p.permName LIKE %:permName% AND p.tenantId = :tenantId")
    Page<Permission> findByPermNameContainingAndTenantId(@Param("permName") String permName, @Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * 统计租户权限数量
     */
    long countByTenantId(Long tenantId);

    /**
     * 检查权限编码是否已存在
     */
    boolean existsByPermCodeAndTenantId(String permCode, Long tenantId);

    /**
     * 根据路径和方法查找API权限
     */
    @Query("SELECT p FROM Permission p WHERE p.path = :path AND p.method = :method AND p.permType = 'api' AND p.tenantId = :tenantId")
    Optional<Permission> findByPathAndMethodAndTenantId(@Param("path") String path, @Param("method") String method, @Param("tenantId") Long tenantId);

    /**
     * 查找菜单权限
     */
    @Query("SELECT p FROM Permission p WHERE p.permType = 'menu' AND p.tenantId = :tenantId ORDER BY p.sortOrder")
    List<Permission> findMenuPermissionsByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 查找按钮权限
     */
    @Query("SELECT p FROM Permission p WHERE p.permType = 'button' AND p.tenantId = :tenantId")
    List<Permission> findButtonPermissionsByTenantId(@Param("tenantId") Long tenantId);
}