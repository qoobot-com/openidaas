package com.qoobot.openidaas.common.dto.role;

import com.qoobot.openidaas.common.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色更新DTO
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色更新请求")
public class RoleUpdateDTO extends BaseDTO {

    @NotNull(message = "角色ID不能为空")
    @Schema(description = "角色ID", example = "1")
    private Long id;

    @NotBlank(message = "角色编码不能为空")
    @Schema(description = "角色编码", example = "ROLE_ADMIN")
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    @Schema(description = "角色名称", example = "管理员")
    private String roleName;

    @Schema(description = "角色类型：1-系统角色，2-自定义角色", example = "2")
    private Integer roleType;

    @Schema(description = "父角色ID", example = "0")
    private Long parentId;

    @Schema(description = "角色描述", example = "系统管理员角色")
    private String description;

    @Schema(description = "排序", example = "1")
    private Integer sortOrder;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;
}
