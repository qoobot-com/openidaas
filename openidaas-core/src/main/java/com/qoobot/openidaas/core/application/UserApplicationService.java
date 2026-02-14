package com.qoobot.openidaas.core.application;

import com.qoobot.openidaas.common.dto.user.UserCreateDTO;
import com.qoobot.openidaas.common.dto.user.UserQueryDTO;
import com.qoobot.openidaas.common.dto.user.UserUpdateDTO;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.common.util.PasswordUtil;
import com.qoobot.openidaas.common.vo.PageResultVO;
import com.qoobot.openidaas.common.vo.ResultVO;
import com.qoobot.openidaas.common.vo.user.UserVO;
import com.qoobot.openidaas.core.domain.User;
import com.qoobot.openidaas.core.mapper.UserMapper;
import com.qoobot.openidaas.core.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户应用服务
 *
 * @author QooBot
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserApplicationService {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * 创建用户
     */
    @Transactional
    public ResultVO<UserVO> createUser(UserCreateDTO userCreateDTO) {
        try {
            // 参数校验
            validateUserCreateDTO(userCreateDTO);
            
            // 检查用户名是否已存在
            if (userMapper.findByUsername(userCreateDTO.getUsername()) != null) {
                throw new BusinessException("用户名已存在");
            }

            // 检查邮箱是否已存在
            if (StringUtils.hasText(userCreateDTO.getEmail()) &&
                userMapper.findByEmail(userCreateDTO.getEmail()) != null) {
                throw new BusinessException("邮箱已存在");
            }

            // 检查手机号是否已存在
            if (StringUtils.hasText(userCreateDTO.getMobile()) &&
                userMapper.findByMobile(userCreateDTO.getMobile()) != null) {
                throw new BusinessException("手机号已存在");
            }
            
            // 创建用户
            User user = new User();
            user.setUsername(userCreateDTO.getUsername());
            user.setEmail(userCreateDTO.getEmail());
            user.setMobile(userCreateDTO.getMobile());
            user.setNickname(userCreateDTO.getNickname());
            user.setRealName(userCreateDTO.getRealName());
            user.setGender(userCreateDTO.getGender());
            user.setPassword(PasswordUtil.encode(userCreateDTO.getPassword()));
            user.setStatus(com.qoobot.openidaas.common.enumeration.UserStatusEnum.NORMAL);
            user.setTenantId(userCreateDTO.getTenantId());
            
            User savedUser = userService.createUser(userCreateDTO);
            
            // 转换为VO
            UserVO userVO = convertToVO(savedUser);
            
            log.info("用户创建成功: {}", user.getUsername());
            return ResultVO.success(userVO);
            
        } catch (BusinessException e) {
            log.error("创建用户失败: {}", e.getMessage());
            return ResultVO.error("-1", e.getMessage());
        } catch (Exception e) {
            log.error("创建用户异常", e);
            return ResultVO.error("-1", "创建用户失败");
        }
    }

    /**
     * 更新用户
     */
    @Transactional
    public ResultVO<UserVO> updateUser(Long userId, UserUpdateDTO userUpdateDTO) {
        try {
            // 检查用户是否存在
            User existingUser = userService.getUserById(userId);
            if (existingUser == null) {
                throw new BusinessException("用户不存在");
            }
            
            // 更新用户信息
            User updatedUser = userService.updateUser(userId, userUpdateDTO);
            
            // 转换为VO
            UserVO userVO = convertToVO(updatedUser);
            
            log.info("用户更新成功: {}", userId);
            return ResultVO.success(userVO);
            
        } catch (BusinessException e) {
            log.error("更新用户失败: {}", e.getMessage());
            return ResultVO.error("-1", e.getMessage());
        } catch (Exception e) {
            log.error("更新用户异常", e);
            return ResultVO.error("-1", "更新用户失败");
        }
    }

    /**
     * 删除用户
     */
    @Transactional
    public ResultVO<Void> deleteUser(Long userId) {
        try {
            userService.deleteUser(userId);
            log.info("用户删除成功: {}", userId);
            return ResultVO.success();
        } catch (BusinessException e) {
            log.error("删除用户失败: {}", e.getMessage());
            return ResultVO.error("-1", e.getMessage());
        } catch (Exception e) {
            log.error("删除用户异常", e);
            return ResultVO.error("-1", "删除用户失败");
        }
    }

    /**
     * 查询用户列表
     */
    public ResultVO<PageResultVO<UserVO>> queryUsers(UserQueryDTO queryDTO) {
        try {
            PageResultVO<UserVO> result = userService.queryUsers(queryDTO);
            return ResultVO.success(result);
        } catch (Exception e) {
            log.error("查询用户列表异常", e);
            return ResultVO.error("-1", "查询用户列表失败");
        }
    }

    /**
     * 根据ID获取用户
     */
    public ResultVO<UserVO> getUserById(Long userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResultVO.error("-1", "用户不存在");
            }
            UserVO userVO = convertToVO(user);
            return ResultVO.success(userVO);
        } catch (Exception e) {
            log.error("获取用户信息异常", e);
            return ResultVO.error("-1", "获取用户信息失败");
        }
    }

    /**
     * 重置用户密码
     */
    @Transactional
    public ResultVO<Void> resetPassword(Long userId, String newPassword) {
        try {
            userService.resetUserPassword(userId, newPassword);
            log.info("用户密码重置成功: {}", userId);
            return ResultVO.success();
        } catch (BusinessException e) {
            log.error("重置用户密码失败: {}", e.getMessage());
            return ResultVO.error("-1", e.getMessage());
        } catch (Exception e) {
            log.error("重置用户密码异常", e);
            return ResultVO.error("-1", "重置用户密码失败");
        }
    }

    /**
     * 启用/禁用用户
     */
    @Transactional
    public ResultVO<Void> toggleUserStatus(Long userId, Boolean enabled) {
        try {
            if (enabled) {
                userService.enableUser(userId);
            } else {
                userService.disableUser(userId);
            }
            log.info("用户状态更新成功: {}, 状态: {}", userId, enabled);
            return ResultVO.success();
        } catch (BusinessException e) {
            log.error("更新用户状态失败: {}", e.getMessage());
            return ResultVO.error("-1", e.getMessage());
        } catch (Exception e) {
            log.error("更新用户状态异常", e);
            return ResultVO.error("-1", "更新用户状态失败");
        }
    }

    /**
     * 参数校验
     */
    private void validateUserCreateDTO(UserCreateDTO userCreateDTO) {
        if (!StringUtils.hasText(userCreateDTO.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        if (!StringUtils.hasText(userCreateDTO.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        if (userCreateDTO.getTenantId() == null) {
            throw new BusinessException("租户ID不能为空");
        }
    }

    /**
     * 转换为VO对象
     */
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
