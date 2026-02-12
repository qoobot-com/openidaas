package com.qoobot.openidaas.common.vo.audit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计日志VO
 *
 * @author QooBot
 */
@Data
@Schema(description = "审计日志响应")
public class AuditLogVO {

    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "操作类型")
    private String operationType;

    @Schema(description = "操作描述")
    private String operationDesc;

    @Schema(description = "操作模块")
    private String module;

    @Schema(description = "操作子模块")
    private String subModule;

    @Schema(description = "目标类型")
    private String targetType;

    @Schema(description = "目标ID")
    private Long targetId;

    @Schema(description = "目标名称")
    private String targetName;

    @Schema(description = "请求URL")
    private String requestUrl;

    @Schema(description = "请求方法")
    private String requestMethod;

    @Schema(description = "请求参数")
    private String requestParams;

    @Schema(description = "响应结果")
    private String responseResult;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人名称")
    private String operatorName;

    @Schema(description = "操作人IP")
    private String operatorIp;

    @Schema(description = "用户代理")
    private String userAgent;

    @Schema(description = "操作时间")
    private LocalDateTime operationTime;

    @Schema(description = "执行耗时（毫秒）")
    private Long executionTime;

    @Schema(description = "操作结果")
    private String result;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "应用ID")
    private Long appId;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
