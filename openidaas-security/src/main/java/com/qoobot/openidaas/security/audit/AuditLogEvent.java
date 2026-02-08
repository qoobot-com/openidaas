package com.qoobot.openidaas.security.audit;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计日志事件
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
@Builder
public class AuditLogEvent {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 操作名称
     */
    private String operation;

    /**
     * 模块名称
     */
    private String module;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 类名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 方法参数（JSON格式）
     */
    private String params;

    /**
     * 方法结果（JSON格式）
     */
    private String result;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 错误类型
     */
    private String errorType;

    /**
     * IP地址
     */
    private String ip;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 执行时长（毫秒）
     */
    private Long duration;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 风险级别
     */
    private AuditLog.RiskLevel riskLevel;
}
