package com.qoobot.openidaas.common.vo.application;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SAML服务提供商信息VO
 */
@Data
@Schema(description = "SAML服务提供商信息")
public class SamlSpVO {

    @Schema(description = "SAML服务提供商ID")
    private Long id;

    @Schema(description = "SP实体ID")
    private String spEntityId;

    @Schema(description = "应用ID")
    private Long appId;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "断言消费服务URL")
    private String acsUrl;

    @Schema(description = "证书")
    private String certificate;

    @Schema(description = "元数据URL")
    private String metadataUrl;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
