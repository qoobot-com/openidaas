package com.qoobot.openidaas.user.exception;

/**
 * 部门有关联用户异常
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public class DepartmentHasUsersException extends RuntimeException {

    public DepartmentHasUsersException(String message) {
        super(message);
    }
}