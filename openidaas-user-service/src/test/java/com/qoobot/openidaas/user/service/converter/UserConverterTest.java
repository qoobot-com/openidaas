package com.qoobot.openidaas.user.service.converter;

import com.qoobot.openidaas.common.dto.user.UserCreateDTO;
import com.qoobot.openidaas.common.dto.user.UserUpdateDTO;
import com.qoobot.openidaas.common.util.PasswordUtil;
import com.qoobot.openidaas.common.vo.user.UserVO;
import com.qoobot.openidaas.user.entity.User;
import com.qoobot.openidaas.user.entity.UserProfile;
import com.qoobot.openidaas.user.mapper.UserDepartmentMapper;
import com.qoobot.openidaas.user.mapper.UserProfileMapper;
import com.qoobot.openidaas.user.mapper.UserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户转换器测试
 *
 * @author QooBot
 */
class UserConverterTest {

    private UserConverter userConverter;
    
    @Mock
    private UserProfileMapper userProfileMapper;
    
    @Mock
    private UserDepartmentMapper userDepartmentMapper;
    
    @Mock
    private UserRoleMapper userRoleMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userConverter = new UserConverter(userProfileMapper, userDepartmentMapper, userRoleMapper);
    }

    @Test
    void testToVO_Success() {
        // 准备
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setMobile("13800000001");
        user.setStatus(1);
        user.setFailedLoginAttempts(0);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp("192.168.1.1");
        user.setPasswordUpdatedAt(LocalDateTime.now());
        user.setPwdResetRequired(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        UserProfile profile = new UserProfile();
        profile.setUserId(1L);
        profile.setAvatarUrl("http://avatar.com/1.jpg");
        profile.setFullName("Test User");
        profile.setNickname("Test");
        profile.setGender(1);
        profile.setBirthDate(java.time.LocalDate.of(1990, 1, 1));
        profile.setEmployeeId("EMP001");
        profile.setHireDate(java.time.LocalDate.of(2020, 1, 1));

        // 执行
        UserVO result = userConverter.toVO(user);

        // 验证
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getMobile(), result.getMobile());
        assertEquals(user.getStatus(), result.getStatus());
        assertEquals(profile.getAvatarUrl(), result.getAvatar());
        assertEquals(profile.getFullName(), result.getRealName());
        assertEquals(profile.getNickname(), result.getNickname());
        assertEquals(profile.getGender(), result.getGender());
    }

    @Test
    void testToVO_WithoutProfile() {
        // 准备
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 执行
        UserVO result = userConverter.toVO(user);

        // 验证
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
        assertNull(result.getAvatar());
        assertNull(result.getRealName());
        assertNull(result.getNickname());
    }

    @Test
    void testToVO_WithAllFields() {
        // 准备
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setMobile("13800000001");
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 执行
        UserVO result = userConverter.toVO(user);

        // 验证
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getMobile(), result.getMobile());
        assertEquals(user.getStatus(), result.getStatus());
    }

    @Test
    void testToDetailVO_WithProfile() {
        // 准备
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setMobile("13800000001");
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        UserProfile profile = new UserProfile();
        profile.setUserId(1L);
        profile.setAvatarUrl("http://avatar.com/1.jpg");
        profile.setFullName("Test User");
        profile.setNickname("Test");
        profile.setGender(1);

        // 模拟mapper行为
        // when(userProfileMapper.selectById(1L)).thenReturn(profile);

        // 执行
        UserVO result = userConverter.toDetailVO(user);

        // 验证
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getMobile(), result.getMobile());
        assertEquals(user.getStatus(), result.getStatus());
    }

    @Test
    void testToDetailVO_WithoutProfile() {
        // 准备
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 模拟mapper行为返回null
        // when(userProfileMapper.selectById(1L)).thenReturn(null);

        // 执行
        UserVO result = userConverter.toDetailVO(user);

        // 验证
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
        assertNull(result.getAvatar());
        assertNull(result.getRealName());
        assertNull(result.getNickname());
    }

    @Test
    void testToDetailVO_Success() {
        // 准备
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setMobile("13800000001");
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        UserProfile profile = new UserProfile();
        profile.setUserId(1L);
        profile.setAvatarUrl("http://avatar.com/1.jpg");
        profile.setFullName("Test User");
        profile.setNickname("Test");
        profile.setGender(1);

        // 模拟mapper行为
        // when(userProfileMapper.selectById(1L)).thenReturn(profile);

        // 执行
        UserVO result = userConverter.toDetailVO(user);

        // 验证
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getMobile(), result.getMobile());
        assertEquals(user.getStatus(), result.getStatus());
    }
}
