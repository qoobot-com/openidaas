package com.qoobot.openidaas.common.vo.position;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 职位信息VO
 *
 * @author QooBot
 */
@Data
@Schema(description = "职位信息")
public class PositionVO {

    @Schema(description = "职位ID", example = "1")
    private Long id;

    @Schema(description = "职位编码", example = "POS_001")
    private String positionCode;

    @Schema(description = "职位名称", example = "高级工程师")
    private String positionName;

    @Schema(description = "所属部门ID", example = "1")
    private Long deptId;

    @Schema(description = "所属部门名称", example = "技术部")
    private String deptName;

    @Schema(description = "职级等级", example = "1")
    private Integer level;

    @Schema(description = "职级", example = "P7")
    private String jobGrade;

    @Schema(description = "汇报对象职位ID", example = "2")
    private Long reportsTo;

    @Schema(description = "汇报对象职位名称", example = "技术总监")
    private String reportsToName;

    @Schema(description = "是否管理岗位", example = "0")
    private Integer isManager;

    @Schema(description = "是否管理岗位", example = "false")
    private Boolean manager;

    @Schema(description = "职位描述", example = "负责核心技术研发工作")
    private String description;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "租户ID", example = "1")
    private Long tenantId;

    @Schema(description = "职位下用户数量", example = "10")
    private Long userCount;
}
