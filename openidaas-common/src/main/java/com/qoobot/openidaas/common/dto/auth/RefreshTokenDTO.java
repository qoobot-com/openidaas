package com.qoobot.openidaas.common.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 刷新令牌DTO
 *
 * @author QooBot
 */
@Data
@Schema(description = "刷新令牌请求")
public class RefreshTokenDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "刷新令牌", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;

    @Schema(description = "客户端ID")
    private String clientId;

    @Schema(description = "客户端密钥")
    private String clientSecret;

    public RefreshTokenDTO() {}

    public RefreshTokenDTO(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public RefreshTokenDTO(String refreshToken, String clientId, String clientSecret) {
        this.refreshToken = refreshToken;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }
}