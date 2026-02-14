package com.qoobot.openidaas.core.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.qoobot.openidaas.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 应用领域模型
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("applications")
public class Application extends BaseEntity {

    /**
     * 应用名称
     */
    @TableField("app_name")
    private String appName;

    /**
     * 应用描述
     */
    @TableField("app_description")
    private String appDescription;

    /**
     * 客户端ID
     */
    @TableField("client_id")
    private String clientId;

    /**
     * 客户端密钥
     */
    @TableField("client_secret")
    private String clientSecret;

    /**
     * 回调地址
     */
    @TableField("redirect_uri")
    private String redirectUri;

    /**
     * 授权类型（多个用逗号分隔）
     */
    @TableField("grant_types")
    private String grantTypes;

    /**
     * 作用域（多个用逗号分隔）
     */
    @TableField("scopes")
    private String scopes;

    /**
     * 应用图标URL
     */
    @TableField("icon_url")
    private String iconUrl;

    /**
     * 应用主页URL
     */
    @TableField("home_url")
    private String homeUrl;

    /**
     * 应用类型
     */
    @TableField("app_type")
    private String appType;

    /**
     * 是否启用
     */
    @TableField("enabled")
    private Boolean enabled = true;

    /**
     * 应用状态（审核中、已上线、已下线等）
     */
    @TableField("status")
    private String status;

    /**
     * 审核时间
     */
    @TableField("audit_time")
    private LocalDateTime auditTime;

    /**
     * 审核人
     */
    @TableField("auditor")
    private Long auditor;

    /**
     * 审核意见
     */
    @TableField("audit_opinion")
    private String auditOpinion;

    /**
     * 应用拥有的权限集合
     */
    @TableField(exist = false)
    private Set<Permission> permissions;

    /**
     * 应用所属租户
     */
    @TableField(exist = false)
    private Tenant tenant;
}
