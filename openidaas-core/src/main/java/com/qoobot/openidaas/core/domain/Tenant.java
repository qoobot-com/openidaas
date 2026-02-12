package com.qoobot.openidaas.core.domain;

import com.qoobot.openidaas.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 租户领域模型
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tenants")
public class Tenant extends BaseEntity {

    /**
     * 租户名称
     */
    @Column(name = "tenant_name", length = 100, nullable = false)
    private String tenantName;

    /**
     * 租户编码
     */
    @Column(name = "tenant_code", length = 50, nullable = false, unique = true)
    private String tenantCode;

    /**
     * 租户描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 联系人
     */
    @Column(name = "contact_person", length = 50)
    private String contactPerson;

    /**
     * 联系电话
     */
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    /**
     * 地址
     */
    @Column(name = "address", length = 200)
    private String address;

    /**
     * 域名
     */
    @Column(name = "domain", length = 100, unique = true)
    private String domain;

    /**
     * Logo URL
     */
    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    /**
     * 是否启用
     */
    @Column(name = "enabled")
    private Boolean enabled = true;

    /**
     * 租户状态
     */
    @Column(name = "status", length = 20)
    private String status;

    /**
     * 过期时间
     */
    @Column(name = "expire_time")
    private LocalDateTime expireTime;

    /**
     * 最大用户数
     */
    @Column(name = "max_users")
    private Integer maxUsers;

    /**
     * 已使用用户数
     */
    @Column(name = "used_users")
    private Integer usedUsers = 0;

    /**
     * 最大应用数
     */
    @Column(name = "max_apps")
    private Integer maxApps;

    /**
     * 已使用应用数
     */
    @Column(name = "used_apps")
    private Integer usedApps = 0;

    /**
     * 配置信息（JSON格式）
     */
    @Column(name = "config", columnDefinition = "TEXT")
    private String config;
}