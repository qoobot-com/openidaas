package com.qoobot.openidaas.core.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.qoobot.openidaas.common.entity.BaseEntity;
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
@TableName("tenants")
public class Tenant extends BaseEntity {

    /**
     * 租户名称
     */
    @TableField("tenant_name")
    private String tenantName;

    /**
     * 租户编码
     */
    @TableField("tenant_code")
    private String tenantCode;

    /**
     * 租户描述
     */
    @TableField("description")
    private String description;

    /**
     * 联系人
     */
    @TableField("contact_person")
    private String contactPerson;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @TableField("contact_email")
    private String contactEmail;

    /**
     * 地址
     */
    @TableField("address")
    private String address;

    /**
     * 域名
     */
    @TableField("domain")
    private String domain;

    /**
     * Logo URL
     */
    @TableField("logo_url")
    private String logoUrl;

    /**
     * 是否启用
     */
    @TableField("enabled")
    private Boolean enabled = true;

    /**
     * 租户状态
     */
    @TableField("status")
    private String status;

    /**
     * 过期时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 最大用户数
     */
    @TableField("max_users")
    private Integer maxUsers;

    /**
     * 已使用用户数
     */
    @TableField("used_users")
    private Integer usedUsers = 0;

    /**
     * 最大应用数
     */
    @TableField("max_apps")
    private Integer maxApps;

    /**
     * 已使用应用数
     */
    @TableField("used_apps")
    private Integer usedApps = 0;

    /**
     * 配置信息（JSON格式）
     */
    @TableField("config")
    private String config;
}
