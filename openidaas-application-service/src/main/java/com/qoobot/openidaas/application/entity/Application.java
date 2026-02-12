package com.qoobot.openidaas.application.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.qoobot.openidaas.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 应用实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("applications")
public class Application extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 应用密钥
     */
    private String appKey;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用类型：1-Web，2-移动，3-API
     */
    private Integer appType;

    /**
     * 重定向URI列表（JSON数组）
     */
    private String redirectUris;

    /**
     * Logo URL
     */
    private String logoUrl;

    /**
     * 主页URL
     */
    private String homepageUrl;

    /**
     * 应用描述
     */
    private String description;

    /**
     * 状态：1-启用，2-禁用
     */
    private Integer status;

    /**
     * 所有者ID
     */
    private Long ownerId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
