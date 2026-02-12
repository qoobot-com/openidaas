package com.qoobot.openidaas.common.vo.audit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 审计统计VO
 *
 * @author QooBot
 */
@Data
@Schema(description = "审计统计响应")
public class AuditStatisticsVO {

    @Schema(description = "总操作次数")
    private Long totalOperations;

    @Schema(description = "成功操作次数")
    private Long successCount;

    @Schema(description = "失败操作次数")
    private Long failureCount;

    @Schema(description = "操作类型分布")
    private Map<String, Long> operationTypeDistribution;

    @Schema(description = "模块操作分布")
    private Map<String, Long> moduleDistribution;

    @Schema(description = "热门操作用户")
    private Map<String, Long> topUsers;
}
