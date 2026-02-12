package com.qoobot.openidaas.auth.service;

import com.qoobot.openidaas.auth.entity.UserMFAFactor;
import com.qoobot.openidaas.auth.mapper.UserMFAFactorMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MFA 服务单元测试
 *
 * @author QooBot
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("MFA服务测试")
class MFAServiceTest {

    @Autowired
    private MFAService mfaService;

    @Autowired
    private UserMFAFactorMapper mfaFactorMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Long testUserId = 1L;

    @BeforeEach
    void setUp() {
        // 清理Redis
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    @Test
    @DisplayName("测试生成TOTP设置信息")
    void testGenerateTOTPSetup() {
        // 执行
        Map<String, Object> result = mfaService.generateTOTPSetup(testUserId, "IDaaS-Test");

        // 验证
        assertNotNull(result);
        assertNotNull(result.get("factorId"));
        assertNotNull(result.get("secret"));
        assertNotNull(result.get("otpAuthURI"));
        assertNotNull(result.get("qrCode"));
        assertNotNull(result.get("remainingSeconds"));

        // 验证因子已创建
        UserMFAFactor factor = mfaFactorMapper.selectById(Long.valueOf(result.get("factorId").toString()));
        assertNotNull(factor);
        assertEquals(testUserId, factor.getUserId());
        assertNotNull(factor.getSecret());
    }

    @Test
    @DisplayName("测试验证并激活TOTP - 成功")
    void testVerifyAndActivateTOTP_Success() {
        // 先生成设置信息
        Map<String, Object> setup = mfaService.generateTOTPSetup(testUserId, "IDaaS-Test");
        String secret = (String) setup.get("secret");
        Long factorId = Long.valueOf(setup.get("factorId").toString());

        // 生成正确的验证码
        String correctCode = com.qoobot.openidaas.auth.util.TOTPUtil.generateTOTP(secret);

        // 执行
        boolean result = mfaService.verifyAndActivateTOTP(testUserId, secret, correctCode);

        // 验证
        assertTrue(result);

        // 验证因子已激活
        UserMFAFactor factor = mfaFactorMapper.selectById(factorId);
        assertNotNull(factor);
        assertEquals(1, factor.getIsPrimary()); // 第一个激活的应该设为主MFA
    }

    @Test
    @DisplayName("测试验证TOTP - 错误验证码")
    void testVerifyAndActivateTOTP_InvalidCode() {
        // 先生成设置信息
        Map<String, Object> setup = mfaService.generateTOTPSetup(testUserId, "IDaaS-Test");
        String secret = (String) setup.get("secret");

        // 使用错误的验证码
        String wrongCode = "000000";

        // 执行和验证
        assertThrows(com.qoobot.openidaas.common.exception.BusinessException.class, () -> {
            mfaService.verifyAndActivateTOTP(testUserId, secret, wrongCode);
        });
    }

    @Test
    @DisplayName("测试发送短信验证码")
    void testSendSMSCode() {
        // 执行
        boolean result = mfaService.sendSMSCode(testUserId, "13800000000");

        // 验证
        assertTrue(result);

        // 验证Redis中已存储验证码
        String redisKey = "mfa:sms:" + testUserId;
        String storedCode = redisTemplate.opsForValue().get(redisKey);
        assertNotNull(storedCode);
        assertEquals(6, storedCode.length());
    }

    @Test
    @DisplayName("测试验证短信验证码 - 成功")
    void testVerifySMSCode_Success() {
        // 先发送验证码
        mfaService.sendSMSCode(testUserId, "13800000000");
        String redisKey = "mfa:sms:" + testUserId;
        String correctCode = redisTemplate.opsForValue().get(redisKey);

        // 执行
        boolean result = mfaService.verifySMSCode(testUserId, correctCode);

        // 验证
        assertTrue(result);

        // 验证验证码已删除
        assertNull(redisTemplate.opsForValue().get(redisKey));
    }

    @Test
    @DisplayName("测试验证短信验证码 - 错误验证码")
    void testVerifySMSCode_InvalidCode() {
        // 先发送验证码
        mfaService.sendSMSCode(testUserId, "13800000000");

        // 执行和验证
        assertThrows(com.qoobot.openidaas.common.exception.BusinessException.class, () -> {
            mfaService.verifySMSCode(testUserId, "000000");
        });
    }

    @Test
    @DisplayName("测试验证短信验证码 - 过期")
    void testVerifySMSCode_Expired() {
        // 直接测试过期情况（没有先发送验证码）
        assertThrows(com.qoobot.openidaas.common.exception.BusinessException.class, () -> {
            mfaService.verifySMSCode(testUserId, "000000");
        });
    }

    @Test
    @DisplayName("测试发送邮箱验证码")
    void testSendEmailCode() {
        // 执行
        boolean result = mfaService.sendEmailCode(testUserId, "test@example.com");

        // 验证
        assertTrue(result);

        // 验证Redis中已存储验证码
        String redisKey = "mfa:email:" + testUserId;
        String storedCode = redisTemplate.opsForValue().get(redisKey);
        assertNotNull(storedCode);
        assertEquals(6, storedCode.length());
    }

    @Test
    @DisplayName("测试验证邮箱验证码 - 成功")
    void testVerifyEmailCode_Success() {
        // 先发送验证码
        mfaService.sendEmailCode(testUserId, "test@example.com");
        String redisKey = "mfa:email:" + testUserId;
        String correctCode = redisTemplate.opsForValue().get(redisKey);

        // 执行
        boolean result = mfaService.verifyEmailCode(testUserId, correctCode);

        // 验证
        assertTrue(result);
    }

    @Test
    @DisplayName("测试生成备用码")
    void testGenerateBackupCodes() {
        // 先激活TOTP
        Map<String, Object> setup = mfaService.generateTOTPSetup(testUserId, "IDaaS-Test");
        String secret = (String) setup.get("secret");
        String correctCode = com.qoobot.openidaas.auth.util.TOTPUtil.generateTOTP(secret);
        mfaService.verifyAndActivateTOTP(testUserId, secret, correctCode);

        // 生成备用码
        Map<String, Object> result = mfaService.generateBackupCodes(testUserId, 10);

        // 验证
        assertNotNull(result);
        assertNotNull(result.get("factorId"));
        assertNotNull(result.get("codes"));
        assertNotNull(result.get("count"));

        @SuppressWarnings("unchecked")
        java.util.List<String> codes = (java.util.List<String>) result.get("codes");
        assertEquals(10, codes.size());
    }

    @Test
    @DisplayName("测试生成备用码 - 用户未配置MFA")
    void testGenerateBackupCodes_NoMFAConfigured() {
        // 执行和验证
        assertThrows(com.qoobot.openidaas.common.exception.BusinessException.class, () -> {
            mfaService.generateBackupCodes(testUserId, 10);
        });
    }

    @Test
    @DisplayName("测试获取用户MFA偏好设置")
    void testGetUserMFAPreferences() {
        // 先配置MFA
        Map<String, Object> setup = mfaService.generateTOTPSetup(testUserId, "IDaaS-Test");
        String secret = (String) setup.get("secret");
        String correctCode = com.qoobot.openidaas.auth.util.TOTPUtil.generateTOTP(secret);
        mfaService.verifyAndActivateTOTP(testUserId, secret, correctCode);

        // 执行
        Map<String, Object> result = mfaService.getUserMFAPreferences(testUserId);

        // 验证
        assertNotNull(result);
        assertEquals(testUserId, result.get("userId"));
        assertTrue((Boolean) result.get("mfaEnabled"));

        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> factors = (java.util.List<Map<String, Object>>) result.get("factors");
        assertFalse(factors.isEmpty());
    }

    @Test
    @DisplayName("测试获取用户MFA偏好设置 - 未配置MFA")
    void testGetUserMFAPreferences_NoMFA() {
        // 执行
        Map<String, Object> result = mfaService.getUserMFAPreferences(testUserId);

        // 验证
        assertNotNull(result);
        assertFalse((Boolean) result.get("mfaEnabled"));

        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> factors = (java.util.List<Map<String, Object>>) result.get("factors");
        assertTrue(factors.isEmpty());
    }

    @Test
    @DisplayName("测试检查是否启用MFA")
    void testIsMFAEnabled() {
        // 未配置MFA
        assertFalse(mfaService.isMFAEnabled(testUserId));

        // 配置MFA
        Map<String, Object> setup = mfaService.generateTOTPSetup(testUserId, "IDaaS-Test");
        String secret = (String) setup.get("secret");
        String correctCode = com.qoobot.openidaas.auth.util.TOTPUtil.generateTOTP(secret);
        mfaService.verifyAndActivateTOTP(testUserId, secret, correctCode);

        // 已配置MFA
        assertTrue(mfaService.isMFAEnabled(testUserId));
    }

    @Test
    @DisplayName("测试禁用MFA因子")
    void testDisableMFAPreference() {
        // 先配置MFA
        Map<String, Object> setup = mfaService.generateTOTPSetup(testUserId, "IDaaS-Test");
        String secret = (String) setup.get("secret");
        String correctCode = com.qoobot.openidaas.auth.util.TOTPUtil.generateTOTP(secret);
        mfaService.verifyAndActivateTOTP(testUserId, secret, correctCode);
        Long factorId = Long.valueOf(setup.get("factorId").toString());

        // 执行
        boolean result = mfaService.disableMFAPreference(testUserId, factorId);

        // 验证
        assertTrue(result);

        // 验证因子已禁用
        UserMFAFactor factor = mfaFactorMapper.selectById(factorId);
        assertNotNull(factor);
        assertEquals(3, factor.getStatus()); // 3 = 已禁用
    }

    @Test
    @DisplayName("测试设置主MFA")
    void testSetPrimaryMFA() {
        // 配置第一个MFA
        Map<String, Object> setup1 = mfaService.generateTOTPSetup(testUserId, "IDaaS-Test");
        String secret1 = (String) setup1.get("secret");
        String code1 = com.qoobot.openidaas.auth.util.TOTPUtil.generateTOTP(secret1);
        mfaService.verifyAndActivateTOTP(testUserId, secret1, code1);

        // 配置第二个MFA（模拟）
        UserMFAFactor factor2 = new UserMFAFactor();
        factor2.setUserId(testUserId);
        factor2.setFactorType("SMS");
        factor2.setFactorName("短信验证");
        factor2.setSecret("test-secret");
        factor2.setIsPrimary(0);
        factor2.setStatus(1);
        mfaFactorMapper.insert(factor2);

        // 设置第二个为主MFA
        boolean result = mfaService.setPrimaryMFA(testUserId, factor2.getId());

        // 验证
        assertTrue(result);

        // 验证主MFA已更新
        UserMFAFactor primary1 = mfaFactorMapper.selectById(Long.valueOf(setup1.get("factorId").toString()));
        UserMFAFactor primary2 = mfaFactorMapper.selectById(factor2.getId());
        assertEquals(0, primary1.getIsPrimary());
        assertEquals(1, primary2.getIsPrimary());
    }

    @Test
    @DisplayName("测试多次生成TOTP设置")
    void testMultipleTOTPSetup() {
        // 第一次
        Map<String, Object> setup1 = mfaService.generateTOTPSetup(testUserId, "IDaaS-Test");
        String secret1 = (String) setup1.get("secret");

        // 第二次
        Map<String, Object> setup2 = mfaService.generateTOTPSetup(testUserId, "IDaaS-Test");
        String secret2 = (String) setup2.get("secret");

        // 验证两个密钥不同
        assertNotEquals(secret1, secret2);
    }

    @Test
    @DisplayName("测试验证TOTP - 时间窗口")
    void testVerifyTOTP_TimeWindow() {
        // 先激活TOTP
        Map<String, Object> setup = mfaService.generateTOTPSetup(testUserId, "IDaaS-Test");
        String secret = (String) setup.get("secret");
        String correctCode = com.qoobot.openidaas.auth.util.TOTPUtil.generateTOTP(secret);
        mfaService.verifyAndActivateTOTP(testUserId, secret, correctCode);

        // 生成当前时间窗口的验证码
        String currentCode = com.qoobot.openidaas.auth.util.TOTPUtil.generateTOTP(secret);

        // 验证当前时间窗口的验证码
        boolean result = mfaService.verifyTOTP(testUserId, currentCode);
        assertTrue(result);
    }
}
