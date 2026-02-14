package com.qoobot.openidaas.core.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.qoobot.openidaas.common.entity.BaseEntity;
import com.qoobot.openidaas.common.enumeration.UserStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户领域模型
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class User extends BaseEntity {

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 手机号
     */
    @TableField("mobile")
    private String mobile;

    /**
     * 密码（加密存储）
     */
    @TableField("password")
    private String password;

    /**
     * 昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 性别（0-未知，1-男，2-女）
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 生日
     */
    @TableField("birthday")
    private LocalDateTime birthday;

    /**
     * 状态
     */
    @TableField("status")
    private UserStatusEnum status;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /**
     * 登录失败次数
     */
    @TableField("login_fail_count")
    private Integer loginFailCount = 0;

    /**
     * 账户锁定时间
     */
    @TableField("lock_time")
    private LocalDateTime lockTime;

    /**
     * 密码过期时间
     */
    @TableField("pwd_expire_time")
    private LocalDateTime pwdExpireTime;

    /**
     * 是否需要重置密码
     */
    @TableField("pwd_reset_required")
    private Boolean pwdResetRequired = false;

    /**
     * 密码重置时间
     */
    @TableField("pwd_reset_time")
    private LocalDateTime pwdResetTime;

    /**
     * 密码重置操作人
     */
    @TableField("pwd_reset_by")
    private Long pwdResetBy;

    /**
     * 用户拥有的角色集合（非数据库字段，需要手动加载）
     */
    @TableField(exist = false)
    private Set<Role> roles;

    /**
     * 用户所属部门集合（非数据库字段，需要手动加载）
     */
    @TableField(exist = false)
    private Set<Department> departments;

    /**
     * 用户拥有的权限集合（非数据库字段，需要手动加载）
     */
    @TableField(exist = false)
    private Set<Permission> permissions;

    /**
     * 用户所属租户（非数据库字段，需要手动加载）
     */
    @TableField(exist = false)
    private Tenant tenant;

    /**
     * 兼容方法：获取启用状态
     */
    public Boolean getEnabled() {
        return this.status == UserStatusEnum.ACTIVE || this.status == UserStatusEnum.NORMAL;
    }

    /**
     * 兼容方法：设置启用状态
     */
    public void setEnabled(Boolean enabled) {
        this.status = enabled ? UserStatusEnum.ACTIVE : UserStatusEnum.DISABLED;
    }
}