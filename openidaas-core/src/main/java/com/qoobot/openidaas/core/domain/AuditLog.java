package com.qoobot.openidaas.core.domain;

import com.qoobot.openidaas.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 审计日志领域模型
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {

    /**
     * 操作类型
     */
    @Column(name = "operation_type", length = 50, nullable = false)
    private String operationType;

    /**
     * 操作描述
     */
    @Column(name = "operation_desc", length = 200)
    private String operationDesc;

    /**
     * 操作模块
     */
    @Column(name = "module", length = 50)
    private String module;

    /**
     * 操作子模块
     */
    @Column(name = "sub_module", length = 50)
    private String subModule;

    /**
     * 操作对象类型
     */
    @Column(name = "target_type", length = 50)
    private String targetType;

    /**
     * 操作对象ID
     */
    @Column(name = "target_id")
    private Long targetId;

    /**
     * 操作对象名称
     */
    @Column(name = "target_name", length = 100)
    private String targetName;

    /**
     * 请求URL
     */
    @Column(name = "request_url", length = 500)
    private String requestUrl;

    /**
     * 请求方法
     */
    @Column(name = "request_method", length = 10)
    private String requestMethod;

    /**
     * 请求参数
     */
    @Column(name = "request_params", columnDefinition = "TEXT")
    private String requestParams;

    /**
     * 响应结果
     */
    @Column(name = "response_result", columnDefinition = "TEXT")
    private String responseResult;

    /**
     * 操作用户ID
     */
    @Column(name = "operator_id")
    private Long operatorId;

    /**
     * 操作用户名
     */
    @Column(name = "operator_name", length = 50)
    private String operatorName;

    /**
     * 操作用户IP
     */
    @Column(name = "operator_ip", length = 50)
    private String operatorIp;

    /**
     * 操作用户代理
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 操作时间
     */
    @Column(name = "operation_time", nullable = false)
    private LocalDateTime operationTime;

    /**
     * 执行耗时（毫秒）
     */
    @Column(name = "execution_time")
    private Long executionTime;

    /**
     * 操作结果（成功/失败）
     */
    @Column(name = "result", length = 20)
    private String result;

    /**
     * 错误信息
     */
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    /**
     * 租户ID
     */
    @Column(name = "tenant_id")
    private Long tenantId;

    /**
     * 应用ID
     */
    @Column(name = "app_id")
    private Long appId;
}