package com.qoobot.openidaas.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 加密工具类测试
 *
 * @author QooBot
 */
class EncryptUtilTest {

    @Test
    void testMd5() {
        // 测试MD5加密
        String content = "hello world";
        String encrypted = EncryptUtil.md5(content);
        assertNotNull(encrypted);
        assertEquals(32, encrypted.length()); // MD5是32位
        assertEquals(EncryptUtil.md5(content), encrypted); // 相同内容加密结果应该一致
    }

    @Test
    void testSha256() {
        // 测试SHA256加密
        String content = "hello world";
        String encrypted = EncryptUtil.sha256(content);
        assertNotNull(encrypted);
        assertEquals(64, encrypted.length()); // SHA256是64位
        assertEquals(EncryptUtil.sha256(content), encrypted); // 相同内容加密结果应该一致
    }

    @Test
    void testAesEncryptAndDecrypt() {
        // 测试AES加解密
        String content = "这是一条测试消息";
        String encrypted = EncryptUtil.aesEncrypt(content);
        assertNotNull(encrypted);
        assertNotEquals(content, encrypted);
        
        String decrypted = EncryptUtil.aesDecrypt(encrypted);
        assertEquals(content, decrypted);
    }

    @Test
    void testCustomKeyAes() {
        // 测试自定义密钥的AES加解密
        String content = "测试自定义密钥";
        String key = "MyCustomKey12345"; // 16位密钥
        
        String encrypted = EncryptUtil.aesEncrypt(content, key);
        assertNotNull(encrypted);
        assertNotEquals(content, encrypted);
        
        String decrypted = EncryptUtil.aesDecrypt(encrypted, key);
        assertEquals(content, decrypted);
    }

    @Test
    void testGenerateSalt() {
        // 测试生成盐值
        String salt1 = EncryptUtil.generateSalt(16);
        String salt2 = EncryptUtil.generateSalt(16);
        assertNotNull(salt1);
        assertNotNull(salt2);
        assertEquals(16, salt1.length());
        assertEquals(16, salt2.length());
        assertNotEquals(salt1, salt2); // 两次生成应该不同
    }

    @Test
    void testPasswordHashAndVerify() {
        // 测试密码哈希和验证
        String password = "mypassword123";
        String salt = EncryptUtil.generateSalt(16);
        
        String hashedPassword = EncryptUtil.hashPassword(password, salt);
        assertNotNull(hashedPassword);
        assertNotEquals(password, hashedPassword);
        
        // 验证正确密码
        assertTrue(EncryptUtil.verifyPassword(password, hashedPassword, salt));
        
        // 验证错误密码
        assertFalse(EncryptUtil.verifyPassword("wrongpassword", hashedPassword, salt));
    }

    @Test
    void testBase64EncodeAndDecode() {
        // 测试Base64编解码
        String content = "Hello World! 你好世界！";
        String encoded = EncryptUtil.base64Encode(content);
        assertNotNull(encoded);
        assertNotEquals(content, encoded);
        
        String decoded = EncryptUtil.base64Decode(encoded);
        assertEquals(content, decoded);
    }

    @Test
    void testEmptyStringHandling() {
        // 测试空字符串处理
        assertNull(EncryptUtil.aesEncrypt(null));
        assertEquals("", EncryptUtil.aesEncrypt(""));
        assertNull(EncryptUtil.aesDecrypt(null));
        assertEquals("", EncryptUtil.aesDecrypt(""));
        
        assertNull(EncryptUtil.base64Encode(null));
        assertEquals("", EncryptUtil.base64Encode(""));
        assertNull(EncryptUtil.base64Decode(null));
        assertEquals("", EncryptUtil.base64Decode(""));
    }

    @Test
    void testInvalidBase64Decode() {
        // 测试无效Base64解码
        assertThrows(RuntimeException.class, () -> {
            EncryptUtil.base64Decode("invalid_base64_string!!!");
        });
    }
}