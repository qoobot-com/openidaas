package com.qoobot.openidaas.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qoobot.openidaas.common.entity.BaseEntity;
import com.qoobot.openidaas.common.enumeration.UserStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class User extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（3-20位字母数字下划线）
     */
    private String username;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 密码哈希值（BCrypt）
     */
    private String passwordHash;

    /**
     * 密码盐值
     */
    private String passwordSalt;

    /**
     * 密码最后更新时间
     */
    private LocalDateTime passwordUpdatedAt;

    /**
     * 账户状态：1-正常，2-锁定，3-停用，4-删除
     */
    private Integer status;

    /**
     * 连续登录失败次数
     */
    private Integer failedLoginAttempts;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 是否需要重置密码：0-否，1-是
     */
    private Integer pwdResetRequired;

    /**
     * 密码重置时间
     */
    private LocalDateTime pwdResetTime;

    /**
     * 密码重置操作人
     */
    private Long pwdResetBy;

    /**
     * 账户是否锁定
     */
    @TableField(exist = false)
    private boolean locked;

    /**
     * 账户是否可用
     */
    @TableField(exist = false)
    private boolean accountNonExpired;

    /**
     * 凭据是否可用
     */
    @TableField(exist = false)
    private boolean credentialsNonExpired;

    /**
     * 账户是否可用
     */
    @TableField(exist = false)
    private boolean enabled;

    /**
     * 判断用户状态
     */
    public boolean isActive() {
        return UserStatusEnum.ACTIVE.getCode().equals(this.status);
    }

    /**
     * 判断用户是否被锁定
     */
    public boolean isLocked() {
        return UserStatusEnum.LOCKED.getCode().equals(this.status);
    }

    /**
     * 判断用户是否被禁用
     */
    public boolean isDisabled() {
        return UserStatusEnum.DISABLED.getCode().equals(this.status);
    }
}
