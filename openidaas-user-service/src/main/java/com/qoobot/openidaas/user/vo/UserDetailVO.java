package com.qoobot.openidaas.user.vo;

import com.qoobot.openidaas.common.vo.role.RoleVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户详细信息VO
 *
 * @author QooBot
 */
@Data
@Schema(description = "用户详细信息")
public class UserDetailVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "状态")
    private Integer status;

    // 部门信息
    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "部门编码")
    private String deptCode;

    // 职位信息
    @Schema(description = "职位ID")
    private Long positionId;

    @Schema(description = "职位名称")
    private String positionName;

    @Schema(description = "职位编码")
    private String positionCode;

    // 角色信息
    @Schema(description = "用户角色列表")
    private List<RoleVO> roles;

    @Schema(description = "角色名称列表")
    private List<String> roleNames;
}