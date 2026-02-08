package com.qoobot.openidaas.user.exception;

/**
 * 无效的部门移动异常
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public class InvalidDepartmentMoveException extends RuntimeException {

    public InvalidDepartmentMoveException(String message) {
        super(message);
    }
}
