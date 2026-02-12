package com.qoobot.openidaas.common.vo.application;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应用信息VO
 */
@Data
@Schema(description = "应用信息")
public class ApplicationVO {

    @Schema(description = "应用ID")
    private Long id;

    @Schema(description = "应用密钥")
    private String appKey;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "应用类型：1-Web，2-移动，3-API，4-桌面，5-服务")
    private Integer appType;

    @Schema(description = "应用类型描述")
    private String appTypeDesc;

    @Schema(description = "重定向URI列表")
    private String redirectUris;

    @Schema(description = "Logo URL")
    private String logoUrl;

    @Schema(description = "主页URL")
    private String homepageUrl;

    @Schema(description = "应用描述")
    private String description;

    @Schema(description = "状态：1-启用，2-禁用")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "所有者ID")
    private Long ownerId;

    @Schema(description = "所有者名称")
    private String ownerName;

    @Schema(description = "OAuth2客户端配置")
    private OAuth2ClientVO oauth2Client;

    @Schema(description = "SAML服务提供商配置")
    private SamlSpVO samlSp;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Data
    @Schema(description = "OAuth2客户端信息")
    public static class OAuth2ClientVO {
        private Long id;
        private String clientId;
        private String grantTypes;
        private String scopes;
        private Integer accessTokenValidity;
        private Integer refreshTokenValidity;
        private Boolean autoApprove;
    }

    @Data
    @Schema(description = "SAML服务提供商信息")
    public static class SamlSpVO {
        private Long id;
        private String spEntityId;
        private String acsUrl;
        private String metadataUrl;
    }
}
