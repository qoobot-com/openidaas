package com.qoobot.openidaas.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openidaas.core.domain.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper接口
 *
 * @author QooBot
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查找用户
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查找用户
     */
    @Select("SELECT * FROM users WHERE email = #{email}")
    User findByEmail(@Param("email") String email);

    /**
     * 根据手机号查找用户
     */
    @Select("SELECT * FROM users WHERE mobile = #{mobile}")
    User findByMobile(@Param("mobile") String mobile);

    /**
     * 根据租户ID查找用户列表（分页）
     */
    IPage<User> findByTenantId(Page<User> page, @Param("tenantId") Long tenantId);

    /**
     * 根据部门ID查找用户列表
     * 【需在XML中实现】涉及中间表关联查询
     */
    List<User> findByDepartmentId(@Param("deptId") Long deptId);

    /**
     * 根据角色ID查找用户列表
     * 【需在XML中实现】涉及中间表关联查询
     */
    List<User> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户名模糊查询（分页）
     */
    IPage<User> findByUsernameContainingAndTenantId(Page<User> page, @Param("username") String username, @Param("tenantId") Long tenantId);

    /**
     * 根据昵称模糊查询（分页）
     */
    IPage<User> findByNicknameContainingAndTenantId(Page<User> page, @Param("nickname") String nickname, @Param("tenantId") Long tenantId);

    /**
     * 统计租户用户数量
     */
    @Select("SELECT COUNT(*) FROM users WHERE tenant_id = #{tenantId}")
    long countByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 统计启用用户数量
     */
    @Select("SELECT COUNT(*) FROM users WHERE tenant_id = #{tenantId} AND status = 'ACTIVE'")
    long countByTenantIdAndEnabledTrue(@Param("tenantId") Long tenantId);

    /**
     * 查找登录失败次数超过指定次数的用户
     */
    @Select("SELECT * FROM users WHERE login_fail_count >= #{failCount} AND tenant_id = #{tenantId}")
    List<User> findUsersWithExcessiveLoginFailures(@Param("failCount") Integer failCount, @Param("tenantId") Long tenantId);

    /**
     * 根据用户名或邮箱查找用户
     */
    @Select("SELECT * FROM users WHERE username = #{param} OR email = #{param} LIMIT 1")
    User findByUsernameOrEmail(@Param("param") String param);

    /**
     * 根据ID集合查询用户
     */
    @Select("<script>" +
            "SELECT * FROM users WHERE id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<User> findByIdIn(@Param("ids") List<Long> ids);
}
