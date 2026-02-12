package com.qoobot.openidaas.common.dto.application;

import com.qoobot.openidaas.common.dto.BaseDTO;
import com.qoobot.openidaas.common.enumeration.ApplicationTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应用查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "应用查询条件")
public class ApplicationQueryDTO extends BaseDTO {

    @Schema(description = "应用名称（模糊查询）")
    private String appName;

    @Schema(description = "应用类型")
    private Integer appType;

    @Schema(description = "状态：1-启用，2-禁用")
    private Integer status;

    @Schema(description = "所有者ID")
    private Long ownerId;
}
