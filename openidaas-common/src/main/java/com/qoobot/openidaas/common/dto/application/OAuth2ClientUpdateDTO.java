package com.qoobot.openidaas.common.dto.application;

import com.qoobot.openidaas.common.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * OAuth2客户端更新DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "OAuth2客户端更新请求")
public class OAuth2ClientUpdateDTO extends BaseDTO {

    @NotNull(message = "ID不能为空")
    @Schema(description = "OAuth2客户端ID", required = true)
    private Long id;

    @Schema(description = "客户端密钥")
    private String clientSecret;

    @Schema(description = "授权类型列表")
    private List<String> grantTypes;

    @Schema(description = "权限范围")
    private List<String> scopes;

    @Schema(description = "访问令牌有效期（秒）")
    private Integer accessTokenValidity;

    @Schema(description = "刷新令牌有效期（秒）")
    private Integer refreshTokenValidity;

    @Schema(description = "是否自动批准")
    private Boolean autoApprove;
}
