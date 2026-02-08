package com.qoobot.openidaas.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

/**
 * 创建角色请求DTO
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
public class CreateRoleRequest {

    @NotBlank(message = "角色名称不能为空")
    @Size(min = 1, max = 64, message = "角色名称长度必须在1-64之间")
    private String name;

    @NotBlank(message = "角色编码不能为空")
    @Size(min = 1, max = 32, message = "角色编码长度必须在1-32之间")
    private String code;

    @Size(max = 500, message = "角色描述长度不能超过500")
    private String description;

    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    private Set<Long> permissionIds;

    @Size(max = 100, message = "外部ID长度不能超过100")
    private String externalId;

    @Size(max = 50, message = "同步来源长度不能超过50")
    private String syncSource;
}