package com.qoobot.openidaas.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qoobot.openidaas.common.dto.user.UserCreateDTO;
import com.qoobot.openidaas.common.dto.user.UserPasswordDTO;
import com.qoobot.openidaas.common.dto.user.UserQueryDTO;
import com.qoobot.openidaas.common.dto.user.UserUpdateDTO;
import com.qoobot.openidaas.common.enumeration.UserStatusEnum;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.common.util.PasswordUtil;
import com.qoobot.openidaas.common.vo.user.UserVO;
import com.qoobot.openidaas.user.entity.User;
import com.qoobot.openidaas.user.entity.UserAttribute;
import com.qoobot.openidaas.user.entity.UserDepartment;
import com.qoobot.openidaas.user.entity.UserProfile;
import com.qoobot.openidaas.user.entity.UserRole;
import com.qoobot.openidaas.user.mapper.*;
import com.qoobot.openidaas.user.service.UserService;
import com.qoobot.openidaas.user.service.converter.UserConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现类
 *
 * @author QooBot
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserDepartmentMapper userDepartmentMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserAttributeMapper userAttributeMapper;
    private final UserConverter userConverter;

    @Override
    public IPage<User> selectUserPage(UserQueryDTO query) {
        Page<User> page = new Page<>(query.getPage(), query.getSize());
        return userMapper.selectUserPage(page, query);
    }

    @Override
    public User selectByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public User selectByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public User selectByMobile(String mobile) {
        return userMapper.selectByMobile(mobile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO createUser(UserCreateDTO createDTO) {
        // 检查用户名是否已存在
        User existUser = selectByUsername(createDTO.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (StringUtils.hasText(createDTO.getEmail())) {
            existUser = selectByEmail(createDTO.getEmail());
            if (existUser != null) {
                throw new BusinessException("邮箱已存在");
            }
        }

        // 检查手机号是否已存在
        if (StringUtils.hasText(createDTO.getMobile())) {
            existUser = selectByMobile(createDTO.getMobile());
            if (existUser != null) {
                throw new BusinessException("手机号已存在");
            }
        }

        // 生成密码哈希
        String passwordHash = PasswordUtil.encodePassword(createDTO.getPassword());

        // 创建用户
        User user = new User();
        user.setUsername(createDTO.getUsername());
        user.setEmail(createDTO.getEmail());
        user.setMobile(createDTO.getMobile());
        user.setPasswordHash(passwordHash);
        user.setPasswordUpdatedAt(LocalDateTime.now());
        user.setStatus(UserStatusEnum.ACTIVE.getCode());
        user.setFailedLoginAttempts(0);
        user.setPwdResetRequired(0);

        userMapper.insert(user);

        // 创建用户档案
        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setNickname(createDTO.getNickname());
        profile.setFullName(createDTO.getRealName());
        profile.setAvatarUrl(createDTO.getAvatar());
        profile.setGender(createDTO.getGender());
        userProfileMapper.insert(profile);

        // 注意：UserCreateDTO中没有departmentIds字段，需要通过其他方式处理部门分配

        log.info("创建用户成功, userId: {}, username: {}", user.getId(), user.getUsername());
        return userConverter.toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUser(Long userId, UserUpdateDTO updateDTO) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查邮箱是否被其他用户使用
        if (StringUtils.hasText(updateDTO.getEmail())) {
            User existUser = selectByEmail(updateDTO.getEmail());
            if (existUser != null && !existUser.getId().equals(userId)) {
                throw new BusinessException("邮箱已被其他用户使用");
            }
            user.setEmail(updateDTO.getEmail());
        }

        // 检查手机号是否被其他用户使用
        if (StringUtils.hasText(updateDTO.getMobile())) {
            User existUser = selectByMobile(updateDTO.getMobile());
            if (existUser != null && !existUser.getId().equals(userId)) {
                throw new BusinessException("手机号已被其他用户使用");
            }
            user.setMobile(updateDTO.getMobile());
        }

        // 更新用户档案
        UserProfile profile = userProfileMapper.selectById(userId);
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
        }
        profile.setNickname(updateDTO.getNickname());
        profile.setFullName(updateDTO.getRealName());
        profile.setAvatarUrl(updateDTO.getAvatar());
        profile.setGender(updateDTO.getGender());
        
        if (profile.getUserId() == null) {
            userProfileMapper.insert(profile);
        } else {
            userProfileMapper.updateById(profile);
        }

        // 更新用户状态
        if (updateDTO.getStatus() != null) {
            user.setStatus(updateDTO.getStatus());
        }

        userMapper.updateById(user);
        log.info("更新用户成功, userId: {}", userId);
        return userConverter.toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 逻辑删除用户
        user.setStatus(UserStatusEnum.DISABLED.getCode());
        userMapper.updateById(user);

        // 删除用户档案
        userProfileMapper.deleteById(userId);

        // 删除用户部门关系
        userDepartmentMapper.deleteByUserId(userId);

        // 删除用户角色
        userRoleMapper.deleteByUserId(userId);

        // 删除用户扩展属性
        userAttributeMapper.deleteByUserId(userId);

        log.info("删除用户成功, userId: {}", userId);
        return true;
    }

    @Override
    public UserVO getUserDetail(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return userConverter.toDetailVO(user);
    }

    @Override
    public List<User> selectBatchByIds(List<Long> userIds) {
        return userMapper.selectBatchIds(userIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(UserPasswordDTO passwordDTO) {
        // 从上下文或其他方式获取当前用户ID
        Long userId = 1L; // 临时使用固定值，实际应从SecurityContext获取
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 生成新密码哈希
        String passwordHash = PasswordUtil.encodePassword(passwordDTO.getNewPassword());

        // 更新密码
        user.setPasswordHash(passwordHash);
        user.setPasswordUpdatedAt(LocalDateTime.now());
        user.setPwdResetRequired(0);
        user.setPwdResetTime(LocalDateTime.now());
        user.setPwdResetBy(0L); // 系统重置

        userMapper.updateById(user);
        log.info("重置用户密码成功, userId: {}", userId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证旧密码
        boolean isValid = validatePassword(userId, oldPassword);
        if (!isValid) {
            throw new BusinessException("旧密码不正确");
        }

        // 生成新密码哈希
        String passwordHash = PasswordUtil.encodePassword(newPassword);

        // 更新密码
        user.setPasswordHash(passwordHash);
        user.setPasswordUpdatedAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);

        userMapper.updateById(user);
        log.info("修改用户密码成功, userId: {}", userId);
        return true;
    }

    @Override
    public boolean validatePassword(Long userId, String password) {
        User user = getById(userId);
        if (user == null) {
            return false;
        }
        return PasswordUtil.matches(password, user.getPasswordHash());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean lockUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(UserStatusEnum.LOCKED.getCode());
        userMapper.updateById(user);
        log.info("锁定用户账户, userId: {}", userId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlockUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(UserStatusEnum.ACTIVE.getCode());
        user.setFailedLoginAttempts(0);
        userMapper.updateById(user);
        log.info("解锁用户账户, userId: {}", userId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(UserStatusEnum.DISABLED.getCode());
        userMapper.updateById(user);
        log.info("停用用户账户, userId: {}", userId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(UserStatusEnum.ACTIVE.getCode());
        userMapper.updateById(user);
        log.info("启用用户账户, userId: {}", userId);
        return true;
    }

    @Override
    public void updateLastLoginInfo(Long userId, String loginIp) {
        userMapper.updateLastLoginInfo(userId, loginIp);
    }

    @Override
    public void incrementFailedAttempts(Long userId) {
        User user = getById(userId);
        if (user != null) {
            userMapper.incrementFailedAttempts(userId);
            // 检查是否需要锁定账户
            if (user.getFailedLoginAttempts() >= 4) { // 5次失败后锁定
                lockUser(userId);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignDepartments(Long userId, List<Long> deptIds, Long positionId, Boolean isPrimary) {
        // 删除现有部门关系
        userDepartmentMapper.deleteByUserId(userId);

        // 添加新的部门关系
        for (int i = 0; i < deptIds.size(); i++) {
            UserDepartment ud = new UserDepartment();
            ud.setUserId(userId);
            ud.setDeptId(deptIds.get(i));
            ud.setPositionId(positionId);
            ud.setIsPrimary(isPrimary && i == 0 ? 1 : 0);
            ud.setStartDate(LocalDateTime.now().toLocalDate());
            userDepartmentMapper.insert(ud);
        }

        log.info("分配用户部门成功, userId: {}, deptIds: {}", userId, deptIds);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoles(Long userId, List<Long> roleIds, Integer scopeType, Long scopeId) {
        // 删除现有角色
        userRoleMapper.deleteByUserId(userId);

        // 添加新角色
        for (Long roleId : roleIds) {
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            ur.setScopeType(scopeType);
            ur.setScopeId(scopeId);
            ur.setGrantTime(LocalDateTime.now());
            ur.setIsTemporary(0);
            userRoleMapper.insert(ur);
        }

        log.info("分配用户角色成功, userId: {}, roleIds: {}", userId, roleIds);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeRoles(Long userId, List<Long> roleIds) {
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId)
               .in(UserRole::getRoleId, roleIds);
        userRoleMapper.delete(wrapper);
        log.info("移除用户角色成功, userId: {}, roleIds: {}", userId, roleIds);
        return true;
    }

    @Override
    public boolean saveAttribute(Long userId, String attrKey, String attrValue) {
        UserAttribute attr = userAttributeMapper.selectByKey(userId, attrKey);
        if (attr == null) {
            attr = new UserAttribute();
            attr.setUserId(userId);
            attr.setAttrKey(attrKey);
            attr.setAttrValue(attrValue);
            attr.setAttrType("STRING");
            userAttributeMapper.insert(attr);
        } else {
            attr.setAttrValue(attrValue);
            userAttributeMapper.updateById(attr);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAttribute(Long userId, String attrKey) {
        LambdaQueryWrapper<UserAttribute> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAttribute::getUserId, userId)
               .eq(UserAttribute::getAttrKey, attrKey);
        userAttributeMapper.delete(wrapper);
        return true;
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        // TODO: 从角色权限关联表中查询用户的权限
        return List.of();
    }
}
