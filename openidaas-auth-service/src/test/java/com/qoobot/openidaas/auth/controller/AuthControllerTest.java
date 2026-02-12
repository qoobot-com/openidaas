package com.qoobot.openidaas.auth.controller;

import com.qoobot.openidaas.auth.dto.UserInfoDTO;
import com.qoobot.openidaas.auth.service.MFAService;
import com.qoobot.openidaas.auth.util.JwtUtil;
import com.qoobot.openidaas.auth.vo.LoginVO;
import com.qoobot.openidaas.common.dto.auth.RefreshTokenDTO;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.common.vo.ResultVO;
import com.qoobot.openidaas.common.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 认证控制器单元测试
 *
 * @author QooBot
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private MFAService mfaService;

    @Mock
    private UserInfoDTO mockUser;

    @InjectMocks
    private AuthController authController;

    private final String testSecret = "test-jwt-secret-key-12345678901234567890";
    private final String testPassword = "Test@123456";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authController, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(authController, "accessTokenValidity", 3600L);
        ReflectionTestUtils.setField(authController, "refreshTokenValidity", 2592000L);
        ReflectionTestUtils.setField(authController, "mfaIssuer", "IDaaS");

        // Setup mock user
        mockUser = new UserInfoDTO();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@example.com");
        mockUser.setPasswordHash(PasswordUtil.hashPassword(testPassword));
        mockUser.setEnabled(true);
        mockUser.setAccountNonLocked(true);
        mockUser.setAccountNonExpired(true);
        mockUser.setCredentialsNonExpired(true);
    }

    @Test
    void testLogin_Success() {
        // Given
        try (MockedStatic<com.qoobot.openidaas.auth.client.UserClient> mockedUserClient = mockStatic(com.qoobot.openidaas.auth.client.UserClient.class)) {
            AuthController.LoginRequest request = new AuthController.LoginRequest();
            request.setUsername("testuser");
            request.setPassword(testPassword);
            request.setClientIp("192.168.1.100");

            mockedUserClient.when(() -> com.qoobot.openidaas.auth.client.UserClient.getUserByUsername("testuser"))
                .thenReturn(mockUser);

            when(mfaService.isMFAEnabled(1L)).thenReturn(false);
            when(jwtUtil.generateToken(eq("testuser"), anyMap(), eq(3600L))).thenReturn("access_token_123");
            when(jwtUtil.generateToken(eq("testuser:refresh"), anyMap(), eq(2592000L))).thenReturn("refresh_token_123");

            // When
            ResultVO<LoginVO> result = authController.login(request);

            // Then
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertNotNull(result.getData());
            assertEquals("access_token_123", result.getData().getAccessToken());
            assertEquals("refresh_token_123", result.getData().getRefreshToken());
            assertEquals(3600L, result.getData().getExpiresIn());
            assertEquals("Bearer", result.getData().getTokenType());
            assertNotNull(result.getData().getUserInfo());

            mockedUserClient.verify(() -> com.qoobot.openidaas.auth.client.UserClient.getUserByUsername("testuser"), times(1));
        }
    }

    @Test
    void testLogin_InvalidPassword() {
        // Given
        try (MockedStatic<com.qoobot.openidaas.auth.client.UserClient> mockedUserClient = mockStatic(com.qoobot.openidaas.auth.client.UserClient.class)) {
            AuthController.LoginRequest request = new AuthController.LoginRequest();
            request.setUsername("testuser");
            request.setPassword("wrong_password");

            mockedUserClient.when(() -> com.qoobot.openidaas.auth.client.UserClient.getUserByUsername("testuser"))
                .thenReturn(mockUser);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                authController.login(request);
            });
            assertTrue(exception.getMessage().contains("用户名或密码错误"));
        }
    }

    @Test
    void testLogin_UserNotFound() {
        // Given
        try (MockedStatic<com.qoobot.openidaas.auth.client.UserClient> mockedUserClient = mockStatic(com.qoobot.openidaas.auth.client.UserClient.class)) {
            AuthController.LoginRequest request = new AuthController.LoginRequest();
            request.setUsername("nonexistent");
            request.setPassword(testPassword);

            mockedUserClient.when(() -> com.qoobot.openidaas.auth.client.UserClient.getUserByUsername("nonexistent"))
                .thenReturn(null);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                authController.login(request);
            });
            assertTrue(exception.getMessage().contains("用户名或密码错误"));
        }
    }

    @Test
    void testLogin_AccountDisabled() {
        // Given
        try (MockedStatic<com.qoobot.openidaas.auth.client.UserClient> mockedUserClient = mockStatic(com.qoobot.openidaas.auth.client.UserClient.class)) {
            AuthController.LoginRequest request = new AuthController.LoginRequest();
            request.setUsername("testuser");
            request.setPassword(testPassword);

            mockUser.setEnabled(false);
            mockedUserClient.when(() -> com.qoobot.openidaas.auth.client.UserClient.getUserByUsername("testuser"))
                .thenReturn(mockUser);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                authController.login(request);
            });
            assertTrue(exception.getMessage().contains("账户已被禁用"));
        }
    }

    @Test
    void testLogin_AccountLocked() {
        // Given
        try (MockedStatic<com.qoobot.openidaas.auth.client.UserClient> mockedUserClient = mockStatic(com.qoobot.openidaas.auth.client.UserClient.class)) {
            AuthController.LoginRequest request = new AuthController.LoginRequest();
            request.setUsername("testuser");
            request.setPassword(testPassword);

            mockUser.setAccountNonLocked(false);
            mockedUserClient.when(() -> com.qoobot.openidaas.auth.client.UserClient.getUserByUsername("testuser"))
                .thenReturn(mockUser);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                authController.login(request);
            });
            assertTrue(exception.getMessage().contains("账户已被锁定"));
        }
    }

    @Test
    void testLogin_WithMFA_Success() {
        // Given
        try (MockedStatic<com.qoobot.openidaas.auth.client.UserClient> mockedUserClient = mockStatic(com.qoobot.openidaas.auth.client.UserClient.class)) {
            AuthController.LoginRequest request = new AuthController.LoginRequest();
            request.setUsername("testuser");
            request.setPassword(testPassword);
            request.setMfaCode("123456");
            request.setClientIp("192.168.1.100");

            mockedUserClient.when(() -> com.qoobot.openidaas.auth.client.UserClient.getUserByUsername("testuser"))
                .thenReturn(mockUser);

            when(mfaService.isMFAEnabled(1L)).thenReturn(true);
            when(mfaService.verifyMFA(1L, "123456", "192.168.1.100")).thenReturn(true);
            when(jwtUtil.generateToken(eq("testuser"), anyMap(), eq(3600L))).thenReturn("access_token_123");
            when(jwtUtil.generateToken(eq("testuser:refresh"), anyMap(), eq(2592000L))).thenReturn("refresh_token_123");

            // When
            ResultVO<LoginVO> result = authController.login(request);

            // Then
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertNotNull(result.getData());
            assertEquals("access_token_123", result.getData().getAccessToken());

            verify(mfaService, times(1)).verifyMFA(1L, "123456", "192.168.1.100");
        }
    }

    @Test
    void testLogin_WithMFA_MissingCode() {
        // Given
        try (MockedStatic<com.qoobot.openidaas.auth.client.UserClient> mockedUserClient = mockStatic(com.qoobot.openidaas.auth.client.UserClient.class)) {
            AuthController.LoginRequest request = new AuthController.LoginRequest();
            request.setUsername("testuser");
            request.setPassword(testPassword);
            // 未设置MFA验证码

            mockedUserClient.when(() -> com.qoobot.openidaas.auth.client.UserClient.getUserByUsername("testuser"))
                .thenReturn(mockUser);

            when(mfaService.isMFAEnabled(1L)).thenReturn(true);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                authController.login(request);
            });
            assertTrue(exception.getMessage().contains("请输入MFA验证码"));
        }
    }

    @Test
    void testLogin_WithMFA_InvalidCode() {
        // Given
        try (MockedStatic<com.qoobot.openidaas.auth.client.UserClient> mockedUserClient = mockStatic(com.qoobot.openidaas.auth.client.UserClient.class)) {
            AuthController.LoginRequest request = new AuthController.LoginRequest();
            request.setUsername("testuser");
            request.setPassword(testPassword);
            request.setMfaCode("wrong_code");
            request.setClientIp("192.168.1.100");

            mockedUserClient.when(() -> com.qoobot.openidaas.auth.client.UserClient.getUserClient.getUserByUsername("testuser"))
                .thenReturn(mockUser);

            when(mfaService.isMFAEnabled(1L)).thenReturn(true);
            when(mfaService.verifyMFA(1L, "wrong_code", "192.168.1.100")).thenReturn(false);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                authController.login(request);
            });
            assertTrue(exception.getMessage().contains("MFA验证码错误"));
        }
    }

    @Test
    void testLogout_Success() {
        // Given
        String authorization = "Bearer access_token_123";

        // When
        ResultVO<Void> result = authController.logout(authorization);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    void testRefreshToken_Success() {
        // Given
        try (MockedStatic<com.qoobot.openidaas.auth.client.UserClient> mockedUserClient = mockStatic(com.qoobot.openidaas.auth.client.UserClient.class)) {
            RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO();
            refreshTokenDTO.setRefreshToken("refresh_token_123");

            when(jwtUtil.extractUsername("refresh_token_123")).thenReturn("testuser");
            when(jwtUtil.validateToken("refresh_token_123", "testuser")).thenReturn(true);

            mockedUserClient.when(() -> com.qoobot.openidaas.auth.client.UserClient.getUserByUsername("testuser"))
                .thenReturn(mockUser);

            when(jwtUtil.generateToken(eq("testuser"), anyMap(), eq(3600L))).thenReturn("new_access_token");

            // When
            ResultVO<Map<String, Object>> result = authController.refreshToken(refreshTokenDTO);

            // Then
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertNotNull(result.getData());
            assertEquals("new_access_token", result.getData().get("accessToken"));
            assertEquals(3600L, result.getData().get("expiresIn"));
        }
    }

    @Test
    void testRefreshToken_InvalidToken() {
        // Given
        RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO();
        refreshTokenDTO.setRefreshToken("invalid_token");

        when(jwtUtil.extractUsername("invalid_token")).thenReturn("testuser");
        when(jwtUtil.validateToken("invalid_token", "testuser")).thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authController.refreshToken(refreshTokenDTO);
        });
        assertTrue(exception.getMessage().contains("刷新令牌无效或已过期"));
    }

    @Test
    void testGenerateTOTPSetup_Success() {
        // Given
        Long userId = 1L;
        Map<String, Object> expected = new HashMap<>();
        expected.put("secret", "JBSWY3DPEHPK3PXP");
        expected.put("qrCode", "otpauth://totp/IDaaS:testuser?secret=JBSWY3DPEHPK3PXP&issuer=IDaaS");

        when(mfaService.generateTOTPSetup(userId, "IDaaS")).thenReturn(expected);

        // When
        ResultVO<Map<String, Object>> result = authController.generateTOTPSetup(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("JBSWY3DPEHPK3PXP", result.getData().get("secret"));

        verify(mfaService, times(1)).generateTOTPSetup(userId, "IDaaS");
    }

    @Test
    void testActivateTOTP_Success() {
        // Given
        Long userId = 1L;
        AuthController.ActivateTOTPRequest request = new AuthController.ActivateTOTPRequest();
        request.setSecret("JBSWY3DPEHPK3PXP");
        request.setCode("123456");

        when(mfaService.verifyAndActivateTOTP(userId, "JBSWY3DPEHPK3PXP", "123456")).thenReturn(true);

        // When
        ResultVO<Void> result = authController.activateTOTP(userId, request);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());

        verify(mfaService, times(1)).verifyAndActivateTOTP(userId, "JBSWY3DPEHPK3PXP", "123456");
    }

    @Test
    void testActivateTOTP_Failure() {
        // Given
        Long userId = 1L;
        AuthController.ActivateTOTPRequest request = new AuthController.ActivateTOTPRequest();
        request.setSecret("JBSWY3DPEHPK3PXP");
        request.setCode("wrong_code");

        when(mfaService.verifyAndActivateTOTP(userId, "JBSWY3DPEHPK3PXP", "wrong_code")).thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authController.activateTOTP(userId, request);
        });
        assertTrue(exception.getMessage().contains("TOTP验证失败"));
    }
}
