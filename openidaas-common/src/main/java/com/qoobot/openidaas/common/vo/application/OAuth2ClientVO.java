package com.qoobot.openidaas.common.vo.application;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * OAuth2客户端信息VO
 */
@Data
@Schema(description = "OAuth2客户端信息")
public class OAuth2ClientVO {

    @Schema(description = "OAuth2客户端ID")
    private Long id;

    @Schema(description = "客户端ID")
    private String clientId;

    @Schema(description = "应用ID")
    private Long appId;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "授权类型列表")
    private String grantTypes;

    @Schema(description = "权限范围")
    private String scopes;

    @Schema(description = "访问令牌有效期（秒）")
    private Integer accessTokenValidity;

    @Schema(description = "刷新令牌有效期（秒）")
    private Integer refreshTokenValidity;

    @Schema(description = "是否自动批准")
    private Boolean autoApprove;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
