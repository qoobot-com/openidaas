package com.qoobot.openidaas.application.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.qoobot.openidaas.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * SAML服务提供商实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("saml_service_providers")
public class SamlServiceProvider extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * SP实体ID
     */
    private String spEntityId;

    /**
     * 应用ID
     */
    private Long appId;

    /**
     * 断言消费服务URL
     */
    private String acsUrl;

    /**
     * 证书（PEM格式）
     */
    private String certificate;

    /**
     * 元数据URL
     */
    private String metadataUrl;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
