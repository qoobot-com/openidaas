package com.qoobot.openidaas.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qoobot.openidaas.common.util.DataMaskUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 安全审计切面
 * 记录敏感操作、登录登出、权限变更等
 *
 * @author QooBot
 */
@Slf4j
@Aspect
@Component
public class SecurityAuditAspect {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 定义切入点: 所有Controller方法
     */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {
    }

    /**
     * 定义切入点: 敏感操作方法
     */
    @Pointcut("@annotation(com.qoobot.openidaas.common.annotation.SensitiveOperation)")
    public void sensitiveOperations() {
    }

    /**
     * 记录所有Controller调用
     */
    @Around("controllerMethods()")
    public Object auditController(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();

        try {
            // 获取请求信息
            HttpServletRequest request = getCurrentRequest();
            String uri = request != null ? request.getRequestURI() : "unknown";
            String method = request != null ? request.getMethod() : "unknown";
            String ip = request != null ? getClientIp(request) : "unknown";
            String userAgent = request != null ? request.getHeader("User-Agent") : "unknown";
            String userId = request != null ? request.getHeader("X-User-Id") : "anonymous";

            // 记录请求
            log.info("[AUDIT] Request ID: {}, URI: {}, Method: {}, User: {}, IP: {}",
                requestId, uri, method, userId, ip);

            // 执行方法
            Object result = joinPoint.proceed();

            // 计算耗时
            long duration = System.currentTimeMillis() - startTime;

            // 记录响应(不包含敏感数据)
            log.info("[AUDIT] Request ID: {}, Status: SUCCESS, Duration: {}ms",
                requestId, duration);

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[AUDIT] Request ID: {}, Status: ERROR, Duration: {}ms, Error: {}",
                requestId, duration, e.getMessage());
            throw e;
        }
    }

    /**
     * 记录敏感操作
     */
    @Around("sensitiveOperations()")
    public Object auditSensitiveOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        String requestId = UUID.randomUUID().toString();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        HttpServletRequest request = getCurrentRequest();
        String userId = request != null ? request.getHeader("X-User-Id") : "unknown";
        String ip = request != null ? getClientIp(request) : "unknown";

        // 记录敏感操作(脱敏请求参数)
        Map<String, Object> auditLog = new HashMap<>();
        auditLog.put("requestId", requestId);
        auditLog.put("operation", className + "." + methodName);
        auditLog.put("userId", userId);
        auditLog.put("ip", ip);
        auditLog.put("timestamp", LocalDateTime.now());
        auditLog.put("request", maskSensitiveData(joinPoint.getArgs()));

        log.warn("[SECURITY AUDIT] Sensitive Operation: {}", objectMapper.writeValueAsString(auditLog));

        try {
            Object result = joinPoint.proceed();

            // 记录成功(脱敏响应数据)
            Map<String, Object> successLog = new HashMap<>(auditLog);
            successLog.put("status", "SUCCESS");
            successLog.put("response", maskSensitiveData(result));
            log.info("[SECURITY AUDIT] Operation Success: {}", objectMapper.writeValueAsString(successLog));

            return result;

        } catch (Exception e) {
            // 记录失败
            Map<String, Object> failureLog = new HashMap<>(auditLog);
            failureLog.put("status", "FAILED");
            failureLog.put("error", e.getMessage());
            log.error("[SECURITY AUDIT] Operation Failed: {}", objectMapper.writeValueAsString(failureLog));

            throw e;
        }
    }

    /**
     * 脱敏敏感数据
     */
    private Object maskSensitiveData(Object data) {
        if (data == null) {
            return null;
        }

        try {
            String json = objectMapper.writeValueAsString(data);

            // 简单替换常见的敏感字段(可根据需要扩展)
            json = json.replaceAll("\"password\":\"[^\"]*\"", "\"password\":\"******\"");
            json = json.replaceAll("\"oldPassword\":\"[^\"]*\"", "\"oldPassword\":\"******\"");
            json = json.replaceAll("\"newPassword\":\"[^\"]*\"", "\"newPassword\":\"******\"");
            json = json.replaceAll("\"secret\":\"[^\"]*\"", "\"secret\":\"******\"");
            json = json.replaceAll("\"mfaCode\":\"[^\"]*\"", "\"mfaCode\":\"******\"");

            return json;
        } catch (Exception e) {
            return "[Unable to mask data]";
        }
    }

    /**
     * 获取当前请求
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多个IP的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
