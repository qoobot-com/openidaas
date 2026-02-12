package com.qoobot.openidaas.common.vo.application;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用详情VO
 *
 * @author QooBot
 */
@Data
@Schema(description = "应用详情")
public class ApplicationDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "应用ID")
    private Long id;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "应用描述")
    private String appDescription;

    @Schema(description = "客户端ID")
    private String clientId;

    @Schema(description = "客户端密钥")
    private String clientSecret;

    @Schema(description = "回调地址")
    private String redirectUri;

    @Schema(description = "授权类型")
    private String grantTypes;

    @Schema(description = "作用域")
    private String scopes;

    @Schema(description = "应用状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建者")
    private Long createdBy;

    @Schema(description = "更新者")
    private Long updatedBy;

    public ApplicationDetailVO() {}

    public ApplicationDetailVO(Long id, String appName, String appDescription, 
                              String clientId, String clientSecret, String redirectUri,
                              String grantTypes, String scopes, Integer status,
                              LocalDateTime createTime, LocalDateTime updateTime,
                              Long createdBy, Long updatedBy) {
        this.id = id;
        this.appName = appName;
        this.appDescription = appDescription;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.grantTypes = grantTypes;
        this.scopes = scopes;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }
}