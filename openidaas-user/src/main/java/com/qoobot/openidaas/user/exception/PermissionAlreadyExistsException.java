package com.qoobot.openidaas.user.exception;

/**
 * 权限已存在异常
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public class PermissionAlreadyExistsException extends RuntimeException {

    public PermissionAlreadyExistsException(String message) {
        super(message);
    }
}
