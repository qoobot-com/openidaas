package com.qoobot.openidaas.common.dto.audit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计日志查询DTO
 *
 * @author QooBot
 */
@Data
@Schema(description = "审计日志查询条件")
public class AuditLogQueryDTO {

    @Schema(description = "操作类型")
    private String operationType;

    @Schema(description = "操作模块")
    private String module;

    @Schema(description = "操作子模块")
    private String subModule;

    @Schema(description = "目标类型")
    private String targetType;

    @Schema(description = "目标ID")
    private Long targetId;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人名称")
    private String operatorName;

    @Schema(description = "操作结果")
    private String result;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "应用ID")
    private Long appId;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "页码")
    private Integer page = 1;

    @Schema(description = "每页大小")
    private Integer size = 20;
}
