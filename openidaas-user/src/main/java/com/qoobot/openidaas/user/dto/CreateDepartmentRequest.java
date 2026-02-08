package com.qoobot.openidaas.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建部门请求DTO
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
public class CreateDepartmentRequest {

    @NotBlank(message = "部门名称不能为空")
    @Size(min = 1, max = 50, message = "部门名称长度必须在1-50之间")
    private String name;

    @Size(max = 50, message = "部门编码长度不能超过50")
    private String code;

    @Size(max = 500, message = "部门描述长度不能超过500")
    private String description;

    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    private Long parentId;

    private Integer level;

    private Integer sortOrder = 0;

    private Long leaderId;

    @Size(max = 50, message = "负责人姓名长度不能超过50")
    private String leaderName;

    @Size(max = 20, message = "联系电话长度不能超过20")
    private String phone;

    @Size(max = 100, message = "邮箱长度不能超过100")
    private String email;

    @Size(max = 500, message = "地址长度不能超过500")
    private String address;

    @Size(max = 100, message = "外部ID长度不能超过100")
    private String externalId;

    @Size(max = 50, message = "同步来源长度不能超过50")
    private String syncSource;
}