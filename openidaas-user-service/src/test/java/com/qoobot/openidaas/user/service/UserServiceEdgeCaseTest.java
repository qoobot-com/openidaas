package com.qoobot.openidaas.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qoobot.openidaas.common.dto.user.UserCreateDTO;

import com.qoobot.openidaas.common.dto.user.UserUpdateDTO;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.common.util.PasswordUtil;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户服务边界条件测试
 * 测试各种边界情况和异常场景
 *
 * @author QooBot
 */
@ExtendWith(MockitoExtension.class)
class UserServiceEdgeCaseTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

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
        testUser.setFailedLoginAttempts(0);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    // ==================== 空值和 null 测试 ====================

    @Test
    void testSelectByUsername_NullInput() {
        // 执行
        User result = userService.selectByUsername(null);

        // 验证
        assertNull(result);
    }

    @Test
    void testSelectByEmail_NullInput() {
        // 执行
        User result = userService.selectByEmail(null);

        // 验证
        assertNull(result);
    }

    @Test
    void testSelectByMobile_NullInput() {
        // 执行
        User result = userService.selectByMobile(null);

        // 验证
        assertNull(result);
    }

    @Test
    void testSelectByUsername_EmptyString() {
        // 准备
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // 执行
        User result = userService.selectByUsername("");

        // 验证
        assertNull(result);
    }

    @Test
    void testSelectBatchByIds_EmptyList() {
        // 执行
        List<User> result = userService.selectBatchByIds(Collections.emptyList());

        // 验证
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSelectBatchByIds_NullList() {
        // 执行
        List<User> result = userService.selectBatchByIds(null);

        // 验证
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== 字符串边界测试 ====================

    @Test
    void testCreateUser_UsernameTooShort() {
        try (MockedStatic<PasswordUtil> mocked = mockStatic(PasswordUtil.class)) {
            // 准备
            UserCreateDTO createDTO = new UserCreateDTO();
            createDTO.setUsername("ab");  // 少于3位
            createDTO.setPassword("Test@123");

            mocked.when(() -> PasswordUtil.checkPasswordStrength(anyString())).thenReturn(true);

            // 执行和验证
            assertThrows(BusinessException.class, () -> userService.createUser(createDTO));
            verify(userMapper, never()).insert(any(User.class));
        }
    }

    @Test
    void testCreateUser_UsernameTooLong() {
        try (MockedStatic<PasswordUtil> mocked = mockStatic(PasswordUtil.class)) {
            // 准备
            UserCreateDTO createDTO = new UserCreateDTO();
            createDTO.setUsername("a".repeat(30));  // 超过20位
            createDTO.setPassword("Test@123");

            when(userMapper.selectByUsername(anyString())).thenReturn(null);
            mocked.when(() -> PasswordUtil.checkPasswordStrength(anyString())).thenReturn(true);

            // 执行和验证
            assertThrows(BusinessException.class, () -> userService.createUser(createDTO));
            verify(userMapper, never()).insert(any(User.class));
        }
    }

    @Test
    void testCreateUser_InvalidEmailFormat() {
        try (MockedStatic<PasswordUtil> mocked = mockStatic(PasswordUtil.class)) {
            // 准备
            UserCreateDTO createDTO = new UserCreateDTO();
            createDTO.setUsername("testuser");
            createDTO.setEmail("invalid-email");
            createDTO.setPassword("Test@123");

            when(userMapper.selectByUsername(anyString())).thenReturn(null);
            mocked.when(() -> PasswordUtil.checkPasswordStrength(anyString())).thenReturn(true);

            // 执行和验证
            assertThrows(BusinessException.class, () -> userService.createUser(createDTO));
            verify(userMapper, never()).insert(any(User.class));
        }
    }

    @Test
    void testCreateUser_InvalidMobileFormat() {
        try (MockedStatic<PasswordUtil> mocked = mockStatic(PasswordUtil.class)) {
            // 准备
            UserCreateDTO createDTO = new UserCreateDTO();
            createDTO.setUsername("testuser");
            createDTO.setMobile("12345");  // 无效手机号
            createDTO.setPassword("Test@123");

            when(userMapper.selectByUsername(anyString())).thenReturn(null);
            mocked.when(() -> PasswordUtil.checkPasswordStrength(anyString())).thenReturn(true);

            // 执行和验证
            assertThrows(BusinessException.class, () -> userService.createUser(createDTO));
            verify(userMapper, never()).insert(any(User.class));
        }
    }

    // ==================== 密码强度测试 ====================

    @Test
    void testCreateUser_PasswordTooShort() {
        try (MockedStatic<PasswordUtil> mocked = mockStatic(PasswordUtil.class)) {
            // 准备
            UserCreateDTO createDTO = new UserCreateDTO();
            createDTO.setUsername("testuser");
            createDTO.setPassword("short");  // 少于8位

            when(userMapper.selectByUsername(anyString())).thenReturn(null);
            mocked.when(() -> PasswordUtil.checkPasswordStrength(anyString())).thenReturn(false);

            // 执行和验证
            assertThrows(BusinessException.class, () -> userService.createUser(createDTO));
            verify(userMapper, never()).insert(any(User.class));
        }
    }

    @Test
    void testCreateUser_PasswordWithoutUppercase() {
        try (MockedStatic<PasswordUtil> mocked = mockStatic(PasswordUtil.class)) {
            // 准备
            UserCreateDTO createDTO = new UserCreateDTO();
            createDTO.setUsername("testuser");
            createDTO.setPassword("lowercase123@");

            when(userMapper.selectByUsername(anyString())).thenReturn(null);
            mocked.when(() -> PasswordUtil.checkPasswordStrength(anyString())).thenReturn(false);

            // 执行和验证
            assertThrows(BusinessException.class, () -> userService.createUser(createDTO));
            verify(userMapper, never()).insert(any(User.class));
        }
    }

    @Test
    void testCreateUser_PasswordWithoutLowercase() {
        try (MockedStatic<PasswordUtil> mocked = mockStatic(PasswordUtil.class)) {
            // 准备
            UserCreateDTO createDTO = new UserCreateDTO();
            createDTO.setUsername("testuser");
            createDTO.setPassword("UPPERCASE123@");

            when(userMapper.selectByUsername(anyString())).thenReturn(null);
            mocked.when(() -> PasswordUtil.checkPasswordStrength(anyString())).thenReturn(false);

            // 执行和验证
            assertThrows(BusinessException.class, () -> userService.createUser(createDTO));
            verify(userMapper, never()).insert(any(User.class));
        }
    }

    @Test
    void testCreateUser_PasswordWithoutDigit() {
        try (MockedStatic<PasswordUtil> mocked = mockStatic(PasswordUtil.class)) {
            // 准备
            UserCreateDTO createDTO = new UserCreateDTO();
            createDTO.setUsername("testuser");
            createDTO.setPassword("Password@");

            when(userMapper.selectByUsername(anyString())).thenReturn(null);
            mocked.when(() -> PasswordUtil.checkPasswordStrength(anyString())).thenReturn(false);

            // 执行和验证
            assertThrows(BusinessException.class, () -> userService.createUser(createDTO));
            verify(userMapper, never()).insert(any(User.class));
        }
    }

    @Test
    void testCreateUser_PasswordWithoutSpecialChar() {
        try (MockedStatic<PasswordUtil> mocked = mockStatic(PasswordUtil.class)) {
            // 准备
            UserCreateDTO createDTO = new UserCreateDTO();
            createDTO.setUsername("testuser");
            createDTO.setPassword("Password123");

            when(userMapper.selectByUsername(anyString())).thenReturn(null);
            mocked.when(() -> PasswordUtil.checkPasswordStrength(anyString())).thenReturn(false);

            // 执行和验证
            assertThrows(BusinessException.class, () -> userService.createUser(createDTO));
            verify(userMapper, never()).insert(any(User.class));
        }
    }

    // ==================== 用户状态测试 ====================

    @Test
    void testLockUser_UserAlreadyLocked() {
        // 准备
        testUser.setStatus(2);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // 执行
        boolean result = userService.lockUser(1L);

        // 验证
        assertTrue(result);
        assertEquals(2, testUser.getStatus());
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    void testUnlockUser_UserAlreadyUnlocked() {
        // 准备
        testUser.setStatus(1);
        testUser.setFailedLoginAttempts(0);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // 执行
        boolean result = userService.unlockUser(1L);

        // 验证
        assertTrue(result);
        assertEquals(1, testUser.getStatus());
        assertEquals(0, testUser.getFailedLoginAttempts());
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    void testDisableUser_UserAlreadyDisabled() {
        // 准备
        testUser.setStatus(3);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // 执行
        boolean result = userService.disableUser(1L);

        // 验证
        assertTrue(result);
        assertEquals(3, testUser.getStatus());
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    void testEnableUser_UserAlreadyEnabled() {
        // 准备
        testUser.setStatus(1);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // 执行
        boolean result = userService.enableUser(1L);

        // 验证
        assertTrue(result);
        assertEquals(1, testUser.getStatus());
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    // ==================== 删除用户状态测试 ====================

    @Test
    void testDeleteUser_UserAlreadyDeleted() {
        // 准备
        testUser.setStatus(4);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // 执行
        boolean result = userService.deleteUser(1L);

        // 验证
        assertTrue(result);
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    void testDeleteUser_UserDisabled() {
        // 准备
        testUser.setStatus(3);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // 执行
        boolean result = userService.deleteUser(1L);

        // 验证
        assertTrue(result);
        assertEquals(4, testUser.getStatus());
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    // ==================== 登录失败次数测试 ====================

    @Test
    void testIncrementFailedAttempts_AtMaxLimit() {
        // 准备
        testUser.setFailedLoginAttempts(4);  // 接近上限
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // 执行
        userService.incrementFailedAttempts(1L);

        // 验证
        assertEquals(5, testUser.getFailedLoginAttempts());
    }

    @Test
    void testIncrementFailedAttempts_OverLimit() {
        // 准备
        testUser.setFailedLoginAttempts(10);  // 超过上限
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // 执行
        userService.incrementFailedAttempts(1L);

        // 验证
        assertEquals(11, testUser.getFailedLoginAttempts());
    }

    // ==================== 密码修改测试 ====================

    @Test
    void testChangePassword_SamePassword() {
        try (MockedStatic<PasswordUtil> mocked = mockStatic(PasswordUtil.class)) {
            // 准备
            Long userId = 1L;
            String samePassword = "Old@123";

            when(userMapper.selectById(userId)).thenReturn(testUser);
            mocked.when(() -> PasswordUtil.matches(samePassword, testUser.getPasswordHash())).thenReturn(true);
            mocked.when(() -> PasswordUtil.checkPasswordStrength(samePassword)).thenReturn(true);

            // 执行和验证 - 应该抛出异常或拒绝修改
            assertThrows(BusinessException.class, () ->
                userService.changePassword(userId, samePassword, samePassword));
        }
    }

    @Test
    void testChangePassword_NewPasswordTooWeak() {
        try (MockedStatic<PasswordUtil> mocked = mockStatic(PasswordUtil.class)) {
            // 准备
            Long userId = 1L;
            String oldPassword = "Old@123";
            String weakPassword = "weak";

            when(userMapper.selectById(userId)).thenReturn(testUser);
            mocked.when(() -> PasswordUtil.matches(oldPassword, testUser.getPasswordHash())).thenReturn(true);
            mocked.when(() -> PasswordUtil.checkPasswordStrength(weakPassword)).thenReturn(false);

            // 执行和验证
            assertThrows(BusinessException.class, () ->
                userService.changePassword(userId, oldPassword, weakPassword));
        }
    }

    // ==================== 部门和角色分配测试 ====================

    @Test
    void testAssignDepartments_EmptyDepartmentList() {
        // 准备
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // 执行
        boolean result = userService.assignDepartments(1L, Collections.emptyList(), null, true);

        // 验证
        assertTrue(result);
    }

    @Test
    void testAssignDepartments_NullDepartmentList() {
        // 准备
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // 执行
        boolean result = userService.assignDepartments(1L, null, null, true);

        // 验证
        assertTrue(result);
    }

    @Test
    void testAssignRoles_EmptyRoleList() {
        // 准备
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // 执行
        boolean result = userService.assignRoles(1L, Collections.emptyList(), 1, null);

        // 验证
        assertTrue(result);
    }

    @Test
    void testAssignRoles_NullRoleList() {
        // 准备
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // 执行
        boolean result = userService.assignRoles(1L, null, 1, null);

        // 验证
        assertTrue(result);
    }

    @Test
    void testRemoveRoles_EmptyRoleList() {
        // 准备
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // 执行
        boolean result = userService.removeRoles(1L, Collections.emptyList());

        // 验证
        assertTrue(result);
    }

    @Test
    void testRemoveRoles_NullRoleList() {
        // 准备
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // 执行
        boolean result = userService.removeRoles(1L, null);

        // 验证
        assertTrue(result);
    }

    // ==================== 扩展属性测试 ====================

    @Test
    void testSaveAttribute_NullAttrKey() {
        // 准备
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // 执行和验证
        assertThrows(BusinessException.class, () -> userService.saveAttribute(1L, null, "value"));
    }

    @Test
    void testSaveAttribute_EmptyAttrKey() {
        // 准备
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // 执行和验证
        assertThrows(BusinessException.class, () -> userService.saveAttribute(1L, "", "value"));
    }

    @Test
    void testDeleteAttribute_NullAttrKey() {
        // 准备
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // 执行和验证
        assertThrows(BusinessException.class, () -> userService.deleteAttribute(1L, null));
    }

    @Test
    void testDeleteAttribute_EmptyAttrKey() {
        // 准备
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // 执行和验证
        assertThrows(BusinessException.class, () -> userService.deleteAttribute(1L, ""));
    }

    // ==================== 用户权限测试 ====================

    @Test
    void testGetUserPermissions_NoPermissions() {
        // 准备
        Long userId = 1L;
        // when(userMapper.selectUserPermissions(userId)).thenReturn(Collections.emptyList());

        // 执行
        List<String> result = userService.getUserPermissions(userId);

        // 验证
        assertNotNull(result);
        assertTrue(result.isEmpty());
        // verify(userMapper, times(1)).selectUserPermissions(userId);
    }

    @Test
    void testGetUserPermissions_LargePermissionList() {
        // 准备
        Long userId = 1L;
        List<String> permissions = java.util.stream.IntStream.range(0, 100)
            .mapToObj(i -> "PERMISSION_" + i)
            .toList();
        // when(userMapper.selectUserPermissions(userId)).thenReturn(permissions);

        // 执行
        List<String> result = userService.getUserPermissions(userId);

        // 验证
        assertNotNull(result);
        assertEquals(100, result.size());
    }
}
