package com.qoobot.openidaas.core.service.impl;

import com.qoobot.openidaas.common.dto.user.UserCreateDTO;
import com.qoobot.openidaas.common.dto.user.UserQueryDTO;
import com.qoobot.openidaas.common.dto.user.UserUpdateDTO;
import com.qoobot.openidaas.common.enumeration.UserStatusEnum;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.common.util.PasswordUtil;
import com.qoobot.openidaas.common.vo.PageResultVO;
import com.qoobot.openidaas.common.vo.user.UserVO;
import com.qoobot.openidaas.core.domain.User;
import com.qoobot.openidaas.core.repository.UserRepository;
import com.qoobot.openidaas.core.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 用户服务实现类
 *
 * @author QooBot
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

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
        
        return userRepository.save(user);
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
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void deleteUsers(Set<Long> userIds) {
        for (Long userId : userIds) {
            deleteUser(userId);
        }
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public PageResultVO<UserVO> queryUsers(UserQueryDTO queryDTO) {
        Pageable pageable = PageRequest.of(
            queryDTO.getPage().intValue() - 1, 
            queryDTO.getSize().intValue()
        );
        
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(queryDTO.getUsername())) {
                predicates.add(criteriaBuilder.like(root.get("username"), "%" + queryDTO.getUsername() + "%"));
            }
            if (StringUtils.hasText(queryDTO.getNickname())) {
                predicates.add(criteriaBuilder.like(root.get("nickname"), "%" + queryDTO.getNickname() + "%"));
            }
            if (queryDTO.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), queryDTO.getStatus()));
            }
            if (queryDTO.getTenantId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("tenantId"), queryDTO.getTenantId()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<User> page = userRepository.findAll(spec, pageable);
        
        List<UserVO> userVOs = page.getContent().stream()
                .map(this::convertToVO)
                .toList();
        
        return new PageResultVO<UserVO>(
                page.getNumber() + 1L,
                (long) page.getSize(),
                page.getTotalElements(),
                userVOs
        );
    }

    @Override
    public List<Long> getUserRoleIds(Long userId) {
        User user = getUserById(userId);
        if (user != null && user.getRoles() != null) {
            return user.getRoles().stream()
                    .map(role -> role.getId())
                    .toList();
        }
        return List.of();
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        User user = getUserById(userId);
        if (user != null && user.getPermissions() != null) {
            return user.getPermissions().stream()
                    .map(permission -> permission.getPermCode())
                    .toList();
        }
        return List.of();
    }

    @Override
    public List<String> getUserMenuPermissions(Long userId) {
        User user = getUserById(userId);
        if (user != null && user.getPermissions() != null) {
            return user.getPermissions().stream()
                    .filter(permission -> "menu".equals(permission.getPermType()))
                    .map(permission -> permission.getPermCode())
                    .toList();
        }
        return List.of();
    }

    @Override
    @Transactional
    public void assignRolesToUser(Long userId, Set<Long> roleIds) {
        // 实现角色分配逻辑
        log.info("为用户{}分配角色: {}", userId, roleIds);
    }

    @Override
    @Transactional
    public void assignDepartmentsToUser(Long userId, Set<Long> deptIds) {
        // 实现部门分配逻辑
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
        
        userRepository.save(user);
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
        
        userRepository.save(user);
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
        
        userRepository.save(user);
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
        
        userRepository.save(user);
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
        
        userRepository.save(user);
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
        
        userRepository.save(user);
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
        
        userRepository.save(user);
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
        
        userRepository.save(user);
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