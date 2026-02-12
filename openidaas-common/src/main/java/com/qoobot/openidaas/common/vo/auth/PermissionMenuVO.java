package com.qoobot.openidaas.common.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 权限菜单VO
 *
 * @author QooBot
 */
@Data
@Schema(description = "权限菜单")
public class PermissionMenuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单ID")
    private Long menuId;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "菜单路径")
    private String menuPath;

    @Schema(description = "菜单图标")
    private String menuIcon;

    @Schema(description = "父菜单ID")
    private Long parentId;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否显示")
    private Boolean visible;

    @Schema(description = "权限标识")
    private String permission;

    public PermissionMenuVO() {}

    public PermissionMenuVO(Long menuId, String menuName, String menuPath, 
                           String menuIcon, Long parentId, Integer sort, 
                           Boolean visible, String permission) {
        this.menuId = menuId;
        this.menuName = menuName;
        this.menuPath = menuPath;
        this.menuIcon = menuIcon;
        this.parentId = parentId;
        this.sort = sort;
        this.visible = visible;
        this.permission = permission;
    }
}