package com.qoobot.openidaas.common.dto.application;

import com.qoobot.openidaas.common.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SAML服务提供商创建DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "SAML服务提供商创建请求")
public class SamlSpCreateDTO extends BaseDTO {

    @NotNull(message = "应用ID不能为空")
    @Schema(description = "应用ID", required = true)
    private Long appId;

    @NotBlank(message = "SP实体ID不能为空")
    @Schema(description = "SP实体ID", required = true)
    private String spEntityId;

    @NotBlank(message = "断言消费服务URL不能为空")
    @Schema(description = "断言消费服务URL", required = true)
    private String acsUrl;

    @Schema(description = "证书（PEM格式）")
    private String certificate;

    @Schema(description = "元数据URL")
    private String metadataUrl;
}
