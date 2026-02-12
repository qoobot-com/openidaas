package com.qoobot.openidaas.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qoobot.openidaas.common.dto.user.UserCreateDTO;
import com.qoobot.openidaas.common.dto.user.UserPasswordDTO;
import com.qoobot.openidaas.common.dto.user.UserQueryDTO;
import com.qoobot.openidaas.common.dto.user.UserUpdateDTO;
import com.qoobot.openidaas.common.vo.user.UserVO;
import com.qoobot.openidaas.user.entity.User;

import java.util.List;

/**
 * 用户服务接口
 *
 * @author QooBot
 */
public interface UserService extends IService<User> {

    /**
     * 分页查询用户列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<User> selectUserPage(UserQueryDTO query);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User selectByUsername(String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    User selectByEmail(String email);

    /**
     * 根据手机号查询用户
     *
     * @param mobile 手机号
     * @return 用户信息
     */
    User selectByMobile(String mobile);

    /**
     * 创建用户
     *
     * @param createDTO 创建用户DTO
     * @return 用户VO
     */
    UserVO createUser(UserCreateDTO createDTO);

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param updateDTO 更新用户DTO
     * @return 用户VO
     */
    UserVO updateUser(Long userId, UserUpdateDTO updateDTO);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long userId);

    /**
     * 获取用户详情
     *
     * @param userId 用户ID
     * @return 用户VO
     */
    UserVO getUserDetail(Long userId);

    /**
     * 批量查询用户信息
     *
     * @param userIds 用户ID列表
     * @return 用户列表
     */
    List<User> selectBatchByIds(List<Long> userIds);

    /**
     * 重置用户密码
     *
     * @param passwordDTO 密码DTO
     * @return 是否成功
     */
    boolean resetPassword(UserPasswordDTO passwordDTO);

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 验证用户密码
     *
     * @param userId 用户ID
     * @param password 密码
     * @return 是否正确
     */
    boolean validatePassword(Long userId, String password);

    /**
     * 锁定用户账户
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean lockUser(Long userId);

    /**
     * 解锁用户账户
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean unlockUser(Long userId);

    /**
     * 停用用户账户
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean disableUser(Long userId);

    /**
     * 启用用户账户
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean enableUser(Long userId);

    /**
     * 更新最后登录信息
     *
     * @param userId 用户ID
     * @param loginIp 登录IP
     */
    void updateLastLoginInfo(Long userId, String loginIp);

    /**
     * 增加登录失败次数
     *
     * @param userId 用户ID
     */
    void incrementFailedAttempts(Long userId);

    /**
     * 分配用户部门
     *
     * @param userId 用户ID
     * @param deptIds 部门ID列表
     * @param positionId 职位ID
     * @param isPrimary 是否主部门
     * @return 是否成功
     */
    boolean assignDepartments(Long userId, List<Long> deptIds, Long positionId, Boolean isPrimary);

    /**
     * 分配用户角色
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @param scopeType 作用域类型
     * @param scopeId 作用域ID
     * @return 是否成功
     */
    boolean assignRoles(Long userId, List<Long> roleIds, Integer scopeType, Long scopeId);

    /**
     * 移除用户角色
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 是否成功
     */
    boolean removeRoles(Long userId, List<Long> roleIds);

    /**
     * 保存用户扩展属性
     *
     * @param userId 用户ID
     * @param attrKey 属性键
     * @param attrValue 属性值
     * @return 是否成功
     */
    boolean saveAttribute(Long userId, String attrKey, String attrValue);

    /**
     * 删除用户扩展属性
     *
     * @param userId 用户ID
     * @param attrKey 属性键
     * @return 是否成功
     */
    boolean deleteAttribute(Long userId, String attrKey);

    /**
     * 获取用户的所有权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<String> getUserPermissions(Long userId);
}
