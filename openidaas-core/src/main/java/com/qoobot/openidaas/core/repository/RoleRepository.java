package com.qoobot.openidaas.core.repository;

import com.qoobot.openidaas.core.domain.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 角色仓储接口
 *
 * @author QooBot
 */
public interface RoleRepository extends BaseRepository<Role, Long> {

    /**
     * 根据角色编码查找角色
     */
    Optional<Role> findByRoleCode(String roleCode);

    /**
     * 根据角色名称查找角色
     */
    Optional<Role> findByRoleName(String roleName);

    /**
     * 根据租户ID查找角色列表
     */
    Page<Role> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * 根据角色类型查找角色列表
     */
    List<Role> findByRoleType(com.qoobot.openidaas.common.enumeration.RoleTypeEnum roleType);

    /**
     * 根据租户ID和启用状态查找角色列表
     */
    List<Role> findByTenantIdAndEnabledTrue(Long tenantId);

    /**
     * 根据用户ID查找角色列表
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    List<Role> findByUserId(@Param("userId") Long userId);

    /**
     * 根据权限ID查找角色列表
     */
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.id = :permId")
    List<Role> findByPermissionId(@Param("permId") Long permId);

    /**
     * 根据角色ID集合查找角色列表
     */
    List<Role> findByIdIn(Set<Long> roleIds);

    /**
     * 根据角色编码模糊查询
     */
    @Query("SELECT r FROM Role r WHERE r.roleCode LIKE %:roleCode% AND r.tenantId = :tenantId")
    Page<Role> findByRoleCodeContainingAndTenantId(@Param("roleCode") String roleCode, @Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * 根据角色名称模糊查询
     */
    @Query("SELECT r FROM Role r WHERE r.roleName LIKE %:roleName% AND r.tenantId = :tenantId")
    Page<Role> findByRoleNameContainingAndTenantId(@Param("roleName") String roleName, @Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * 统计租户角色数量
     */
    long countByTenantId(Long tenantId);

    /**
     * 检查角色编码是否已存在
     */
    boolean existsByRoleCodeAndTenantId(String roleCode, Long tenantId);

    /**
     * 查找默认角色
     */
    @Query("SELECT r FROM Role r WHERE r.roleType = 'DEFAULT' AND r.tenantId = :tenantId")
    List<Role> findDefaultRoles(@Param("tenantId") Long tenantId);
}