package com.qoobot.openidaas.common.vo.role;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色信息VO
 *
 * @author QooBot
 */
@Data
@Schema(description = "角色信息")
public class RoleVO {

    @Schema(description = "角色ID", example = "1")
    private Long id;

    @Schema(description = "角色编码", example = "ROLE_ADMIN")
    private String roleCode;

    @Schema(description = "角色名称", example = "管理员")
    private String roleName;

    @Schema(description = "角色类型：1-系统角色，2-自定义角色", example = "2")
    private Integer roleType;

    @Schema(description = "角色类型描述", example = "自定义角色")
    private String roleTypeDesc;

    @Schema(description = "父角色ID", example = "0")
    private Long parentId;

    @Schema(description = "父角色名称", example = "超级管理员")
    private String parentName;

    @Schema(description = "角色描述", example = "系统管理员角色")
    private String description;

    @Schema(description = "是否内置角色", example = "false")
    private Boolean isBuiltin;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "排序", example = "1")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "租户ID", example = "1")
    private Long tenantId;

    @Schema(description = "子角色列表")
    private List<RoleVO> children;

    @Schema(description = "用户数量", example = "10")
    private Long userCount;

    @Schema(description = "权限数量", example = "20")
    private Long permissionCount;
}
