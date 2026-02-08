package com.qoobot.openidaas.core.service;

import com.qoobot.openidaas.core.dto.UserDTO;
import com.qoobot.openidaas.core.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务接口
 * 
 * 提供用户管理、查询等功能
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public interface UserService {
    
    /**
     * 创建用户
     * 
     * @param user 用户实体
     * @return 创建后的用户
     */
    User createUser(User user);
    
    /**
     * 更新用户
     * 
     * @param user 用户实体
     * @return 更新后的用户
     */
    User updateUser(User user);
    
    /**
     * 删除用户
     * 
     * @param userId 用户ID
     */
    void deleteUser(Long userId);
    
    /**
     * 根据ID查找用户
     * 
     * @param userId 用户ID
     * @return 用户实体
     */
    Optional<User> findUserById(Long userId);
    
    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return 用户实体
     */
    Optional<User> findUserByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     * 
     * @param email 邮箱
     * @return 用户实体
     */
    Optional<User> findUserByEmail(String email);
    
    /**
     * 分页查询用户
     * 
     * @param pageable 分页参数
     * @return 用户分页结果
     */
    Page<User> findAllUsers(Pageable pageable);
    
    /**
     * 根据租户ID查询用户列表
     * 
     * @param tenantId 租户ID
     * @return 用户列表
     */
    List<User> findUsersByTenantId(Long tenantId);
    
    /**
     * 更新用户密码
     * 
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 重置用户密码
     * 
     * @param userId 用户ID
     * @param newPassword 新密码
     */
    void resetPassword(Long userId, String newPassword);
    
    /**
     * 启用/禁用用户
     * 
     * @param userId 用户ID
     * @param enabled 是否启用
     */
    void toggleUserStatus(Long userId, Boolean enabled);
    
    /**
     * 转换为DTO
     * 
     * @param user 用户实体
     * @return 用户DTO
     */
    UserDTO toDTO(User user);
}
