package com.qoobot.openidaas.user.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 更新用户请求DTO
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
public class UpdateUserRequest {

    @Size(max = 100, message = "姓名长度不能超过100")
    private String fullname;

    @Size(max = 100, message = "昵称长度不能超过100")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private Long departmentId;

    private Long positionId;

    @Size(max = 50, message = "员工ID长度不能超过50")
    private String employeeId;

    private String jobTitle;

    private String workLocation;

    private Long managerId;

    private List<Long> roleIds;

    private Boolean mustChangePassword;

    private Boolean mfaEnabled;
}
