package com.qoobot.openidaas.audit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qoobot.openidaas.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 审计日志实体
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("audit_logs")
public class AuditLog extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 操作描述
     */
    private String operationDesc;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作子模块
     */
    private String subModule;

    /**
     * 操作对象类型
     */
    private String targetType;

    /**
     * 操作对象ID
     */
    private Long targetId;

    /**
     * 操作对象名称
     */
    private String targetName;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 响应结果
     */
    private String responseResult;

    /**
     * 操作用户ID
     */
    private Long operatorId;

    /**
     * 操作用户名
     */
    private String operatorName;

    /**
     * 操作用户IP
     */
    private String operatorIp;

    /**
     * 操作用户代理
     */
    private String userAgent;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * 执行耗时（毫秒）
     */
    private Long executionTime;

    /**
     * 操作结果（SUCCESS/FAILURE）
     */
    private String result;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 应用ID
     */
    private Long appId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
