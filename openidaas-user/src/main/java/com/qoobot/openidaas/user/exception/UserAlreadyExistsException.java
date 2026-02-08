package com.qoobot.openidaas.user.exception;

/**
 * 用户已存在异常
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
