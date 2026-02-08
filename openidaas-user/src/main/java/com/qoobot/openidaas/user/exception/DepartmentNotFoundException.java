package com.qoobot.openidaas.user.exception;

/**
 * 部门未找到异常
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public class DepartmentNotFoundException extends RuntimeException {

    public DepartmentNotFoundException(String message) {
        super(message);
    }
}
