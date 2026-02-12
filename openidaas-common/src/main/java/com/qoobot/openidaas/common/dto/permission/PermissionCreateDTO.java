package com.qoobot.openidaas.common.dto.permission;

import com.qoobot.openidaas.common.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限创建DTO
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "权限创建请求")
public class PermissionCreateDTO extends BaseDTO {

    @NotBlank(message = "权限编码不能为空")
    @Schema(description = "权限编码", example = "user:list")
    private String permCode;

    @NotBlank(message = "权限名称不能为空")
    @Schema(description = "权限名称", example = "用户列表")
    private String permName;

    @NotBlank(message = "权限类型不能为空")
    @Schema(description = "权限类型（menu-菜单，button-按钮，api-API接口）", example = "menu")
    private String permType;

    @Schema(description = "权限路径", example = "/user/list")
    private String path;

    @Schema(description = "请求方法", example = "GET")
    private String method;

    @Schema(description = "权限描述", example = "查看用户列表权限")
    private String description;

    @Schema(description = "父权限ID", example = "0")
    private Long parentId = 0L;

    @Schema(description = "图标", example = "user")
    private String icon;

    @Schema(description = "组件路径", example = "UserList")
    private String component;

    @Schema(description = "是否外链", example = "false")
    private Boolean externalLink = false;

    @Schema(description = "是否隐藏", example = "false")
    private Boolean hidden = false;

    @Schema(description = "排序", example = "1")
    private Integer sortOrder = 0;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled = true;

    @Schema(description = "租户ID", example = "1")
    private Long tenantId;
}