package com.qoobot.openidaas.common.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 加密工具类
 *
 * @author QooBot
 */
@Slf4j
public class EncryptUtil {

    private static final String DEFAULT_KEY = "OpenIDaaS1234567"; // 16位密钥
    
    private static final AES AES_ENCRYPTOR = SecureUtil.aes(DEFAULT_KEY.getBytes(StandardCharsets.UTF_8));

    /**
     * MD5加密
     */
    public static String md5(String content) {
        return SecureUtil.md5(content);
    }

    /**
     * SHA256加密
     */
    public static String sha256(String content) {
        return SecureUtil.sha256(content);
    }

    /**
     * AES加密
     */
    public static String aesEncrypt(String content) {
        if (StrUtil.isBlank(content)) {
            return content;
        }
        try {
            byte[] encryptBytes = AES_ENCRYPTOR.encrypt(content);
            return Base64.getEncoder().encodeToString(encryptBytes);
        } catch (Exception e) {
            log.error("AES加密失败", e);
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * AES解密
     */
    public static String aesDecrypt(String encryptedContent) {
        if (StrUtil.isBlank(encryptedContent)) {
            return encryptedContent;
        }
        try {
            byte[] decryptBytes = Base64.getDecoder().decode(encryptedContent);
            return AES_ENCRYPTOR.decryptStr(decryptBytes);
        } catch (Exception e) {
            log.error("AES解密失败", e);
            throw new RuntimeException("解密失败", e);
        }
    }

    /**
     * 使用自定义密钥进行AES加密
     */
    public static String aesEncrypt(String content, String key) {
        if (StrUtil.isBlank(content) || StrUtil.isBlank(key)) {
            return content;
        }
        try {
            AES aes = SecureUtil.aes(key.getBytes(StandardCharsets.UTF_8));
            byte[] encryptBytes = aes.encrypt(content);
            return Base64.getEncoder().encodeToString(encryptBytes);
        } catch (Exception e) {
            log.error("AES加密失败", e);
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * 使用自定义密钥进行AES解密
     */
    public static String aesDecrypt(String encryptedContent, String key) {
        if (StrUtil.isBlank(encryptedContent) || StrUtil.isBlank(key)) {
            return encryptedContent;
        }
        try {
            AES aes = SecureUtil.aes(key.getBytes(StandardCharsets.UTF_8));
            byte[] decryptBytes = Base64.getDecoder().decode(encryptedContent);
            return aes.decryptStr(decryptBytes);
        } catch (Exception e) {
            log.error("AES解密失败", e);
            throw new RuntimeException("解密失败", e);
        }
    }

    /**
     * 生成随机盐值
     */
    public static String generateSalt(int length) {
        return cn.hutool.core.util.RandomUtil.randomString(length);
    }

    /**
     * 密码加盐哈希
     */
    public static String hashPassword(String password, String salt) {
        return sha256(password + salt);
    }

    /**
     * 验证密码
     */
    public static boolean verifyPassword(String password, String hashedPassword, String salt) {
        return hashPassword(password, salt).equals(hashedPassword);
    }

    /**
     * Base64编码
     */
    public static String base64Encode(String content) {
        if (StrUtil.isBlank(content)) {
            return content;
        }
        return Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64解码
     */
    public static String base64Decode(String encodedContent) {
        if (StrUtil.isBlank(encodedContent)) {
            return encodedContent;
        }
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedContent);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Base64解码失败", e);
            throw new RuntimeException("解码失败", e);
        }
    }
}