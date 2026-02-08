package com.qoobot.openidaas.user.exception;

/**
 * 无效密码异常
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException(String message) {
        super(message);
    }
}
