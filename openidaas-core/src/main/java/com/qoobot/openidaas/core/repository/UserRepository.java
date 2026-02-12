package com.qoobot.openidaas.core.repository;

import com.qoobot.openidaas.core.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 用户仓储接口
 *
 * @author QooBot
 */
public interface UserRepository extends BaseRepository<User, Long> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号查找用户
     */
    Optional<User> findByMobile(String mobile);

    /**
     * 根据用户名或邮箱查找用户
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * 根据租户ID查找用户列表
     */
    Page<User> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * 根据部门ID查找用户列表
     */
    @Query("SELECT u FROM User u JOIN u.departments d WHERE d.id = :deptId")
    List<User> findByDepartmentId(@Param("deptId") Long deptId);

    /**
     * 根据角色ID查找用户列表
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.id = :roleId")
    List<User> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID集合查找用户列表
     */
    List<User> findByIdIn(Set<Long> userIds);

    /**
     * 根据用户名模糊查询
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:username% AND u.tenantId = :tenantId")
    Page<User> findByUsernameContainingAndTenantId(@Param("username") String username, @Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * 根据昵称模糊查询
     */
    @Query("SELECT u FROM User u WHERE u.nickname LIKE %:nickname% AND u.tenantId = :tenantId")
    Page<User> findByNicknameContainingAndTenantId(@Param("nickname") String nickname, @Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * 统计租户用户数量
     */
    long countByTenantId(Long tenantId);

    /**
     * 统计启用用户数量
     */
    long countByTenantIdAndEnabledTrue(Long tenantId);

    /**
     * 查找登录失败次数超过指定次数的用户
     */
    @Query("SELECT u FROM User u WHERE u.loginFailCount >= :failCount AND u.tenantId = :tenantId")
    List<User> findUsersWithExcessiveLoginFailures(@Param("failCount") Integer failCount, @Param("tenantId") Long tenantId);
}