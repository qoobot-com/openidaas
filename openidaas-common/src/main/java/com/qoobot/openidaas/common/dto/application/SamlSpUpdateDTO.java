package com.qoobot.openidaas.common.dto.application;

import com.qoobot.openidaas.common.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SAML服务提供商更新DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "SAML服务提供商更新请求")
public class SamlSpUpdateDTO extends BaseDTO {

    @NotNull(message = "ID不能为空")
    @Schema(description = "SAML服务提供商ID", required = true)
    private Long id;

    @Schema(description = "SP实体ID")
    private String spEntityId;

    @Schema(description = "断言消费服务URL")
    private String acsUrl;

    @Schema(description = "证书（PEM格式）")
    private String certificate;

    @Schema(description = "元数据URL")
    private String metadataUrl;
}
