package com.qoobot.openidaas.security.listener;

import com.qoobot.openidaas.security.audit.AuditLogEvent;
import com.qoobot.openidaas.security.constants.SecurityConstants;
import com.qoobot.openidaas.security.service.AuditLogService;
import com.qoobot.openidaas.security.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.stereotype.Component;

/**
 * 安全事件监听器
 * 
 * 监听Spring Security安全事件并处理
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityEventListener {

    private final LoginAttemptService loginAttemptService;
    private final AuditLogService auditLogService;

    /**
     * 处理认证成功事件
     */
    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();

        log.info("Authentication success for user: {}", username);

        // 重置登录失败计数
        loginAttemptService.loginSucceeded(username);

        // 记录审计日志
        AuditLogEvent auditEvent = AuditLogEvent.builder()
                .username(username)
                .operation(SecurityConstants.EVENT_LOGIN)
                .module("AUTHENTICATION")
                .description("User logged in successfully")
                .success(true)
                .timestamp(java.time.LocalDateTime.now())
                .build();

        auditLogService.logEvent(auditEvent);
    }

    /**
     * 处理认证失败事件
     */
    @EventListener
    public void handleAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();

        log.warn("Authentication failure for user: {}", username);

        // 记录登录失败
        loginAttemptService.loginFailed(username);

        // 记录审计日志
        AuditLogEvent auditEvent = AuditLogEvent.builder()
                .username(username)
                .operation(SecurityConstants.EVENT_LOGIN)
                .module("AUTHENTICATION")
                .description("User login failed: Bad credentials")
                .success(false)
                .error("Bad credentials")
                .errorType("AuthenticationException")
                .timestamp(java.time.LocalDateTime.now())
                .build();

        auditLogService.logEvent(auditEvent);

        // 检查是否需要锁定账户
        if (loginAttemptService.isLocked(username)) {
            log.warn("Account locked for user: {}", username);

            AuditLogEvent lockEvent = AuditLogEvent.builder()
                    .username(username)
                    .operation(SecurityConstants.EVENT_ACCOUNT_LOCK)
                    .module("SECURITY")
                    .description("Account locked due to too many failed attempts")
                    .success(true)
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

            auditLogService.logEvent(lockEvent);
        }
    }

    /**
     * 处理会话销毁事件
     */
    @EventListener
    public void handleSessionDestroyed(SessionDestroyedEvent event) {
        event.getSecurityContexts().forEach(securityContext -> {
            String username = securityContext.getAuthentication().getName();

            log.info("Session destroyed for user: {}", username);

            AuditLogEvent auditEvent = AuditLogEvent.builder()
                    .username(username)
                    .operation(SecurityConstants.EVENT_LOGOUT)
                    .module("AUTHENTICATION")
                    .description("User logged out")
                    .success(true)
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

            auditLogService.logEvent(auditEvent);
        });
    }
}
