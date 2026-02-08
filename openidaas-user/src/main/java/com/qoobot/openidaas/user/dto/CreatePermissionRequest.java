package com.qoobot.openidaas.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建权限请求DTO
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
public class CreatePermissionRequest {

    @NotBlank(message = "权限编码不能为空")
    @Size(min = 1, max = 64, message = "权限编码长度必须在1-64之间")
    private String code;

    @NotBlank(message = "权限名称不能为空")
    @Size(min = 1, max = 128, message = "权限名称长度必须在1-128之间")
    private String name;

    @NotBlank(message = "资源类型不能为空")
    @Size(min = 1, max = 128, message = "资源类型长度必须在1-128之间")
    private String resource;

    @NotBlank(message = "操作类型不能为空")
    @Size(min = 1, max = 32, message = "操作类型长度必须在1-32之间")
    private String action;

    @Size(max = 500, message = "权限描述长度不能超过500")
    private String description;

    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    private Long parentId;

    @Size(max = 100, message = "图标长度不能超过100")
    private String icon;

    private Integer sortOrder = 0;

    private Boolean isVisible = true;

    @Size(max = 100, message = "外部ID长度不能超过100")
    private String externalId;

    @Size(max = 50, message = "同步来源长度不能超过50")
    private String syncSource;
}