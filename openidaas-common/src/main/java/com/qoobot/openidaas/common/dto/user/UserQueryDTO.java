package com.qoobot.openidaas.common.dto.user;

import com.qoobot.openidaas.common.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询DTO
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户查询请求")
public class UserQueryDTO extends BaseDTO {

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

    @Schema(description = "昵称", example = "管理员")
    private String nickname;

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "状态（1-正常，2-锁定，3-禁用，4-过期）", example = "1")
    private Integer status;

    @Schema(description = "部门ID", example = "1")
    private Long deptId;

    @Schema(description = "角色ID", example = "1")
    private Long roleId;

    @Schema(description = "租户ID", example = "1")
    private Long tenantId;
}