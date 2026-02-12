package com.qoobot.openidaas.common.vo.permission;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限信息VO
 *
 * @author QooBot
 */
@Data
@Schema(description = "权限信息")
public class PermissionVO {

    @Schema(description = "权限ID", example = "1")
    private Long id;

    @Schema(description = "权限编码", example = "user:list")
    private String permCode;

    @Schema(description = "权限名称", example = "用户列表")
    private String permName;

    @Schema(description = "权限类型（menu-菜单，button-按钮，api-API接口）", example = "menu")
    private String permType;

    @Schema(description = "权限类型描述", example = "菜单")
    private String permTypeDesc;

    @Schema(description = "权限路径", example = "/user/list")
    private String path;

    @Schema(description = "请求方法", example = "GET")
    private String method;

    @Schema(description = "权限描述", example = "查看用户列表权限")
    private String description;

    @Schema(description = "父权限ID", example = "0")
    private Long parentId;

    @Schema(description = "父权限名称", example = "用户管理")
    private String parentName;

    @Schema(description = "图标", example = "user")
    private String icon;

    @Schema(description = "组件路径", example = "UserList")
    private String component;

    @Schema(description = "是否外链", example = "false")
    private Boolean externalLink;

    @Schema(description = "是否隐藏", example = "false")
    private Boolean hidden;

    @Schema(description = "排序", example = "1")
    private Integer sortOrder;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "租户ID", example = "1")
    private Long tenantId;

    @Schema(description = "子权限列表")
    private List<PermissionVO> children;

    @Schema(description = "关联的角色数量", example = "5")
    private Long roleCount;
}