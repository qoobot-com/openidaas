package com.qoobot.openidaas.audit.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 日志分析切面
 * 自动收集和分析应用日志，提供运行时洞察
 */
@Slf4j
@Aspect
@Component
public class LogAnalysisAspect {

    private final StructuredLogger structuredLogger;

    public LogAnalysisAspect(StructuredLogger structuredLogger) {
        this.structuredLogger = structuredLogger;
    }

    /**
     * 记录数据库操作性能
     */
    @Around("execution(* com.qoobot.openidaas.audit.mapper.*.*(..))")
    public Object logDatabasePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // 如果执行时间超过1秒，记录为慢查询
            if (duration > 1000) {
                structuredLogger.logSlowQuery("Mapper", methodName, duration, 1000);
            }
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            structuredLogger.logAuditError("DatabaseOperation", e.getMessage(), methodName);
            throw e;
        }
    }

    /**
     * 记录API调用
     */
    @Around("execution(* com.qoobot.openidaas.audit.controller.*.*(..))")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // 提取用户信息（如果有）
            String user = extractUserFromArgs(joinPoint.getArgs());
            
            structuredLogger.logApiCall("HTTP", "/api/audit/" + methodName, 200, duration, user);
            
            // 记录性能指标
            if (duration > 3000) {
                log.warn("Slow API call detected: {} took {}ms", methodName, duration);
            }
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("API call failed: {} took {}ms, error: {}", methodName, duration, e.getMessage());
            throw e;
        }
    }

    /**
     * 记录服务层调用性能
     */
    @Around("execution(* com.qoobot.openidaas.audit.service.impl.*.*(..))")
    public Object logServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // 记录性能指标
            structuredLogger.logPerformanceMetric(methodName, duration, "ms");
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Service method failed: {} took {}ms, error: {}", methodName, duration, e.getMessage());
            throw e;
        }
    }

    /**
     * 从参数中提取用户信息
     */
    private String extractUserFromArgs(Object[] args) {
        // 简化实现，实际项目中可能需要从SecurityContext等获取
        for (Object arg : args) {
            if (arg != null && arg.toString().contains("userId")) {
                return arg.toString();
            }
        }
        return "anonymous";
    }
}
