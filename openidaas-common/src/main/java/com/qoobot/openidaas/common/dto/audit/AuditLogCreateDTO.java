package com.qoobot.openidaas.common.dto.audit;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审计日志创建DTO
 *
 * @author QooBot
 */
@Data
@Schema(description = "审计日志创建请求")
public class AuditLogCreateDTO {

    @NotBlank(message = "操作类型不能为空")
    @Schema(description = "操作类型", example = "CREATE")
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

    @NotNull(message = "操作人ID不能为空")
    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人名称")
    private String operatorName;

    @Schema(description = "操作人IP")
    private String operatorIp;

    @Schema(description = "用户代理")
    private String userAgent;

    @Schema(description = "执行耗时（毫秒）")
    private Long executionTime;

    @Schema(description = "操作时间")
    private java.time.LocalDateTime operationTime;

    @Schema(description = "操作结果")
    private String result;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "应用ID")
    private Long appId;
}
