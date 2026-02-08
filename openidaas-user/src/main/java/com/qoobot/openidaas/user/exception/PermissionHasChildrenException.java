package com.qoobot.openidaas.user.exception;

/**
 * 权限有子权限异常
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public class PermissionHasChildrenException extends RuntimeException {

    public PermissionHasChildrenException(String message) {
        super(message);
    }
}
