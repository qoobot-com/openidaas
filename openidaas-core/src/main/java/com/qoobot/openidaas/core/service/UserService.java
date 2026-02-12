package com.qoobot.openidaas.core.service;

import com.qoobot.openidaas.common.dto.user.UserCreateDTO;
import com.qoobot.openidaas.common.dto.user.UserQueryDTO;
import com.qoobot.openidaas.common.dto.user.UserUpdateDTO;
import com.qoobot.openidaas.common.vo.PageResultVO;
import com.qoobot.openidaas.common.vo.user.UserVO;
import com.qoobot.openidaas.core.domain.User;

import java.util.List;
import java.util.Set;

/**
 * 用户服务接口
 *
 * @author QooBot
 */
public interface UserService {

    /**
     * 创建用户
     */
    User createUser(UserCreateDTO userCreateDTO);

    /**
     * 更新用户
     */
    User updateUser(Long userId, UserUpdateDTO userUpdateDTO);

    /**
     * 删除用户
     */
    void deleteUser(Long userId);

    /**
     * 批量删除用户
     */
    void deleteUsers(Set<Long> userIds);

    /**
     * 根据ID获取用户
     */
    User getUserById(Long userId);

    /**
     * 根据用户名获取用户
     */
    User getUserByUsername(String username);

    /**
     * 查询用户列表
     */
    PageResultVO<UserVO> queryUsers(UserQueryDTO queryDTO);

    /**
     * 获取用户拥有的角色
     */
    List<Long> getUserRoleIds(Long userId);

    /**
     * 获取用户拥有的权限
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 获取用户菜单权限
     */
    List<String> getUserMenuPermissions(Long userId);

    /**
     * 分配用户角色
     */
    void assignRolesToUser(Long userId, Set<Long> roleIds);

    /**
     * 分配用户部门
     */
    void assignDepartmentsToUser(Long userId, Set<Long> deptIds);

    /**
     * 重置用户密码
     */
    void resetUserPassword(Long userId, String newPassword);

    /**
     * 锁定用户
     */
    void lockUser(Long userId);

    /**
     * 解锁用户
     */
    void unlockUser(Long userId);

    /**
     * 启用用户
     */
    void enableUser(Long userId);

    /**
     * 禁用用户
     */
    void disableUser(Long userId);

    /**
     * 记录用户登录信息
     */
    void recordUserLogin(Long userId, String ip);

    /**
     * 增加登录失败次数
     */
    void incrementLoginFailCount(Long userId);

    /**
     * 清除登录失败次数
     */
    void clearLoginFailCount(Long userId);

    /**
     * 检查用户是否可以登录
     */
    boolean canUserLogin(Long userId);
}