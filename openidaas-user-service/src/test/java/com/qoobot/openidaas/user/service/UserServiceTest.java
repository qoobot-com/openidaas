package com.qoobot.openidaas.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openidaas.common.dto.user.UserCreateDTO;
import com.qoobot.openidaas.common.dto.user.UserPasswordDTO;
import com.qoobot.openidaas.common.dto.user.UserQueryDTO;
import com.qoobot.openidaas.common.dto.user.UserUpdateDTO;
import com.qoobot.openidaas.user.entity.User;
import com.qoobot.openidaas.user.mapper.UserMapper;
import com.qoobot.openidaas.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试
 *
 * @author QooBot
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setMobile("13800138000");
        testUser.setPasswordHash("encoded_password");
        testUser.setPasswordSalt("salt123");
        testUser.setStatus(1);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testSelectByUsername_Success() {
        // Given
        String username = "testuser";
        when(userMapper.selectByUsername(username)).thenReturn(testUser);

        // When
        User result = userService.selectByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userMapper, times(1)).selectByUsername(username);
    }

    @Test
    void testSelectByEmail_Success() {
        // Given
        String email = "test@example.com";
        when(userMapper.selectByEmail(email)).thenReturn(testUser);

        // When
        User result = userService.selectByEmail(email);

        // Then
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userMapper, times(1)).selectByEmail(email);
    }

    @Test
    void testSelectByMobile_Success() {
        // Given
        String mobile = "13800138000";
        when(userMapper.selectByMobile(mobile)).thenReturn(testUser);

        // When
        User result = userService.selectByMobile(mobile);

        // Then
        assertNotNull(result);
        assertEquals(mobile, result.getMobile());
        verify(userMapper, times(1)).selectByMobile(mobile);
    }

    @Test
    void testValidatePassword_Success() {
        // Given
        Long userId = 1L;
        String password = "Test@123456";
        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(passwordEncoder.matches(password, testUser.getPasswordHash())).thenReturn(true);

        // When
        boolean result = userService.validatePassword(userId, password);

        // Then
        assertTrue(result);
        verify(passwordEncoder, times(1)).matches(eq(password), eq(testUser.getPasswordHash()));
    }

    @Test
    void testValidatePassword_Failure() {
        // Given
        Long userId = 1L;
        String password = "WrongPassword";
        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(passwordEncoder.matches(password, testUser.getPasswordHash())).thenReturn(false);

        // When
        boolean result = userService.validatePassword(userId, password);

        // Then
        assertFalse(result);
    }

    @Test
    void testLockUser_Success() {
        // Given
        Long userId = 1L;
        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        boolean result = userService.lockUser(userId);

        // Then
        assertTrue(result);
        assertEquals(2, testUser.getStatus()); // 锁定状态
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    void testUnlockUser_Success() {
        // Given
        Long userId = 1L;
        testUser.setStatus(2); // 锁定状态
        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        boolean result = userService.unlockUser(userId);

        // Then
        assertTrue(result);
        assertEquals(1, testUser.getStatus()); // 正常状态
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    void testDisableUser_Success() {
        // Given
        Long userId = 1L;
        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        boolean result = userService.disableUser(userId);

        // Then
        assertTrue(result);
        assertEquals(3, testUser.getStatus()); // 停用状态
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    void testEnableUser_Success() {
        // Given
        Long userId = 1L;
        testUser.setStatus(3); // 停用状态
        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        boolean result = userService.enableUser(userId);

        // Then
        assertTrue(result);
        assertEquals(1, testUser.getStatus()); // 正常状态
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    void testUpdateLastLoginInfo_Success() {
        // Given
        Long userId = 1L;
        String loginIp = "192.168.1.100";
        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        userService.updateLastLoginInfo(userId, loginIp);

        // Then
        assertEquals(loginIp, testUser.getLastLoginIp());
        assertNotNull(testUser.getLastLoginTime());
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    void testIncrementFailedAttempts_Success() {
        // Given
        Long userId = 1L;
        testUser.setFailedLoginAttempts(2);
        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        userService.incrementFailedAttempts(userId);

        // Then
        assertEquals(3, testUser.getFailedLoginAttempts());
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    void testIncrementFailedAttempts_LockUserWhenExceedLimit() {
        // Given
        Long userId = 1L;
        testUser.setFailedLoginAttempts(5); // 假设5次失败即锁定
        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        userService.incrementFailedAttempts(userId);

        // Then
        assertEquals(2, testUser.getStatus()); // 锁定状态
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    void testSelectUserPage_Success() {
        // Given
        UserQueryDTO query = new UserQueryDTO();
        query.setPageNum(1);
        query.setPageSize(10);
        query.setUsername("test");

        Page<User> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testUser));
        page.setTotal(1);

        when(userMapper.selectUserPage(any(Page.class), eq(query))).thenReturn(page);

        // When
        IPage<User> result = userService.selectUserPage(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        verify(userMapper, times(1)).selectUserPage(any(Page.class), eq(query));
    }

    @Test
    void testSelectBatchByIds_Success() {
        // Given
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);
        when(userMapper.selectBatchIds(userIds)).thenReturn(Arrays.asList(testUser));

        // When
        List<User> result = userService.selectBatchByIds(userIds);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(userMapper, times(1)).selectBatchIds(userIds);
    }

    @Test
    void testAssignDepartments_Success() {
        // Given
        Long userId = 1L;
        List<Long> deptIds = Arrays.asList(1L, 2L);
        Long positionId = 1L;
        Boolean isPrimary = true;

        // Mock the behavior (actual implementation would insert user_department records)
        // When
        boolean result = userService.assignDepartments(userId, deptIds, positionId, isPrimary);

        // Then (adjust assertion based on actual implementation)
        assertTrue(result);
    }

    @Test
    void testAssignRoles_Success() {
        // Given
        Long userId = 1L;
        List<Long> roleIds = Arrays.asList(1L, 2L);
        Integer scopeType = 1;
        Long scopeId = null;

        // When
        boolean result = userService.assignRoles(userId, roleIds, scopeType, scopeId);

        // Then (adjust assertion based on actual implementation)
        assertTrue(result);
    }

    @Test
    void testRemoveRoles_Success() {
        // Given
        Long userId = 1L;
        List<Long> roleIds = Arrays.asList(1L, 2L);

        // When
        boolean result = userService.removeRoles(userId, roleIds);

        // Then (adjust assertion based on actual implementation)
        assertTrue(result);
    }
}
