package com.qoobot.openidaas.user.exception;

/**
 * 系统角色不能修改异常
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public class SystemRoleCannotBeModifiedException extends RuntimeException {

    public SystemRoleCannotBeModifiedException(String message) {
        super(message);
    }
}
