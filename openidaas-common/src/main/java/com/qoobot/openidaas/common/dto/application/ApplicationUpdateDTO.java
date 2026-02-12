package com.qoobot.openidaas.common.dto.application;

import com.qoobot.openidaas.common.dto.BaseDTO;
import com.qoobot.openidaas.common.enumeration.ApplicationTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 应用更新DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "应用更新请求")
public class ApplicationUpdateDTO extends BaseDTO {

    @NotNull(message = "应用ID不能为空")
    @Schema(description = "应用ID", required = true)
    private Long id;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "应用类型")
    private Integer appType;

    @Schema(description = "重定向URI列表")
    private List<String> redirectUris;

    @Schema(description = "Logo URL")
    private String logoUrl;

    @Schema(description = "主页URL")
    private String homepageUrl;

    @Schema(description = "应用描述")
    private String description;

    @Schema(description = "状态：1-启用，2-禁用")
    private Integer status;
}
