package com.qoobot.openidaas.common.dto.position;

import com.qoobot.openidaas.common.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 职位更新DTO
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "职位更新请求")
public class PositionUpdateDTO extends BaseDTO {

    @NotNull(message = "职位ID不能为空")
    @Schema(description = "职位ID", example = "1")
    private Long id;

    @NotBlank(message = "职位编码不能为空")
    @Schema(description = "职位编码", example = "POS_001")
    private String positionCode;

    @NotBlank(message = "职位名称不能为空")
    @Schema(description = "职位名称", example = "高级工程师")
    private String positionName;

    @Schema(description = "所属部门ID", example = "1")
    private Long deptId;

    @Schema(description = "职级等级", example = "1")
    private Integer level;

    @Schema(description = "职级", example = "P7")
    private String jobGrade;

    @Schema(description = "汇报对象职位ID", example = "2")
    private Long reportsTo;

    @Schema(description = "是否管理岗位", example = "0")
    private Integer isManager;

    @Schema(description = "职位描述", example = "负责核心技术研发工作")
    private String description;
}
