package com.qoobot.openidaas.user.exception;

/**
 * 权限未找到异常
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public class PermissionNotFoundException extends RuntimeException {

    public PermissionNotFoundException(String message) {
        super(message);
    }
}
