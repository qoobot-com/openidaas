package com.qoobot.openidaas.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码工具类
 * 使用 BCrypt 进行密码加密，符合 OWASP 安全标准
 *
 * @author QooBot
 */
@Slf4j
@Component
public class PasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    /**
     * 使用 BCrypt 加密密码（强度=12）
     * 自动生成并嵌入盐值，每次加密结果不同但验证时结果一致
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码（$2a$12$... 格式）
     */
    public static String encodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        try {
            return encoder.encode(rawPassword);
        } catch (Exception e) {
            log.error("密码加密失败", e);
            throw new RuntimeException("密码加密失败", e);
        }
    }

    /**
     * 兼容旧版本的 encode 方法
     * 使用 BCrypt 加密
     */
    public static String encode(String rawPassword) {
        return encodePassword(rawPassword);
    }

    /**
     * 验证密码
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的密码（BCrypt 格式）
     * @return 验证成功返回 true
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        try {
            return encoder.matches(rawPassword, encodedPassword);
        } catch (Exception e) {
            log.error("密码验证失败", e);
            return false;
        }
    }

    /**
     * 检查密码强度
     */
    public static PasswordStrength checkPasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            return PasswordStrength.WEAK;
        }

        int score = 0;
        
        // 长度检查
        if (password.length() >= 12) {
            score += 2;
        } else if (password.length() >= 10) {
            score += 1;
        }

        // 字符类型检查
        if (password.matches(".*[a-z].*")) score++;  // 小写字母
        if (password.matches(".*[A-Z].*")) score++;  // 大写字母
        if (password.matches(".*\\d.*")) score++;    // 数字
        if (password.matches(".*[^a-zA-Z0-9].*")) score++; // 特殊字符

        if (score >= 5) {
            return PasswordStrength.STRONG;
        } else if (score >= 3) {
            return PasswordStrength.MEDIUM;
        } else {
            return PasswordStrength.WEAK;
        }
    }

    public static boolean verifyPassword(String password, String passwordHash) {
        return encoder.matches(password, passwordHash);
    }

    /**
     * 密码强度枚举
     */
    public enum PasswordStrength {
        WEAK("弱"),
        MEDIUM("中"),
        STRONG("强");

        private final String description;

        PasswordStrength(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}