package com.qoobot.openidaas.common.dto.department;

import com.qoobot.openidaas.common.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门更新DTO
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "部门更新请求")
public class DepartmentUpdateDTO extends BaseDTO {

    @NotNull(message = "部门ID不能为空")
    @Schema(description = "部门ID", example = "1")
    private Long id;

    @NotBlank(message = "部门编码不能为空")
    @Schema(description = "部门编码", example = "DEPT_001")
    private String deptCode;

    @NotBlank(message = "部门名称不能为空")
    @Schema(description = "部门名称", example = "技术部")
    private String deptName;

    @Schema(description = "部门描述", example = "技术研发部门")
    private String description;

    @Schema(description = "父部门ID", example = "0")
    private Long parentId;

    @Schema(description = "部门负责人ID", example = "1")
    private Long leaderId;

    @Schema(description = "联系电话", example = "010-12345678")
    private String phone;

    @Schema(description = "邮箱", example = "tech@example.com")
    private String email;

    @Schema(description = "排序", example = "1")
    private Integer sortOrder;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "租户ID", example = "1")
    private Long tenantId;
}