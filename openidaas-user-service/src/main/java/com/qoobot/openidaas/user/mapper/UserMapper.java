package com.qoobot.openidaas.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openidaas.common.dto.user.UserQueryDTO;
import com.qoobot.openidaas.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper接口
 *
 * @author QooBot
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 分页查询用户列表
     *
     * @param page 分页对象
     * @param query 查询条件
     * @return 用户列表
     */
    IPage<User> selectUserPage(Page<User> page, @Param("query") UserQueryDTO query);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM users WHERE username = #{username} AND deleted = 0")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("SELECT * FROM users WHERE email = #{email} AND deleted = 0")
    User selectByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     *
     * @param mobile 手机号
     * @return 用户信息
     */
    @Select("SELECT * FROM users WHERE mobile = #{mobile} AND deleted = 0")
    User selectByMobile(@Param("mobile") String mobile);

    /**
     * 批量查询用户信息
     *
     * @param userIds 用户ID列表
     * @return 用户列表
     */
    List<User> selectBatchByIds(@Param("userIds") List<Long> userIds);

    /**
     * 更新用户最后登录信息
     *
     * @param userId 用户ID
     * @param loginIp 登录IP
     * @return 影响行数
     */
    @Select("UPDATE users SET last_login_time = NOW(), last_login_ip = #{loginIp}, " +
            "failed_login_attempts = 0 WHERE id = #{userId}")
    int updateLastLoginInfo(@Param("userId") Long userId, @Param("loginIp") String loginIp);

    /**
     * 增加登录失败次数
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    @Select("UPDATE users SET failed_login_attempts = failed_login_attempts + 1 " +
            "WHERE id = #{userId}")
    int incrementFailedAttempts(@Param("userId") Long userId);

    /**
     * 锁定用户账户
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    @Select("UPDATE users SET status = 2, updated_at = NOW() WHERE id = #{userId}")
    int lockUserAccount(@Param("userId") Long userId);

    /**
     * 重置密码
     *
     * @param userId 用户ID
     * @param passwordHash 密码哈希
     * @param resetBy 重置操作人
     * @return 影响行数
     */
    @Select("UPDATE users SET password_hash = #{passwordHash}, " +
            "password_updated_at = NOW(), pwd_reset_required = 0, " +
            "pwd_reset_time = NOW(), pwd_reset_by = #{resetBy}, updated_at = NOW() " +
            "WHERE id = #{userId}")
    int resetPassword(@Param("userId") Long userId, @Param("passwordHash") String passwordHash,
                       @Param("resetBy") Long resetBy);
}
