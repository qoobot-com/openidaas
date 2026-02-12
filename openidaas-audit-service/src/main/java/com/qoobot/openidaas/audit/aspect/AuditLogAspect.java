package com.qoobot.openidaas.audit.aspect;

import com.qoobot.openidaas.audit.service.AuditService;
import com.qoobot.openidaas.common.dto.audit.AuditLogCreateDTO;
import com.qoobot.openidaas.common.enumeration.AuditResultEnum;
import com.qoobot.openidaas.common.enumeration.OperationTypeEnum;
import com.qoobot.openidaas.common.util.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.*;
import java.time.LocalDateTime;

/**
 * 审计日志切面
 *
 * @author QooBot
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditService auditService;

    /**
     * 审计日志注解
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Audit {

        /**
         * 操作类型
         */
        OperationTypeEnum operationType() default OperationTypeEnum.READ;

        /**
         * 操作描述
         */
        String description() default "";

        /**
         * 操作模块
         */
        String module() default "";

        /**
         * 操作子模块
         */
        String subModule() default "";

        /**
         * 目标类型
         */
        String targetType() default "";

        /**
         * 是否记录请求参数
         */
        boolean recordParams() default true;

        /**
         * 是否记录响应结果
         */
        boolean recordResult() default false;

        /**
         * 是否异步记录
         */
        boolean async() default false;
    }

    @Around("@annotation(audit)")
    public Object around(ProceedingJoinPoint joinPoint, Audit audit) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable exception = null;

        // 获取当前用户ID（需要从SecurityContext或Token中获取）
        Long operatorId = getCurrentOperatorId();
        String operatorName = getCurrentOperatorName();

        // 构建审计日志
        AuditLogCreateDTO auditLog = new AuditLogCreateDTO();
        auditLog.setOperationType(audit.operationType().getCode());
        auditLog.setOperationDesc(audit.description());
        auditLog.setModule(audit.module());
        auditLog.setSubModule(audit.subModule());
        auditLog.setTargetType(audit.targetType());
        auditLog.setOperatorId(operatorId);
        auditLog.setOperatorName(operatorName);

        // 获取请求信息
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            auditLog.setRequestUrl(request.getRequestURI());
            auditLog.setRequestMethod(request.getMethod());
            auditLog.setOperatorIp(getClientIp(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
        }

        // 记录请求参数
        if (audit.recordParams()) {
            try {
                String requestParams = getRequestParams(joinPoint.getArgs());
                auditLog.setRequestParams(requestParams);
            } catch (Exception e) {
                log.warn("记录请求参数失败", e);
            }
        }

        try {
            // 执行目标方法
            result = joinPoint.proceed();

            // 记录响应结果
            if (audit.recordResult() && result != null) {
                try {
                    String responseResult = JsonUtil.toJson(result);
                    // 限制响应结果长度，避免过大
                    if (responseResult.length() > 5000) {
                        responseResult = responseResult.substring(0, 5000) + "...";
                    }
                    auditLog.setResponseResult(responseResult);
                } catch (Exception e) {
                    log.warn("记录响应结果失败", e);
                }
            }

            auditLog.setResult(AuditResultEnum.SUCCESS.getCode());

        } catch (Throwable e) {
            exception = e;
            auditLog.setResult(AuditResultEnum.FAILURE.getCode());
            auditLog.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            // 计算执行耗时
            long executionTime = System.currentTimeMillis() - startTime;
            auditLog.setExecutionTime(executionTime);
            auditLog.setOperationTime(LocalDateTime.now());

            // 记录审计日志
            try {
                if (audit.async()) {
                    auditService.sendAuditLogAsync(auditLog);
                } else {
                    auditService.recordAuditLog(auditLog);
                }
            } catch (Exception e) {
                log.error("记录审计日志失败", e);
            }
        }

        return result;
    }

    /**
     * 获取当前操作人ID
     */
    private Long getCurrentOperatorId() {
        // TODO: 从SecurityContext或Token中获取当前用户ID
        return 1L;
    }

    /**
     * 获取当前操作人名称
     */
    private String getCurrentOperatorName() {
        // TODO: 从SecurityContext或Token中获取当前用户名
        return "system";
    }

    /**
     * 获取当前请求
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
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

    /**
     * 获取请求参数
     */
    private String getRequestParams(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        try {
            return JsonUtil.toJson(args);
        } catch (Exception e) {
            return args.toString();
        }
    }
}
