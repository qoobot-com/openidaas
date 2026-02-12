package com.qoobot.openidaas.common.vo.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息VO
 *
 * @author QooBot
 */
@Data
@Schema(description = "用户信息")
public class UserVO {

    @Schema(description = "用户ID", example = "1")
    private Long id;

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

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "性别（0-未知，1-男，2-女）", example = "1")
    private Integer gender;

    @Schema(description = "生日")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime birthday;

    @Schema(description = "状态（1-正常，2-锁定，3-禁用，4-过期）", example = "1")
    private Integer status;

    @Schema(description = "状态描述", example = "正常")
    private String statusDesc;

    @Schema(description = "最后登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    @Schema(description = "最后登录IP", example = "192.168.1.1")
    private String lastLoginIp;

    @Schema(description = "登录失败次数", example = "0")
    private Integer loginFailCount;

    @Schema(description = "账户锁定时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lockTime;

    @Schema(description = "密码过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pwdExpireTime;

    @Schema(description = "是否需要重置密码", example = "false")
    private Boolean pwdResetRequired;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "租户ID", example = "1")
    private Long tenantId;

    @Schema(description = "角色列表")
    private List<RoleVO> roles;

    @Schema(description = "部门列表")
    private List<DepartmentVO> departments;
    
    /**
     * 兼容方法：设置创建时间
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createdAt = createTime;
    }
    
    /**
     * 兼容方法：设置更新时间
     */
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updatedAt = updateTime;
    }

    /**
     * 角色信息VO
     */
    @Data
    @Schema(description = "角色信息")
    public static class RoleVO {
        @Schema(description = "角色ID", example = "1")
        private Long id;

        @Schema(description = "角色编码", example = "ADMIN")
        private String roleCode;

        @Schema(description = "角色名称", example = "管理员")
        private String roleName;

        @Schema(description = "角色描述", example = "系统管理员角色")
        private String description;
    }

    /**
     * 部门信息VO
     */
    @Data
    @Schema(description = "部门信息")
    public static class DepartmentVO {
        @Schema(description = "部门ID", example = "1")
        private Long id;

        @Schema(description = "部门编码", example = "DEPT_001")
        private String deptCode;

        @Schema(description = "部门名称", example = "技术部")
        private String deptName;

        @Schema(description = "部门描述", example = "技术研发部门")
        private String description;
    }
}