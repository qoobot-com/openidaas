package com.qoobot.openidaas.user.exception;

/**
 * 角色已存在异常
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public class RoleAlreadyExistsException extends RuntimeException {

    public RoleAlreadyExistsException(String message) {
        super(message);
    }
}
