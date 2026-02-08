package com.qoobot.openidaas.user.entity;

import com.qoobot.openidaas.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 用户角色关联实体
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Entity
@Table(name = "user_roles", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "role_id"})
    })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRole extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "assigned_by")
    private Long assignedBy;

    @Column(name = "assigned_at")
    private java.time.LocalDateTime assignedAt;

    @Column(name = "expires_at")
    private java.time.LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) {
            assignedAt = java.time.LocalDateTime.now();
        }
    }
}
