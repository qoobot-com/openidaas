package com.qoobot.openidaas.core.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户服务测试
 *
 * @author QooBot
 */
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateUser_Success() {
        // 准备测试数据
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setUsername("testuser");
        userCreateDTO.setEmail("test@example.com");
        userCreateDTO.setPassword("password123");
        userCreateDTO.setTenantId(1L);

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(PasswordUtil.encode("password123"));
        user.setTenantId(1L);
        user.setStatus(UserStatusEnum.NORMAL);
        user.setCreatedAt(LocalDateTime.now());

        // Mock行为
        when(userRepository.save(any(User.class))).thenReturn(user);

        // 执行测试
        User result = userService.createUser(userCreateDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertTrue(PasswordUtil.matches("password123", result.getPassword()));
        assertEquals(UserStatusEnum.NORMAL, result.getStatus());
        
        // 验证调用
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testGetUserById_UserExists() {
        // 准备测试数据
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        // Mock行为
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 执行测试
        User result = userService.getUserById(userId);

        // 验证结果
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getUsername());
        
        // 验证调用
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testGetUserById_UserNotExists() {
        // 准备测试数据
        Long userId = 1L;

        // Mock行为
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // 执行测试
        User result = userService.getUserById(userId);

        // 验证结果
        assertNull(result);
        
        // 验证调用
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testGetUserByUsername_UserExists() {
        // 准备测试数据
        String username = "testuser";
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail("test@example.com");

        // Mock行为
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // 执行测试
        User result = userService.getUserByUsername(username);

        // 验证结果
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        
        // 验证调用
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void testDeleteUser_Success() {
        // 准备测试数据
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        // Mock行为
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 执行测试
        userService.deleteUser(userId);

        // 验证调用
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void testDeleteUser_UserNotExists() {
        // 准备测试数据
        Long userId = 1L;

        // Mock行为
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> userService.deleteUser(userId));
        
        // 验证调用
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void testEnableUser_Success() {
        // 准备测试数据
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEnabled(false);

        // Mock行为
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 执行测试
        userService.enableUser(userId);

        // 验证结果
        assertTrue(user.getEnabled());
        
        // 验证调用
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testDisableUser_Success() {
        // 准备测试数据
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEnabled(true);

        // Mock行为
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 执行测试
        userService.disableUser(userId);

        // 验证结果
        assertFalse(user.getEnabled());
        
        // 验证调用
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testResetUserPassword_Success() {
        // 准备测试数据
        Long userId = 1L;
        String newPassword = "newpassword123";
        User user = new User();
        user.setId(userId);
        user.setPassword("oldpassword");

        // Mock行为
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 执行测试
        userService.resetUserPassword(userId, newPassword);

        // 验证结果
        assertTrue(PasswordUtil.matches(newPassword, user.getPassword()));
        assertNotNull(user.getPwdResetTime());
        
        // 验证调用
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testLockUser_Success() {
        // 准备测试数据
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatusEnum.NORMAL);

        // Mock行为
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 执行测试
        userService.lockUser(userId);

        // 验证结果
        assertEquals(UserStatusEnum.LOCKED, user.getStatus());
        assertNotNull(user.getLockTime());
        
        // 验证调用
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testUnlockUser_Success() {
        // 准备测试数据
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatusEnum.LOCKED);
        user.setLockTime(LocalDateTime.now());

        // Mock行为
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 执行测试
        userService.unlockUser(userId);

        // 验证结果
        assertEquals(UserStatusEnum.NORMAL, user.getStatus());
        assertNull(user.getLockTime());
        assertEquals(0, user.getLoginFailCount());
        
        // 验证调用
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testIncrementLoginFailCount_Success() {
        // 准备测试数据
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setLoginFailCount(0);

        // Mock行为
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 执行测试
        userService.incrementLoginFailCount(userId);

        // 验证结果
        assertEquals(1, user.getLoginFailCount());
        
        // 验证调用
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testClearLoginFailCount_Success() {
        // 准备测试数据
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setLoginFailCount(5);

        // Mock行为
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 执行测试
        userService.clearLoginFailCount(userId);

        // 验证结果
        assertEquals(0, user.getLoginFailCount());
        
        // 验证调用
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testCanUserLogin_NormalUser_ReturnsTrue() {
        // 准备测试数据
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatusEnum.NORMAL);
        user.setEnabled(true);

        // Mock行为
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 执行测试
        boolean result = userService.canUserLogin(userId);

        // 验证结果
        assertTrue(result);
        
        // 验证调用
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testCanUserLogin_DisabledUser_ReturnsFalse() {
        // 准备测试数据
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatusEnum.NORMAL);
        user.setEnabled(false);

        // Mock行为
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 执行测试
        boolean result = userService.canUserLogin(userId);

        // 验证结果
        assertFalse(result);
        
        // 验证调用
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testCanUserLogin_LockedUser_ReturnsFalse() {
        // 准备测试数据
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatusEnum.LOCKED);
        user.setEnabled(true);

        // Mock行为
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 执行测试
        boolean result = userService.canUserLogin(userId);

        // 验证结果
        assertFalse(result);
        
        // 验证调用
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testRecordUserLogin_Success() {
        // 准备测试数据
        Long userId = 1L;
        String ip = "192.168.1.1";
        User user = new User();
        user.setId(userId);

        // Mock行为
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 执行测试
        userService.recordUserLogin(userId, ip);

        // 验证结果
        assertEquals(ip, user.getLastLoginIp());
        assertNotNull(user.getLastLoginTime());
        assertEquals(0, user.getLoginFailCount());
        
        // 验证调用
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }
}