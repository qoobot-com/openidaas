package com.qoobot.openidaas.common.dto.role;

import com.qoobot.openidaas.common.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色查询DTO
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色查询请求")
public class RoleQueryDTO extends BaseDTO {

    @Schema(description = "角色编码", example = "ADMIN")
    private String roleCode;

    @Schema(description = "角色名称", example = "管理员")
    private String roleName;

    @Schema(description = "角色类型（1-系统角色，2-自定义角色，3-默认角色）", example = "1")
    private Integer roleType;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "权限ID", example = "1")
    private Long permissionId;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "租户ID", example = "1")
    private Long tenantId;
}