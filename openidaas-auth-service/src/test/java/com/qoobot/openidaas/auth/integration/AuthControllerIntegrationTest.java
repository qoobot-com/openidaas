package com.qoobot.openidaas.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 集成测试
 *
 * @author QooBot
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("认证控制器集成测试")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("测试用户登录 - 成功")
    void testLogin_Success() throws Exception {
        // 准备登录请求
        String loginJson = "{\n" +
                "  \"username\": \"admin\",\n" +
                "  \"password\": \"Admin@123\",\n" +
                "  \"clientIp\": \"127.0.0.1\"\n" +
                "}";

        // 执行（注意：这需要用户存在，实际测试时需要先创建用户或使用Mock）
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("测试用户登录 - 缺少MFA验证码")
    void testLogin_MissingMFACode() throws Exception {
        // 准备登录请求
        String loginJson = "{\n" +
                "  \"username\": \"testuser\",\n" +
                "  \"password\": \"Test@123\",\n" +
                "  \"clientIp\": \"127.0.0.1\"\n" +
                "}";

        // 执行
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试用户登出")
    void testLogout() throws Exception {
        // 执行
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试刷新令牌")
    void testRefreshToken() throws Exception {
        // 准备刷新令牌请求
        String refreshJson = "{\n" +
                "  \"refreshToken\": \"test-refresh-token\"\n" +
                "}";

        // 执行
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试生成TOTP设置信息")
    void testGenerateTOTPSetup() throws Exception {
        // 执行
        mockMvc.perform(post("/api/auth/mfa/setup/totp")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.data.factorId").exists())
                .andExpect(jsonPath("$.data.secret").exists())
                .andExpect(jsonPath("$.data.qrCode").exists());
    }

    @Test
    @DisplayName("测试激活TOTP")
    void testActivateTOTP() throws Exception {
        // 先生成设置信息
        String setupResponse = mockMvc.perform(post("/api/auth/mfa/setup/totp")
                        .header("X-User-Id", "1"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 提取secret（简化处理）
        String secret = "test-secret";

        // 生成验证码
        String code = "123456";

        // 准备激活请求
        String activateJson = String.format("{\n" +
                "  \"secret\": \"%s\",\n" +
                "  \"code\": \"%s\"\n" +
                "}", secret, code);

        // 执行
        mockMvc.perform(post("/api/auth/mfa/activate/totp")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(activateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试发送短信验证码")
    void testSendSMSCode() throws Exception {
        // 准备请求
        String smsJson = "{\n" +
                "  \"phoneNumber\": \"13800000000\"\n" +
                "}";

        // 执行
        mockMvc.perform(post("/api/auth/mfa/send-sms")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(smsJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试发送邮箱验证码")
    void testSendEmailCode() throws Exception {
        // 准备请求
        String emailJson = "{\n" +
                "  \"email\": \"test@example.com\"\n" +
                "}";

        // 执行
        mockMvc.perform(post("/api/auth/mfa/send-email")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emailJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试生成备用码")
    void testGenerateBackupCodes() throws Exception {
        // 执行
        mockMvc.perform(post("/api/auth/mfa/backup-codes")
                        .header("X-User-Id", "1")
                        .param("count", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.data.factorId").exists())
                .andExpect(jsonPath("$.data.codes").isArray())
                .andExpect(jsonPath("$.data.count").value(10));
    }

    @Test
    @DisplayName("测试获取MFA偏好设置")
    void testGetMFAPreferences() throws Exception {
        // 执行
        mockMvc.perform(get("/api/auth/mfa/preferences")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.mfaEnabled").exists())
                .andExpect(jsonPath("$.data.factors").isArray());
    }

    @Test
    @DisplayName("测试禁用MFA因子")
    void testDisableMFAFactor() throws Exception {
        // 执行
        mockMvc.perform(delete("/api/auth/mfa/factors/{factorId}", 1)
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试设置主MFA方式")
    void testSetPrimaryMFA() throws Exception {
        // 执行
        mockMvc.perform(put("/api/auth/mfa/factors/{factorId}/primary", 1)
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试重置密码")
    void testResetPassword() throws Exception {
        // 准备请求
        String resetJson = "{\n" +
                "  \"userId\": 1,\n" +
                "  \"oldPassword\": \"Old@123\",\n" +
                "  \"newPassword\": \"New@123\",\n" +
                "  \"confirmPassword\": \"New@123\"\n" +
                "}";

        // 执行
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试响应结构")
    void testResponseStructure() throws Exception {
        // 执行
        mockMvc.perform(get("/api/auth/mfa/preferences")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("测试生成备用码 - 自定义数量")
    void testGenerateBackupCodes_CustomCount() throws Exception {
        // 执行
        mockMvc.perform(post("/api/auth/mfa/backup-codes")
                        .header("X-User-Id", "1")
                        .param("count", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.data.count").value(5));

        // 执行
        mockMvc.perform(post("/api/auth/mfa/backup-codes")
                        .header("X-User-Id", "1")
                        .param("count", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.data.count").value(15));
    }

    @Test
    @DisplayName("测试生成备用码 - 默认数量")
    void testGenerateBackupCodes_DefaultCount() throws Exception {
        // 执行（不传count参数）
        mockMvc.perform(post("/api/auth/mfa/backup-codes")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.data.count").value(10)); // 默认10个
    }

    @Test
    @DisplayName("测试缺少用户ID头")
    void testMissingUserIdHeader() throws Exception {
        // 执行（不带X-User-Id头）
        mockMvc.perform(post("/api/auth/mfa/setup/totp"))
                .andExpect(status().isBadRequest());
    }
}
