package com.qoobot.openidaas.core.exception;

/**
 * 认证异常
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public class AuthenticationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
