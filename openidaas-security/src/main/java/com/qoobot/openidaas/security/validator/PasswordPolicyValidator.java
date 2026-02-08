package com.qoobot.openidaas.security.validator;

import com.qoobot.openidaas.security.config.SecurityProperties;
import com.qoobot.openidaas.security.exception.PasswordPolicyViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.passay.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 密码策略验证器
 * 
 * 基于Passay库实现完整的密码策略验证
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordPolicyValidator {

    private final SecurityProperties securityProperties;

    /**
     * 验证密码是否符合策略
     * 
     * @param password 密码
     * @throws PasswordPolicyViolationException 密码策略违规异常
     */
    public void validate(String password) throws PasswordPolicyViolationException {
        SecurityProperties.Password passwordPolicy = securityProperties.getPassword();

        List<Rule> rules = new ArrayList<>();

        // 长度规则
        rules.add(new LengthRule(
                passwordPolicy.getMaxLength() > 0 ? 
                        passwordPolicy.getMinLength() : 8,
                passwordPolicy.getMaxLength() > 0 ? 
                        passwordPolicy.getMaxLength() : 128
        ));

        // 大写字母规则
        if (passwordPolicy.getRequireUppercase()) {
            rules.add(new CharacterRule(EnglishCharacterData.UpperCase, 1));
        }

        // 小写字母规则
        if (passwordPolicy.getRequireLowercase()) {
            rules.add(new CharacterRule(EnglishCharacterData.LowerCase, 1));
        }

        // 数字规则
        if (passwordPolicy.getRequireDigit()) {
            rules.add(new CharacterRule(EnglishCharacterData.Digit, 1));
        }

        // 特殊字符规则
        if (passwordPolicy.getRequireSpecialChar()) {
            String specialChars = passwordPolicy.getSpecialChars();
            if (specialChars != null && !specialChars.isEmpty()) {
                rules.add(new CharacterRule(new CharacterData() {
                    @Override
                    public String getErrorCode() {
                        return "ILLEGAL_CHAR";
                    }

                    @Override
                    public String getCharacters() {
                        return specialChars;
                    }
                }, 1));
            }
        }

        // 构建验证器
        PasswordValidator validator = new PasswordValidator(rules);

        // 执行验证
        RuleResult result = validator.validate(new PasswordData(password));

        if (!result.isValid()) {
            List<String> messages = validator.getMessages(result);
            String message = String.join(", ", messages);
            log.warn("Password validation failed: {}", message);
            throw new PasswordPolicyViolationException(message);
        }

        log.debug("Password validation passed");
    }

    /**
     * 检查密码是否在历史记录中
     * 
     * @param userId 用户ID
     * @param newPassword 新密码
     * @param passwordHistory 密码历史（加密后的）
     * @param passwordEncoder 密码编码器
     * @return 是否在历史记录中
     */
    public boolean isInHistory(
            Long userId,
            String newPassword,
            List<String> passwordHistory,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {

        int historyCount = securityProperties.getPassword().getHistoryCount();
        if (historyCount <= 0 || passwordHistory == null || passwordHistory.isEmpty()) {
            return false;
        }

        // 检查最近的N个密码
        int checkCount = Math.min(historyCount, passwordHistory.size());
        for (int i = 0; i < checkCount; i++) {
            String oldPassword = passwordHistory.get(passwordHistory.size() - 1 - i);
            if (passwordEncoder.matches(newPassword, oldPassword)) {
                log.warn("Password found in history for user: {}", userId);
                return true;
            }
        }

        return false;
    }

    /**
     * 获取密码强度
     * 
     * @param password 密码
     * @return 密码强度（0-100）
     */
    public int getPasswordStrength(String password) {
        int strength = 0;

        // 长度得分
        strength += Math.min(password.length() * 4, 40);

        // 字符类型得分
        boolean hasUpper = !password.equals(password.toLowerCase());
        boolean hasLower = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = !password.matches("[a-zA-Z0-9]*");

        if (hasUpper) strength += 10;
        if (hasLower) strength += 10;
        if (hasDigit) strength += 15;
        if (hasSpecial) strength += 15;

        // 字符种类多样性
        int variety = 0;
        if (hasUpper) variety++;
        if (hasLower) variety++;
        if (hasDigit) variety++;
        if (hasSpecial) variety++;
        strength += (variety - 1) * 5;

        // 限制最大值
        return Math.min(strength, 100);
    }

    /**
     * 获取密码强度等级
     * 
     * @param password 密码
     * @return 强度等级
     */
    public PasswordStrength getPasswordStrengthLevel(String password) {
        int strength = getPasswordStrength(password);

        if (strength >= 80) {
            return PasswordStrength.STRONG;
        } else if (strength >= 60) {
            return PasswordStrength.GOOD;
        } else if (strength >= 40) {
            return PasswordStrength.FAIR;
        } else {
            return PasswordStrength.WEAK;
        }
    }

    /**
     * 密码强度枚举
     */
    public enum PasswordStrength {
        WEAK,
        FAIR,
        GOOD,
        STRONG
    }
}
