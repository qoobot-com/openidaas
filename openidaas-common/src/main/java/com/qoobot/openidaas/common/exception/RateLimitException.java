package com.qoobot.openidaas.common.exception;

/**
 * 限流异常
 * 当请求被Sentinel限流时抛出此异常
 */
public class RateLimitException extends RuntimeException {
    
    public RateLimitException(String message) {
        super(message);
    }
    
    public RateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}