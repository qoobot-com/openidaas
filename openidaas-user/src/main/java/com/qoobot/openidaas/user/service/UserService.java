package com.qoobot.openidaas.user.service;

import com.qoobot.openidaas.user.dto.*;
import com.qoobot.openidaas.user.entity.*;
import com.qoobot.openidaas.user.exception.*;
import com.qoobot.openidaas.user.mapper.UserMapper;
import com.qoobot.openidaas.user.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户服务类
 * 
 * 提供企业级用户管理功能
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisCacheService redisCacheService;

    /**
     * 创建用户
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public UserDTO createUser(CreateUserRequest request) {
        log.info("Creating user: {}", request.getUsername());

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + request.getUsername());
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        // 加密密码
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        String salt = UUID.randomUUID().toString();

        // 构建用户实体
        User user = User.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .salt(salt)
                .fullname(request.getFullname())
                .nickname(request.getNickname())
                .email(request.getEmail())
                .phone(request.getPhone())
                .countryCode(request.getCountryCode())
                .tenantId(request.getTenantId())
                .departmentId(request.getDepartmentId())
                .positionId(request.getPositionId())
                .employeeId(request.getEmployeeId())
                .jobTitle(request.getJobTitle())
                .workLocation(request.getWorkLocation())
                .managerId(request.getManagerId())
                .status(UserStatus.ACTIVE)
                .mustChangePassword(request.getRequirePasswordChange())
                .passwordChangedAt(LocalDateTime.now())
                .passwordExpiresAt(LocalDateTime.now().plusDays(90))
                .build();

        // 保存用户
        User savedUser = userRepository.save(user);

        // 分配角色
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            assignRoles(savedUser.getId(), request.getRoleIds());
        }

        // 创建用户档案
        createUserProfile(savedUser.getId());

        // 发送欢迎邮件（异步）
        if (request.getSendWelcomeEmail()) {
            sendWelcomeEmail(savedUser);
        }

        log.info("User created successfully: {}", savedUser.getId());
        return userMapper.toDTO(savedUser);
    }

    /**
     * 更新用户
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

        // 更新基本信息
        if (request.getFullname() != null) {
            user.setFullname(request.getFullname());
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            if (!user.getEmail().equals(request.getEmail()) && 
                userRepository.existsByEmail(request.getEmail())) {
                throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getDepartmentId() != null) {
            user.setDepartmentId(request.getDepartmentId());
        }
        if (request.getPositionId() != null) {
            user.setPositionId(request.getPositionId());
        }
        if (request.getEmployeeId() != null) {
            user.setEmployeeId(request.getEmployeeId());
        }
        if (request.getJobTitle() != null) {
            user.setJobTitle(request.getJobTitle());
        }
        if (request.getWorkLocation() != null) {
            user.setWorkLocation(request.getWorkLocation());
        }
        if (request.getManagerId() != null) {
            user.setManagerId(request.getManagerId());
        }
        if (request.getMustChangePassword() != null) {
            user.setMustChangePassword(request.getMustChangePassword());
        }
        if (request.getMfaEnabled() != null) {
            user.setMfaEnabled(request.getMfaEnabled());
        }

        // 更新角色
        if (request.getRoleIds() != null) {
            assignRoles(id, request.getRoleIds());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", id);

        return userMapper.toDTO(updatedUser);
    }

    /**
     * 根据ID获取用户
     */
    @Cacheable(value = "users", key = "#id")
    public UserDTO getUserById(Long id) {
        log.debug("Getting user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
        return userMapper.toDTO(user);
    }

    /**
     * 根据用户名获取用户
     */
    @Cacheable(value = "users", key = "'username:' + #username")
    public UserDTO getUserByUsername(String username) {
        log.debug("Getting user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        return userMapper.toDTO(user);
    }

    /**
     * 根据邮箱获取用户
     */
    @Cacheable(value = "users", key = "'email:' + #email")
    public UserDTO getUserByEmail(String email) {
        log.debug("Getting user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
        return userMapper.toDTO(user);
    }

    /**
     * 分页查询用户
     */
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        log.debug("Getting all users with pagination: {}", pageable);
        Page<User> users = userRepository.findAll(pageable);
        return users.map(userMapper::toDTO);
    }

    /**
     * 搜索用户
     */
    public Page<UserDTO> searchUsers(UserSearchRequest request) {
        log.info("Searching users with request: {}", request);

        Specification<User> spec = buildSearchSpecification(request);
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy())
        );

        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(userMapper::toDTO);
    }

    /**
     * 删除用户（软删除）
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(Long id, String reason) {
        log.info("Deleting user: {} with reason: {}", id, reason);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setDeletionReason(reason);
        user.setStatus(UserStatus.DISABLED);

        userRepository.save(user);
        redisCacheService.evictUserCache(id);
    }

    /**
     * 激活用户
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public void activateUser(Long id) {
        log.info("Activating user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    /**
     * 停用用户
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public void disableUser(Long id) {
        log.info("Disabling user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

        user.setStatus(UserStatus.DISABLED);
        userRepository.save(user);
    }

    /**
     * 锁定用户
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public void lockUser(Long id, LocalDateTime lockUntil) {
        log.info("Locking user: {} until: {}", id, lockUntil);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

        user.setLockedUntil(lockUntil);
        user.setStatus(UserStatus.LOCKED);
        userRepository.save(user);
    }

    /**
     * 解锁用户
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public void unlockUser(Long id) {
        log.info("Unlocking user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        if (user.getStatus() == UserStatus.LOCKED) {
            user.setStatus(UserStatus.ACTIVE);
        }
        userRepository.save(user);
    }

    /**
     * 修改密码
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public void changePassword(Long id, String oldPassword, String newPassword) {
        log.info("Changing password for user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidPasswordException("Old password is incorrect");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setPasswordExpiresAt(LocalDateTime.now().plusDays(90));
        user.setMustChangePassword(false);

        userRepository.save(user);
    }

    /**
     * 重置密码
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public void resetPassword(Long id, String newPassword) {
        log.info("Resetting password for user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setPasswordExpiresAt(LocalDateTime.now().plusDays(90));
        user.setMustChangePassword(true);
        user.setFailedLoginAttempts(0);

        userRepository.save(user);
    }

    /**
     * 分配角色
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users", allEntries = true)
    public void assignRoles(Long userId, List<Long> roleIds) {
        log.info("Assigning roles to user: {}, roles: {}", userId, roleIds);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        // 清除现有角色
        user.getUserRoles().clear();

        // 分配新角色
        for (int i = 0; i < roleIds.size(); i++) {
            Long roleId = roleIds.get(i);
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new RoleNotFoundException("Role not found: " + roleId));

            UserRole userRole = UserRole.builder()
                    .user(user)
                    .role(role)
                    .isPrimary(i == 0)
                    .assignedAt(LocalDateTime.now())
                    .build();

            user.getUserRoles().add(userRole);
        }

        userRepository.save(user);
    }

    /**
     * 获取用户权限
     */
    @Cacheable(value = "userPermissions", key = "#userId")
    public Set<String> getUserPermissions(Long userId) {
        log.debug("Getting permissions for user: {}", userId);

        List<Permission> permissions = permissionRepository.findByUserId(userId);
        return permissions.stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }

    /**
     * 更新登录信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateLoginInfo(Long userId, String ip) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ip);
        user.setLoginCount(user.getLoginCount() + 1);
        user.setFailedLoginAttempts(0);

        userRepository.save(user);
    }

    /**
     * 记录登录失败
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordFailedLogin(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

            // 达到最大失败次数，锁定账户
            if (user.getFailedLoginAttempts() >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
                user.setStatus(UserStatus.LOCKED);
                log.warn("User locked due to too many failed attempts: {}", username);
            }

            userRepository.save(user);
        }
    }

    /**
     * 构建搜索条件
     */
    private Specification<User> buildSearchSpecification(UserSearchRequest request) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // 关键词搜索
            if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
                String keyword = "%" + request.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullname")), keyword),
                        cb.like(cb.lower(root.get("username")), keyword),
                        cb.like(cb.lower(root.get("email")), keyword),
                        cb.like(cb.lower(root.get("phone")), keyword)
                ));
            }

            // 状态过滤
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            // 租户过滤
            if (request.getTenantId() != null) {
                predicates.add(cb.equal(root.get("tenantId"), request.getTenantId()));
            }

            // 部门过滤
            if (request.getDepartmentId() != null) {
                predicates.add(cb.equal(root.get("departmentId"), request.getDepartmentId()));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    /**
     * 创建用户档案
     */
    private void createUserProfile(Long userId) {
        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .timezone("Asia/Shanghai")
                .language("zh-CN")
                .build();
        userProfileRepository.save(profile);
    }

    /**
     * 发送欢迎邮件（异步）
     */
    private void sendWelcomeEmail(User user) {
        // TODO: 实现邮件发送逻辑
        log.info("Welcome email sent to: {}", user.getEmail());
    }

    // 以下方法为兼容性保留

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    public Page<User> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User toEntity(CreateUserRequest request) {
        return userMapper.toEntity(request);
    }

    public UserDTO toDTO(User user) {
        return userMapper.toDTO(user);
    }

    public void updateUserFromDTO(User user, UpdateUserRequest request) {
        userMapper.updateUserFromDto(request, user);
    }
}
