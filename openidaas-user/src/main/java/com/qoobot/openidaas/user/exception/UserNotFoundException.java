package com.qoobot.openidaas.user.exception;

/**
 * 用户未找到异常
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
