package com.qoobot.openidaas.security.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qoobot.openidaas.security.audit.AuditLog;
import com.qoobot.openidaas.security.audit.AuditLogEvent;
import com.qoobot.openidaas.security.service.AuditLogService;
import com.qoobot.openidaas.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 审计日志切面
 * 
 * 基于Spring AOP实现操作日志记录
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;
    private final HttpServletRequest request;

    /**
     * 环绕通知：拦截带有@AuditLog注解的方法
     */
    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        Throwable exception = null;
        Object result = null;

        try {
            // 执行目标方法
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // 异步记录审计日志
            recordAuditLog(joinPoint, auditLog, result, exception, duration);
        }
    }

    /**
     * 记录审计日志（异步）
     */
    @Async("auditLogExecutor")
    protected void recordAuditLog(
            ProceedingJoinPoint joinPoint,
            AuditLog auditLog,
            Object result,
            Throwable exception,
            long duration) {

        try {
            AuditLogEvent event = buildAuditLogEvent(joinPoint, auditLog, result, exception, duration);
            auditLogService.logEvent(event);
        } catch (Exception e) {
            log.error("Failed to record audit log: {}", e.getMessage());
        }
    }

    /**
     * 构建审计日志事件
     */
    private AuditLogEvent buildAuditLogEvent(
            ProceedingJoinPoint joinPoint,
            AuditLog auditLog,
            Object result,
            Throwable exception,
            long duration) {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        
        String username = SecurityUtils.getCurrentUsername().orElse("anonymous");
        Long userId = null; // TODO: 从认证信息中获取用户ID
        String ip = getClientIp();
        String userAgent = getUserAgent();

        AuditLogEvent.AuditLogEventBuilder builder = AuditLogEvent.builder()
                .username(username)
                .userId(userId)
                .operation(auditLog.operation())
                .module(auditLog.module())
                .description(auditLog.description())
                .className(joinPoint.getTarget().getClass().getSimpleName())
                .methodName(signature.getName())
                .ip(ip)
                .userAgent(userAgent)
                .timestamp(LocalDateTime.now())
                .duration(duration)
                .success(exception == null);

        // 记录参数
        if (auditLog.logParams()) {
            try {
                String params = objectMapper.writeValueAsString(joinPoint.getArgs());
                builder.params(params);
            } catch (Exception e) {
                log.debug("Failed to serialize method params: {}", e.getMessage());
            }
        }

        // 记录结果
        if (auditLog.logResult() && result != null) {
            try {
                String resultStr = objectMapper.writeValueAsString(result);
                builder.result(resultStr);
            } catch (Exception e) {
                log.debug("Failed to serialize method result: {}", e.getMessage());
            }
        }

        // 记录异常
        if (exception != null) {
            builder.error(exception.getMessage());
            builder.errorType(exception.getClass().getSimpleName());
        }

        return builder.build();
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 获取User-Agent
     */
    private String getUserAgent() {
        return request.getHeader("User-Agent");
    }
}
