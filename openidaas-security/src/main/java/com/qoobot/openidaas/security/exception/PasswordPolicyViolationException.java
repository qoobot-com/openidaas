package com.qoobot.openidaas.security.exception;

/**
 * 密码策略违规异常
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public class PasswordPolicyViolationException extends RuntimeException {

    public PasswordPolicyViolationException(String message) {
        super(message);
    }

    public PasswordPolicyViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
