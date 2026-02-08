package com.qoobot.openidaas.user.exception;

/**
 * 部门有子部门异常
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public class DepartmentHasChildrenException extends RuntimeException {

    public DepartmentHasChildrenException(String message) {
        super(message);
    }
}