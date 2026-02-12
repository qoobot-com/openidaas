package com.qoobot.openidaas.core.domain;

import com.qoobot.openidaas.common.entity.BaseEntity;
import jakarta.persistence.*;
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
@Entity
@Table(name = "applications")
public class Application extends BaseEntity {

    /**
     * 应用名称
     */
    @Column(name = "app_name", length = 100, nullable = false)
    private String appName;

    /**
     * 应用描述
     */
    @Column(name = "app_description", length = 500)
    private String appDescription;

    /**
     * 客户端ID
     */
    @Column(name = "client_id", length = 100, nullable = false, unique = true)
    private String clientId;

    /**
     * 客户端密钥
     */
    @Column(name = "client_secret", length = 255, nullable = false)
    private String clientSecret;

    /**
     * 回调地址
     */
    @Column(name = "redirect_uri", length = 500)
    private String redirectUri;

    /**
     * 授权类型（多个用逗号分隔）
     */
    @Column(name = "grant_types", length = 200)
    private String grantTypes;

    /**
     * 作用域（多个用逗号分隔）
     */
    @Column(name = "scopes", length = 200)
    private String scopes;

    /**
     * 应用图标URL
     */
    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    /**
     * 应用主页URL
     */
    @Column(name = "home_url", length = 255)
    private String homeUrl;

    /**
     * 应用类型
     */
    @Column(name = "app_type", length = 50)
    private String appType;

    /**
     * 是否启用
     */
    @Column(name = "enabled")
    private Boolean enabled = true;

    /**
     * 应用状态（审核中、已上线、已下线等）
     */
    @Column(name = "status", length = 20)
    private String status;

    /**
     * 审核时间
     */
    @Column(name = "audit_time")
    private LocalDateTime auditTime;

    /**
     * 审核人
     */
    @Column(name = "auditor")
    private Long auditor;

    /**
     * 审核意见
     */
    @Column(name = "audit_opinion", length = 500)
    private String auditOpinion;

    /**
     * 应用拥有的权限集合
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "app_permissions",
        joinColumns = @JoinColumn(name = "app_id"),
        inverseJoinColumns = @JoinColumn(name = "perm_id")
    )
    private Set<Permission> permissions;

    /**
     * 应用所属租户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;
}