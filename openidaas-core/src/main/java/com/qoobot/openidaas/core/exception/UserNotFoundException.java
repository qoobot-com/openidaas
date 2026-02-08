package com.qoobot.openidaas.core.exception;

/**
 * 用户未找到异常
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public class UserNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(Long userId) {
        super("User not found with id: " + userId);
    }
    
    public UserNotFoundException(String username) {
        super("User not found with username: " + username);
    }
}
