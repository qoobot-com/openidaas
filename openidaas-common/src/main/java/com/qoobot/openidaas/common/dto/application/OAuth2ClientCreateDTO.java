package com.qoobot.openidaas.common.dto.application;

import com.qoobot.openidaas.common.dto.BaseDTO;
import com.qoobot.openidaas.common.enumeration.GrantTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * OAuth2客户端创建DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "OAuth2客户端创建请求")
public class OAuth2ClientCreateDTO extends BaseDTO {

    @NotNull(message = "应用ID不能为空")
    @Schema(description = "应用ID", required = true)
    private Long appId;

    @NotBlank(message = "客户端ID不能为空")
    @Schema(description = "客户端ID", required = true)
    private String clientId;

    @NotBlank(message = "客户端密钥不能为空")
    @Schema(description = "客户端密钥", required = true)
    private String clientSecret;

    @Schema(description = "授权类型列表")
    private List<String> grantTypes;

    @Schema(description = "权限范围")
    private List<String> scopes;

    @Schema(description = "访问令牌有效期（秒），默认3600")
    private Integer accessTokenValidity;

    @Schema(description = "刷新令牌有效期（秒），默认2592000")
    private Integer refreshTokenValidity;

    @Schema(description = "是否自动批准")
    private Boolean autoApprove;
}
