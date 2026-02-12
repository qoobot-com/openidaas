package com.qoobot.openidaas.application.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.qoobot.openidaas.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * OAuth2客户端实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oauth2_clients")
public class OAuth2Client extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 客户端密钥（加密存储）
     */
    private String clientSecret;

    /**
     * 应用ID
     */
    private Long appId;

    /**
     * 授权类型列表（JSON数组）
     */
    private String grantTypes;

    /**
     * 权限范围（JSON数组）
     */
    private String scopes;

    /**
     * 访问令牌有效期（秒）
     */
    private Integer accessTokenValidity;

    /**
     * 刷新令牌有效期（秒）
     */
    private Integer refreshTokenValidity;

    /**
     * 是否自动批准
     */
    private Boolean autoApprove;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
