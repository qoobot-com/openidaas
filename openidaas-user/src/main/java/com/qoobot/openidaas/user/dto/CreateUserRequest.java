package com.qoobot.openidaas.user.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 创建用户请求DTO
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
public class CreateUserRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 100, message = "密码长度必须在8-100之间")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", 
             message = "密码必须包含大小写字母和数字")
    private String password;

    @Size(max = 100, message = "姓名长度不能超过100")
    private String fullname;

    @Size(max = 100, message = "昵称长度不能超过100")
    private String nickname;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Size(max = 20, message = "国家代码长度不能超过20")
    private String countryCode = "+86";

    private Long tenantId;

    private Long departmentId;

    private Long positionId;

    @Size(max = 50, message = "员工ID长度不能超过50")
    private String employeeId;

    private String jobTitle;

    private String workLocation;

    private Long managerId;

    private List<Long> roleIds;

    private Boolean sendWelcomeEmail = true;

    private Boolean requirePasswordChange = false;
}
