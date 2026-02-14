package com.qoobot.openidaas.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openidaas.common.dto.user.UserCreateDTO;
import com.qoobot.openidaas.common.dto.user.UserQueryDTO;
import com.qoobot.openidaas.common.dto.user.UserUpdateDTO;
import com.qoobot.openidaas.common.enumeration.UserStatusEnum;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.common.util.PasswordUtil;
import com.qoobot.openidaas.common.vo.PageResultVO;
import com.qoobot.openidaas.common.vo.user.UserVO;
import com.qoobot.openidaas.core.domain.User;
import com.qoobot.openidaas.core.domain.UserDepartment;
import com.qoobot.openidaas.core.domain.UserRole;
import com.qoobot.openidaas.core.mapper.PermissionMapper;
import com.qoobot.openidaas.core.mapper.UserMapper;
import com.qoobot.openidaas.core.mapper.UserRoleMapper;
import com.qoobot.openidaas.core.mapper.UserDepartmentMapper;
import com.qoobot.openidaas.core.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户服务实现类（MyBatis-Plus版本）
 *
 * @author QooBot
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserDepartmentMapper userDepartmentMapper;
    private final PermissionMapper permissionMapper;

    @Override
    @Transactional
    public User createUser(UserCreateDTO userCreateDTO) {
        User user = new User();
        user.setUsername(userCreateDTO.getUsername());
        user.setEmail(userCreateDTO.getEmail());
        user.setMobile(userCreateDTO.getMobile());
        user.setNickname(userCreateDTO.getNickname());
        user.setRealName(userCreateDTO.getRealName());
        user.setGender(userCreateDTO.getGender());
        user.setPassword(PasswordUtil.encode(userCreateDTO.getPassword()));
        user.setStatus(UserStatusEnum.NORMAL);
        user.setTenantId(userCreateDTO.getTenantId());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
        return user;
    }

    @Override
    @Transactional
    public User updateUser(Long userId, UserUpdateDTO userUpdateDTO) {
        User user = getUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (StringUtils.hasText(userUpdateDTO.getEmail())) {
            user.setEmail(userUpdateDTO.getEmail());
        }
        if (StringUtils.hasText(userUpdateDTO.getMobile())) {
            user.setMobile(userUpdateDTO.getMobile());
        }
        if (StringUtils.hasText(userUpdateDTO.getNickname())) {
            user.setNickname(userUpdateDTO.getNickname());
        }
        if (StringUtils.hasText(userUpdateDTO.getRealName())) {
            user.setRealName(userUpdateDTO.getRealName());
        }
        if (userUpdateDTO.getGender() != null) {
            user.setGender(userUpdateDTO.getGender());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return user;
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 删除用户角色关联
        userRoleMapper.deleteByUserId(userId);
        // 删除用户部门关联
        userDepartmentMapper.deleteByUserId(userId);
        // 删除用户
        userMapper.deleteById(userId);
    }

    @Override
    @Transactional
    public void deleteUsers(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        // 批量删除用户角色关联
        userIds.forEach(userRoleMapper::deleteByUserId);
        // 批量删除用户部门关联
        userIds.forEach(userDepartmentMapper::deleteByUserId);
        // 批量删除用户
        userMapper.deleteBatchIds(userIds);
    }

    @Override
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public PageResultVO<UserVO> queryUsers(UserQueryDTO queryDTO) {
        // 使用LambdaQueryWrapper替代Specification
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getUsername()), User::getUsername, queryDTO.getUsername())
               .like(StringUtils.hasText(queryDTO.getNickname()), User::getNickname, queryDTO.getNickname())
               .eq(queryDTO.getStatus() != null, User::getStatus, queryDTO.getStatus())
               .eq(queryDTO.getTenantId() != null, User::getTenantId, queryDTO.getTenantId())
               .orderByDesc(User::getCreatedAt);

        // 创建MyBatis-Plus分页对象
        Page<User> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<User> result = userMapper.selectPage(page, wrapper);

        List<UserVO> userVOs = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResultVO<>(
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                userVOs
        );
    }

    @Override
    public List<Long> getUserRoleIds(Long userId) {
        return userRoleMapper.findRoleIdsByUserId(userId);
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        // 通过Mapper直接查询用户权限
        return permissionMapper.findByUserId(userId).stream()
                .map(permission -> permission.getPermCode())
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getUserMenuPermissions(Long userId) {
        // 通过Mapper查询用户菜单权限
        return permissionMapper.findByUserId(userId).stream()
                .filter(permission -> "menu".equals(permission.getPermType()))
                .map(permission -> permission.getPermCode())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignRolesToUser(Long userId, Set<Long> roleIds) {
        // 先删除原有角色关联
        userRoleMapper.deleteByUserId(userId);

        // 批量插入新的角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            List<UserRole> userRoles = roleIds.stream()
                    .map(roleId -> {
                        UserRole userRole = new UserRole();
                        userRole.setUserId(userId);
                        userRole.setRoleId(roleId);
                        return userRole;
                    })
                    .collect(Collectors.toList());
            userRoleMapper.batchInsert(userRoles);
        }

        log.info("为用户{}分配角色: {}", userId, roleIds);
    }

    @Override
    @Transactional
    public void assignDepartmentsToUser(Long userId, Set<Long> deptIds) {
        // 先删除原有部门关联
        userDepartmentMapper.deleteByUserId(userId);

        // 批量插入新的部门关联
        if (deptIds != null && !deptIds.isEmpty()) {
            List<UserDepartment> userDepts = deptIds.stream()
                    .map(deptId -> {
                        UserDepartment userDept =
                            new UserDepartment();
                        userDept.setUserId(userId);
                        userDept.setDeptId(deptId);
                        return userDept;
                    })
                    .collect(Collectors.toList());
            userDepartmentMapper.batchInsert(userDepts);
        }

        log.info("为用户{}分配部门: {}", userId, deptIds);
    }

    @Override
    @Transactional
    public void resetUserPassword(Long userId, String newPassword) {
        User user = getUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setPassword(PasswordUtil.encode(newPassword));
        user.setPwdResetTime(LocalDateTime.now());
        user.setPwdResetRequired(false);
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void lockUser(Long userId) {
        User user = getUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setStatus(UserStatusEnum.LOCKED);
        user.setLockTime(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void unlockUser(Long userId) {
        User user = getUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setStatus(UserStatusEnum.NORMAL);
        user.setLockTime(null);
        user.setLoginFailCount(0);
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void enableUser(Long userId) {
        User user = getUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setEnabled(true);
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void disableUser(Long userId) {
        User user = getUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setEnabled(false);
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void recordUserLogin(Long userId, String ip) {
        User user = getUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        user.setLoginFailCount(0);
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void incrementLoginFailCount(Long userId) {
        User user = getUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setLoginFailCount(user.getLoginFailCount() + 1);
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void clearLoginFailCount(Long userId) {
        User user = getUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setLoginFailCount(0);
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.updateById(user);
    }

    @Override
    public boolean canUserLogin(Long userId) {
        User user = getUserById(userId);
        if (user == null) {
            return false;
        }

        return user.getEnabled() &&
               user.getStatus() == UserStatusEnum.NORMAL &&
               (user.getLockTime() == null || user.getLockTime().isBefore(LocalDateTime.now().minusMinutes(30)));
    }

    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setMobile(user.getMobile());
        vo.setNickname(user.getNickname());
        vo.setRealName(user.getRealName());
        vo.setAvatar(user.getAvatar());
        vo.setGender(user.getGender());
        vo.setStatus(user.getStatus() != null ? user.getStatus().getCode() : null);
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setLastLoginIp(user.getLastLoginIp());
        vo.setCreateTime(user.getCreatedAt());
        vo.setUpdateTime(user.getUpdatedAt());
        vo.setTenantId(user.getTenantId());
        return vo;
    }
}
