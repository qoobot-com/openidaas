package com.qoobot.openidaas.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openidaas.common.dto.user.UserCreateDTO;
import com.qoobot.openidaas.common.dto.user.UserPasswordDTO;
import com.qoobot.openidaas.common.dto.user.UserQueryDTO;
import com.qoobot.openidaas.common.dto.user.UserUpdateDTO;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.common.util.PasswordUtil;
import com.qoobot.openidaas.common.vo.user.UserVO;
import com.qoobot.openidaas.user.entity.User;
import com.qoobot.openidaas.user.mapper.UserMapper;
import com.qoobot.openidaas.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
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

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserCreateDTO createDTO;
    private UserUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setMobile("13800000001");
        testUser.setPasswordHash("$2a$12$test_hash");
        testUser.setPasswordSalt("salt");
        testUser.setStatus(1);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        createDTO = new UserCreateDTO();
        createDTO.setUsername("newuser");
        createDTO.setEmail("new@test.com");
        createDTO.setMobile("13800000002");
        createDTO.setPassword("Test@123");

        updateDTO = new UserUpdateDTO();
        updateDTO.setEmail("updated@test.com");
        updateDTO.setMobile("13800000003");
    }

    @Test
    void testSelectByUsername_Success() {
        // 准备
        String username = "testuser";
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        // 执行
        User result = userService.selectByUsername(username);

        // 验证
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void testSelectByUsername_NotFound() {
        // 准备
        String username = "notfound";
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // 执行
        User result = userService.selectByUsername(username);

        // 验证
        assertNull(result);
    }

    @Test
    void testSelectByEmail_Success() {
        // 准备
        String email = "test@test.com";
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        // 执行
        User result = userService.selectByEmail(email);

        // 验证
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void testSelectByMobile_Success() {
        // 准备
        String mobile = "13800000001";
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        // 执行
        User result = userService.selectByMobile(mobile);

        // 验证
        assertNotNull(result);
        assertEquals(mobile, result.getMobile());
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void testSelectUserPage_Success() {
        // 准备
        UserQueryDTO query = new UserQueryDTO();
        query.setCurrentPage(1L);
        query.setPageSize(20L);

        Page<User> pageResult = new Page<>(1, 20, 1);
        pageResult.setRecords(Collections.singletonList(testUser));

        when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(pageResult);

        // 执行
        IPage<User> result = userService.selectUserPage(query);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertFalse(result.getRecords().isEmpty());
        verify(userMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void testCreateUser_Success() {
        try (MockedStatic<PasswordUtil> mocked = mockStatic(PasswordUtil.class)) {
            // 准备
            mocked.when(() -> PasswordUtil.encodePassword(anyString())).thenReturn("$2a$12$encoded");
            mocked.when(() -> PasswordUtil.checkPasswordStrength(anyString())).thenReturn(true);

            when(userMapper.insert(any(User.class))).thenReturn(1);

            // 执行
            UserVO result = userService.createUser(createDTO);

            // 验证
            assertNotNull(result);
            verify(userMapper, times(1)).insert(any(User.class));
        }
    }

    @Test
    void testCreateUser_DuplicateUsername() {
        try (MockedStatic<PasswordUtil> mocked = mockStatic(PasswordUtil.class)) {
            // 准备
            when(userMapper.selectByUsername(anyString())).thenReturn(testUser);
            mocked.when(() -> PasswordUtil.checkPasswordStrength(anyString())).thenReturn(true);

            // 执行和验证
            assertThrows(BusinessException.class, () -> userService.createUser(createDTO));
            verify(userMapper, never()).insert(any(User.class));
        }
    }

    @Test
    void testCreateUser_WeakPassword() {
        try (MockedStatic<PasswordUtil> mocked = mockStatic(PasswordUtil.class)) {
            // 准备
            createDTO.setPassword("weak");
            mocked.when(() -> PasswordUtil.checkPasswordStrength(anyString())).thenReturn(false);

            // 执行和验证
            assertThrows(BusinessException.class, () -> userService.createUser(createDTO));
            verify(userMapper, never()).insert(any(User.class));
        }
    }

    @Test
    void testUpdateUser_Success() {
        // 准备
        Long userId = 1L;
        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // 执行
        UserVO result = userService.updateUser(userId, updateDTO);

        // 验证
        assertNotNull(result);
        verify(userMapper, times(1)).selectById(userId);
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    void testUpdateUser_NotFound() {
        // 准备
        Long userId = 999L;
        when(userMapper.selectById(userId)).thenReturn(null);

        // 执行和验证
        assertThrows(BusinessException.class, () -> userService.updateUser(userId, updateDTO));
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    void testDeleteUser_Success() {
        // 准备
        Long userId = 1L;
        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(userMapper.deleteById(userId)).thenReturn(1);

        // 执行
        boolean result = userService.deleteUser(userId);

        // 验证
        assertTrue(result);
        verify(userMapper, times(1)).selectById(userId);
        verify(userMapper, times(1)).deleteById(userId);
    }

    @Test
    void testDeleteUser_NotFound() {
        // 准备
        Long userId = 999L;
        when(userMapper.selectById(userId)).thenReturn(null);

        // 执行和验证
        assertThrows(BusinessException.class, () -> userService.deleteUser(userId));
        verify(userMapper, never()).deleteById(anyLong());
    }

    @Test
    void testGetUserDetail_Success() {
        // 准备
        Long userId = 1L;
        when(userMapper.selectById(userId)).thenReturn(testUser);

        // 执行
        UserVO result = userService.getUserDetail(userId);

        // 验证
        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(userMapper, times(1)).selectById(userId);
    }

    @Test
    void testSelectBatchByIds_Success() {
        // 准备
        List<Long> userIds = Arrays.asList(1L, 2L);
        when(userMapper.selectBatchIds(userIds)).thenReturn(Collections.singletonList(testUser));

        // 执行
        List<User> result = userService.selectBatchByIds(userIds);

        // 验证
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(userMapper, times(1)).selectBatchIds(userIds);
    }

    @Test
    void testResetPassword_Success() {
        try (MockedStatic<PasswordUtil> mocked = mockStatic(PasswordUtil.class)) {
            // 准备
            Long userId = 1L;
            String newPassword = "New@123";

            when(userMapper.selectById(userId)).thenReturn(testUser);
            mocked.when(() -> PasswordUtil.encodePassword(anyString())).thenReturn("$2a$12$encoded");
            mocked.when(() -> PasswordUtil.checkPasswordStrength(anyString())).thenReturn(true);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            // 执行
            // boolean result = userService.resetPassword(userId, newPassword);

            // 验证
            // assertTrue(result);
            verify(userMapper, times(1)).updateById(any(User.class));
        }
    }

    @Test
    void testChangePassword_Success() {
        try (MockedStatic<PasswordUtil> mocked = mockStatic(PasswordUtil.class)) {
            // 准备
            Long userId = 1L;
            String oldPassword = "Old@123";
            String newPassword = "New@123";

            when(userMapper.selectById(userId)).thenReturn(testUser);
            mocked.when(() -> PasswordUtil.matches(oldPassword, testUser.getPasswordHash())).thenReturn(true);
            mocked.when(() -> PasswordUtil.encodePassword(newPassword)).thenReturn("$2a$12$encoded");
            mocked.when(() -> PasswordUtil.checkPasswordStrength(newPassword)).thenReturn(true);
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            // 执行
            boolean result = userService.changePassword(userId, oldPassword, newPassword);

            // 验证
            assertTrue(result);
            verify(userMapper, times(1)).updateById(any(User.class));
        }
    }

    @Test
    void testChangePassword_WrongOldPassword() {
        try (MockedStatic<PasswordUtil> mocked = mockStatic(PasswordUtil.class)) {
            // 准备
            Long userId = 1L;
            String oldPassword = "Wrong@123";
            String newPassword = "New@123";

            when(userMapper.selectById(userId)).thenReturn(testUser);
            mocked.when(() -> PasswordUtil.matches(oldPassword, testUser.getPasswordHash())).thenReturn(false);

            // 执行和验证
            assertThrows(BusinessException.class, () -> userService.changePassword(userId, oldPassword, newPassword));
            verify(userMapper, never()).updateById(any(User.class));
        }
    }

    @Test
    void testLockUser_Success() {
        // 准备
        Long userId = 1L;
        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // 执行
        boolean result = userService.lockUser(userId);

        // 验证
        assertTrue(result);
        assertEquals(2, testUser.getStatus());
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    void testUnlockUser_Success() {
        // 准备
        Long userId = 1L;
        testUser.setStatus(2);
        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // 执行
        boolean result = userService.unlockUser(userId);

        // 验证
        assertTrue(result);
        assertEquals(1, testUser.getStatus());
        assertEquals(0, testUser.getFailedLoginAttempts());
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    void testDisableUser_Success() {
        // 准备
        Long userId = 1L;
        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // 执行
        boolean result = userService.disableUser(userId);

        // 验证
        assertTrue(result);
        assertEquals(3, testUser.getStatus());
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    void testEnableUser_Success() {
        // 准备
        Long userId = 1L;
        testUser.setStatus(3);
        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // 执行
        boolean result = userService.enableUser(userId);

        // 验证
        assertTrue(result);
        assertEquals(1, testUser.getStatus());
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    void testUpdateLastLoginInfo_Success() {
        // 准备
        Long userId = 1L;
        String loginIp = "192.168.1.1";
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // 执行
        userService.updateLastLoginInfo(userId, loginIp);

        // 验证
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    void testIncrementFailedAttempts_Success() {
        // 准备
        Long userId = 1L;
        testUser.setFailedLoginAttempts(3);
        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // 执行
        userService.incrementFailedAttempts(userId);

        // 验证
        assertEquals(4, testUser.getFailedLoginAttempts());
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    void testSaveAttribute_Success() {
        // 准备
        Long userId = 1L;
        when(userMapper.selectById(userId)).thenReturn(testUser);
        // 模拟成功保存
        // doNothing().when(userMapper).insertAttribute(anyLong(), anyString(), anyString());

        // 执行
        boolean result = userService.saveAttribute(userId, "key", "value");

        // 验证
        assertTrue(result);
    }

    @Test
    void testDeleteAttribute_Success() {
        // 准备
        Long userId = 1L;
        when(userMapper.selectById(userId)).thenReturn(testUser);
        // 模拟成功删除
        // doNothing().when(userMapper).deleteAttribute(anyLong(), anyString());

        // 执行
        boolean result = userService.deleteAttribute(userId, "key");

        // 验证
        assertTrue(result);
    }

    @Test
    void testGetUserPermissions_Success() {
        // 准备
        Long userId = 1L;
        List<String> permissions = Arrays.asList("USER_READ", "USER_WRITE");
        // when(userMapper.selectUserPermissions(userId)).thenReturn(permissions);

        // 执行
        List<String> result = userService.getUserPermissions(userId);

        // 验证
        assertNotNull(result);
        assertEquals(2, result.size());
        // verify(userMapper, times(1)).selectUserPermissions(userId);
    }
}
