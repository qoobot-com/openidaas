package com.qoobot.openidaas.core.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.qoobot.openidaas.common.entity.BaseEntity;
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
@TableName("audit_logs")
public class AuditLog extends BaseEntity {

    /**
     * 操作类型
     */
    @TableField("operation_type")
    private String operationType;

    /**
     * 操作描述
     */
    @TableField("operation_desc")
    private String operationDesc;

    /**
     * 操作模块
     */
    @TableField("module")
    private String module;

    /**
     * 操作子模块
     */
    @TableField("sub_module")
    private String subModule;

    /**
     * 操作对象类型
     */
    @TableField("target_type")
    private String targetType;

    /**
     * 操作对象ID
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 操作对象名称
     */
    @TableField("target_name")
    private String targetName;

    /**
     * 请求URL
     */
    @TableField("request_url")
    private String requestUrl;

    /**
     * 请求方法
     */
    @TableField("request_method")
    private String requestMethod;

    /**
     * 请求参数
     */
    @TableField("request_params")
    private String requestParams;

    /**
     * 响应结果
     */
    @TableField("response_result")
    private String responseResult;

    /**
     * 操作用户ID
     */
    @TableField("operator_id")
    private Long operatorId;

    /**
     * 操作用户名
     */
    @TableField("operator_name")
    private String operatorName;

    /**
     * 操作用户IP
     */
    @TableField("operator_ip")
    private String operatorIp;

    /**
     * 操作用户代理
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 操作时间
     */
    @TableField("operation_time")
    private LocalDateTime operationTime;

    /**
     * 执行耗时（毫秒）
     */
    @TableField("execution_time")
    private Long executionTime;

    /**
     * 操作结果（成功/失败）
     */
    @TableField("result")
    private String result;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 应用ID
     */
    @TableField("app_id")
    private Long appId;
}
