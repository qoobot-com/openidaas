package com.qoobot.openidaas.common.dto.application;

import com.qoobot.openidaas.common.dto.BaseDTO;
import com.qoobot.openidaas.common.enumeration.ApplicationTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 应用创建DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "应用创建请求")
public class ApplicationCreateDTO extends BaseDTO {

    @NotBlank(message = "应用名称不能为空")
    @Schema(description = "应用名称", required = true)
    private String appName;

    @NotNull(message = "应用类型不能为空")
    @Schema(description = "应用类型：1-Web，2-移动，3-API，4-桌面，5-服务", required = true)
    private Integer appType;

    @Schema(description = "重定向URI列表")
    private List<String> redirectUris;

    @Schema(description = "Logo URL")
    private String logoUrl;

    @Schema(description = "主页URL")
    private String homepageUrl;

    @Schema(description = "应用描述")
    private String description;

    @Schema(description = "所有者ID")
    private Long ownerId;
}
