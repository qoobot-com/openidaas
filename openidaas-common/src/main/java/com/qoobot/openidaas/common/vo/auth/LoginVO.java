package com.qoobot.openidaas.common.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录响应VO
 *
 * @author QooBot
 */
@Data
@Schema(description = "登录响应")
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "访问令牌")
    private String accessToken;

    @Schema(description = "刷新令牌")
    private String refreshToken;

    @Schema(description = "令牌类型")
    private String tokenType = "Bearer";

    @Schema(description = "过期时间")
    private LocalDateTime expiresAt;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户昵称")
    private String nickname;

    public LoginVO() {}

    public LoginVO(String accessToken, String refreshToken, LocalDateTime expiresAt, 
                   Long userId, String username, String nickname) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
    }
}