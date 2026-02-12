package com.qoobot.openidaas.core.domain;

import com.qoobot.openidaas.common.entity.BaseEntity;
import com.qoobot.openidaas.common.enumeration.UserStatusEnum;
import jakarta.persistence.*;
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
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    /**
     * 用户名
     */
    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    /**
     * 邮箱
     */
    @Column(name = "email", length = 100, unique = true)
    private String email;

    /**
     * 手机号
     */
    @Column(name = "mobile", length = 20, unique = true)
    private String mobile;

    /**
     * 密码（加密存储）
     */
    @Column(name = "password", length = 255)
    private String password;

    /**
     * 昵称
     */
    @Column(name = "nickname", length = 50)
    private String nickname;

    /**
     * 真实姓名
     */
    @Column(name = "real_name", length = 50)
    private String realName;

    /**
     * 头像URL
     */
    @Column(name = "avatar", length = 255)
    private String avatar;

    /**
     * 性别（0-未知，1-男，2-女）
     */
    @Column(name = "gender")
    private Integer gender;

    /**
     * 生日
     */
    @Column(name = "birthday")
    private LocalDateTime birthday;

    /**
     * 状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private UserStatusEnum status;

    /**
     * 最后登录时间
     */
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;

    /**
     * 登录失败次数
     */
    @Column(name = "login_fail_count")
    private Integer loginFailCount = 0;

    /**
     * 账户锁定时间
     */
    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    /**
     * 密码过期时间
     */
    @Column(name = "pwd_expire_time")
    private LocalDateTime pwdExpireTime;

    /**
     * 是否需要重置密码
     */
    @Column(name = "pwd_reset_required")
    private Boolean pwdResetRequired = false;

    /**
     * 密码重置时间
     */
    @Column(name = "pwd_reset_time")
    private LocalDateTime pwdResetTime;

    /**
     * 密码重置操作人
     */
    @Column(name = "pwd_reset_by")
    private Long pwdResetBy;

    /**
     * 用户拥有的角色集合
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    /**
     * 用户所属部门集合
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_departments",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "dept_id")
    )
    private Set<Department> departments;

    /**
     * 用户拥有的权限集合
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_permissions",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "perm_id")
    )
    private Set<Permission> permissions;

    /**
     * 用户所属租户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
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