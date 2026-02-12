package com.qoobot.openidaas.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 组织更新DTO
 *
 * @author Qoobot
 * @version 1.0.0
 */
@Data
@Schema(description = "组织更新DTO")
public class OrganizationUpdateDTO {

    @NotNull(message = "组织ID不能为空")
    @Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @NotBlank(message = "组织名称不能为空")
    @Size(max = 100, message = "组织名称长度不能超过100个字符")
    @Schema(description = "组织名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 50, message = "组织类型长度不能超过50个字符")
    @Schema(description = "组织类型")
    private String type;

    @Schema(description = "父组织ID")
    private Long parentId;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    @Schema(description = "描述")
    private String description;

    @Schema(description = "负责人ID")
    private Long managerId;

    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    @Schema(description = "联系电话")
    private String phone;

    @Size(max = 100, message = "联系邮箱长度不能超过100个字符")
    @Schema(description = "联系邮箱")
    private String email;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "排序")
    private Integer sort;

}