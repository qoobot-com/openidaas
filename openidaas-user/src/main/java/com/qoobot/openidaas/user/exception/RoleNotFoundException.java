package com.qoobot.openidaas.user.exception;

/**
 * 角色未找到异常
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(String message) {
        super(message);
    }
}
