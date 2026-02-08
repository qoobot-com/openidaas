package com.qoobot.openidaas.user.exception;

/**
 * 部门已存在异常
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public class DepartmentAlreadyExistsException extends RuntimeException {

    public DepartmentAlreadyExistsException(String message) {
        super(message);
    }
}