package com.qoobot.openidaas.auth.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TOTP工具类测试
 *
 * @author QooBot
 */
@DisplayName("TOTP工具类测试")
class TOTPUtilTest {

    @Test
    @DisplayName("测试生成密钥")
    void testGenerateSecret() {
        // 执行
        String secret1 = TOTPUtil.generateSecret();
        String secret2 = TOTPUtil.generateSecret();

        // 验证
        assertNotNull(secret1);
        assertNotNull(secret2);
        assertNotEquals(secret1, secret2); // 两次生成的密钥应该不同
        assertEquals(32, secret1.length()); // Base32编码的160位密钥
    }

    @Test
    @DisplayName("测试生成TOTP验证码")
    void testGenerateTOTP() {
        // 准备
        String secret = TOTPUtil.generateSecret();

        // 执行
        String code = TOTPUtil.generateTOTP(secret);

        // 验证
        assertNotNull(code);
        assertEquals(6, code.length()); // 6位数字
        assertTrue(code.matches("\\d{6}")); // 全是数字
    }

    @Test
    @DisplayName("测试验证TOTP - 正确验证码")
    void testVerify_ValidCode() {
        // 准备
        String secret = TOTPUtil.generateSecret();
        String code = TOTPUtil.generateTOTP(secret);

        // 执行
        boolean result = TOTPUtil.verify(secret, code);

        // 验证
        assertTrue(result);
    }

    @Test
    @DisplayName("测试验证TOTP - 错误验证码")
    void testVerify_InvalidCode() {
        // 准备
        String secret = TOTPUtil.generateSecret();

        // 执行
        boolean result = TOTPUtil.verify(secret, "000000");

        // 验证
        assertFalse(result);
    }

    @Test
    @DisplayName("测试验证TOTP - 空验证码")
    void testVerify_EmptyCode() {
        // 准备
        String secret = TOTPUtil.generateSecret();

        // 执行
        boolean result = TOTPUtil.verify(secret, "");

        // 验证
        assertFalse(result);
    }

    @Test
    @DisplayName("测试验证TOTP - Null验证码")
    void testVerify_NullCode() {
        // 准备
        String secret = TOTPUtil.generateSecret();

        // 执行
        boolean result = TOTPUtil.verify(secret, null);

        // 验证
        assertFalse(result);
    }

    @Test
    @DisplayName("测试验证TOTP - 空密钥")
    void testVerify_EmptySecret() {
        // 执行
        boolean result = TOTPUtil.verify("", "123456");

        // 验证
        assertFalse(result);
    }

    @Test
    @DisplayName("测试生成指定时间的TOTP")
    void testGenerateTOTP_WithTime() {
        // 准备
        String secret = TOTPUtil.generateSecret();
        Instant now = Instant.now();

        // 执行
        String code1 = TOTPUtil.generateTOTP(secret, now);
        String code2 = TOTPUtil.generateTOTP(secret, now.plusSeconds(30)); // 30秒后

        // 验证
        assertNotNull(code1);
        assertNotNull(code2);
        assertNotEquals(code1, code2); // 30秒后应该生成不同的验证码
    }

    @Test
    @DisplayName("测试验证指定时间的TOTP")
    void testVerify_WithTime() {
        // 准备
        String secret = TOTPUtil.generateSecret();
        Instant now = Instant.now();
        String code = TOTPUtil.generateTOTP(secret, now);

        // 执行
        boolean result = TOTPUtil.verify(secret, code, now);

        // 验证
        assertTrue(result);
    }

    @Test
    @DisplayName("测试生成OTP Auth URI")
    void testGenerateOtpAuthURI() {
        // 准备
        String secret = TOTPUtil.generateSecret();
        String account = "test@example.com";
        String issuer = "IDaaS";

        // 执行
        String uri = TOTPUtil.generateOtpAuthURI(secret, account, issuer);

        // 验证
        assertNotNull(uri);
        assertTrue(uri.startsWith("otpauth://totp/"));
        assertTrue(uri.contains("issuer=" + issuer));
        assertTrue(uri.contains("secret=" + secret));
    }

    @Test
    @DisplayName("测试获取剩余秒数")
    void testGetRemainingSeconds() {
        // 执行
        long remaining = TOTPUtil.getRemainingSeconds();

        // 验证
        assertTrue(remaining >= 0);
        assertTrue(remaining <= 30); // 30秒时间窗口
    }

    @Test
    @DisplayName("测试TOTP时间窗口")
    void testTOTPTimeWindow() {
        // 准备
        String secret = TOTPUtil.generateSecret();
        Instant now = Instant.now();

        // 生成当前时间的验证码
        String codeNow = TOTPUtil.generateTOTP(secret, now);

        // 验证当前时间窗口
        assertTrue(TOTPUtil.verify(secret, codeNow, now));

        // 生成29秒后的验证码（仍在同一时间窗口内）
        String codeBeforeWindowEnd = TOTPUtil.generateTOTP(secret, now.plusSeconds(29));
        assertEquals(codeNow, codeBeforeWindowEnd);

        // 生成31秒后的验证码（下一时间窗口）
        String codeAfterWindowStart = TOTPUtil.generateTOTP(secret, now.plusSeconds(31));
        assertNotEquals(codeNow, codeAfterWindowStart);
    }

    @Test
    @DisplayName("测试TOTP一致性 - 同一密钥同一时间")
    void testTOTPConsistency() {
        // 准备
        String secret = TOTPUtil.generateSecret();
        Instant now = Instant.now();

        // 多次生成同一时间的验证码
        String code1 = TOTPUtil.generateTOTP(secret, now);
        String code2 = TOTPUtil.generateTOTP(secret, now);
        String code3 = TOTPUtil.generateTOTP(secret, now);

        // 验证
        assertEquals(code1, code2);
        assertEquals(code2, code3);
    }

    @Test
    @DisplayName("测试TOTP唯一性 - 不同密钥")
    void testTOTPUniqueness_DifferentSecrets() {
        // 准备
        String secret1 = TOTPUtil.generateSecret();
        String secret2 = TOTPUtil.generateSecret();
        Instant now = Instant.now();

        // 执行
        String code1 = TOTPUtil.generateTOTP(secret1, now);
        String code2 = TOTPUtil.generateTOTP(secret2, now);

        // 验证
        assertNotEquals(code1, code2);
    }

    @Test
    @DisplayName("测试验证码格式")
    void testTOTPCodeFormat() {
        // 准备
        String secret = TOTPUtil.generateSecret();

        // 执行
        String code = TOTPUtil.generateTOTP(secret);

        // 验证格式
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"));

        // 验证不会以0开头太多（低概率但可能）
        // 6位数字应该是随机分布的
    }

    @Test
    @DisplayName("测试验证码长度")
    void testTOTPCodeLength() {
        // 准备
        String secret = TOTPUtil.generateSecret();

        // 执行
        String code = TOTPUtil.generateTOTP(secret);

        // 验证长度是6位
        assertEquals(6, code.length());
    }

    @Test
    @DisplayName("测试多次验证同一验证码")
    void testMultipleVerification() {
        // 准备
        String secret = TOTPUtil.generateSecret();
        String code = TOTPUtil.generateTOTP(secret);

        // 多次验证
        assertTrue(TOTPUtil.verify(secret, code));
        assertTrue(TOTPUtil.verify(secret, code));
        assertTrue(TOTPUtil.verify(secret, code));

        // 同一验证码应该可以重复验证（在同一时间窗口内）
    }

    @Test
    @DisplayName("测试时间窗口边界")
    void testTimeWindowBoundary() {
        // 准备
        String secret = TOTPUtil.generateSecret();
        Instant now = Instant.now();

        // 生成当前时间的验证码
        String codeNow = TOTPUtil.generateTOTP(secret, now);

        // 验证前一个时间窗口（应该失败）
        Instant previousTime = now.minus(30, ChronoUnit.SECONDS);
        String codePrevious = TOTPUtil.generateTOTP(secret, previousTime);
        assertFalse(TOTPUtil.verify(secret, codePrevious, now));

        // 验证当前时间窗口（应该成功）
        assertTrue(TOTPUtil.verify(secret, codeNow, now));

        // 验证下一个时间窗口（应该失败）
        Instant nextTime = now.plus(30, ChronoUnit.SECONDS);
        String codeNext = TOTPUtil.generateTOTP(secret, nextTime);
        assertFalse(TOTPUtil.verify(secret, codeNext, now));
    }
}
