package com.qoobot.openidaas.core.exception;

/**
 * 租户未找到异常
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
public class TenantNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public TenantNotFoundException(String message) {
        super(message);
    }
    
    public TenantNotFoundException(Long tenantId) {
        super("Tenant not found with id: " + tenantId);
    }
    
    public TenantNotFoundException(String code) {
        super("Tenant not found with code: " + code);
    }
}
