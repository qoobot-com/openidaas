package com.qoobot.openidaas.common.util;

import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;

/**
 * 字段加密工具类
 * 用于JPA实体字段的自动加密/解密
 *
 * 使用方式:
 * @Convert(converter = FieldEncryptionUtil.EncryptConverter.class)
 * private String sensitiveData;
 *
 * @author QooBot
 */
@Component
public class FieldEncryptionUtil {

    @Autowired
    private PBEStringEncryptor encryptor;

    /**
     * JPA属性转换器 - 加密
     */
    @jakarta.persistence.Converter
    public static class EncryptConverter implements AttributeConverter<String, String> {

        private static FieldEncryptionUtil instance;

        @Autowired
        public void setInstance(FieldEncryptionUtil util) {
            instance = util;
        }

        @Override
        public String convertToDatabaseColumn(String attribute) {
            if (attribute == null || attribute.isEmpty()) {
                return attribute;
            }
            return instance.encrypt(attribute);
        }

        @Override
        public String convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.isEmpty()) {
                return dbData;
            }
            return instance.decrypt(dbData);
        }
    }

    /**
     * 加密字符串
     *
     * @param plainText 明文
     * @return 密文
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            return encryptor.encrypt(plainText);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    /**
     * 解密字符串
     *
     * @param encryptedText 密文
     * @return 明文
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        try {
            return encryptor.decrypt(encryptedText);
        } catch (Exception e) {
            // 如果解密失败,可能是未加密的数据,直接返回
            return encryptedText;
        }
    }

    /**
     * 批量加密
     *
     * @param plainTexts 明文列表
     * @return 密文列表
     */
    public String[] encryptBatch(String[] plainTexts) {
        if (plainTexts == null) {
            return null;
        }
        String[] encrypted = new String[plainTexts.length];
        for (int i = 0; i < plainTexts.length; i++) {
            encrypted[i] = encrypt(plainTexts[i]);
        }
        return encrypted;
    }

    /**
     * 批量解密
     *
     * @param encryptedTexts 密文列表
     * @return 明文列表
     */
    public String[] decryptBatch(String[] encryptedTexts) {
        if (encryptedTexts == null) {
            return null;
        }
        String[] decrypted = new String[encryptedTexts.length];
        for (int i = 0; i < encryptedTexts.length; i++) {
            decrypted[i] = decrypt(encryptedTexts[i]);
        }
        return decrypted;
    }
}
