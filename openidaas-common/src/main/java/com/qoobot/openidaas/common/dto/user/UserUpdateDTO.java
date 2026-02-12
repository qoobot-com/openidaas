package com.qoobot.openidaas.common.dto.user;

import com.qoobot.openidaas.common.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户更新DTO
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户更新请求")
public class UserUpdateDTO extends BaseDTO {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1")
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{2,19}$", message = "用户名格式不正确")
    @Schema(description = "用户名", example = "admin")
    private String username;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

    @Schema(description = "昵称", example = "管理员")
    private String nickname;

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "性别（0-未知，1-男，2-女）", example = "1")
    private Integer gender;

    @Schema(description = "生日")
    private LocalDateTime birthday;

    @Schema(description = "状态（1-正常，2-锁定，3-禁用，4-过期）", example = "1")
    private Integer status;

    @Schema(description = "租户ID", example = "1")
    private Long tenantId;
}