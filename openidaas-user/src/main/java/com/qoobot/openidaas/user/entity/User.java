package com.qoobot.openidaas.user.entity;

import com.qoobot.openidaas.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户实体类
 * 
 * 支持用户生命周期管理、组织架构关联、扩展属性等企业级功能
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_phone", columnList = "phone"),
    @Index(name = "idx_tenant_id", columnList = "tenantId"),
    @Index(name = "idx_department_id", columnList = "departmentId"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_fulltext", columnList = "fullname", columnDefinition = "gin")
})
@SQLDelete(sql = "UPDATE users SET deleted = true, deletedAt = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"password", "salt"})
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    private String salt;

    @Column(name = "fullname", length = 100)
    private String fullname;

    @Column(length = 100)
    private String nickname;

    @Column(length = 100)
    @Builder.Default
    private String avatar = "default-avatar.png";

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 50)
    private String countryCode = "+86";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AccountType accountType = AccountType.LOCAL;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "position_id")
    private Long positionId;

    @Column(name = "employee_id", length = 50)
    private String employeeId;

    @Column(name = "hire_date")
    private LocalDateTime hireDate;

    @Column(name = "job_title", length = 100)
    private String jobTitle;

    @Column(name = "work_location", length = 100)
    private String workLocation;

    @Column(name = "manager_id")
    private Long managerId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserRole> userRoles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserAttribute> attributes = new HashSet<>();

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;

    @Column(name = "login_count")
    @Builder.Default
    private Integer loginCount = 0;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "password_expires_at")
    private LocalDateTime passwordExpiresAt;

    @Column(name = "must_change_password")
    @Builder.Default
    private Boolean mustChangePassword = false;

    @Column(name = "mfa_enabled")
    @Builder.Default
    private Boolean mfaEnabled = false;

    @Column(name = "mfa_secret", length = 100)
    private String mfaSecret;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "deletion_reason", length = 500)
    private String deletionReason;

    @Column(name = "created_ip", length = 50)
    private String createdIp;

    @Column(name = "updated_ip", length = 50)
    private String updatedIp;

    @PrePersist
    protected void onCreate() {
        if (passwordChangedAt == null) {
            passwordChangedAt = LocalDateTime.now();
        }
        if (passwordExpiresAt == null) {
            passwordExpiresAt = LocalDateTime.now().plusDays(90);
        }
    }

    /**
     * 检查账户是否被锁定
     */
    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    /**
     * 检查密码是否过期
     */
    public boolean isPasswordExpired() {
        return passwordExpiresAt != null && passwordExpiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * 获取完整的显示名称
     */
    public String getDisplayName() {
        return fullname != null ? fullname : nickname != null ? nickname : username;
    }
}
