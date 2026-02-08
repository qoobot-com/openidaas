package com.qoobot.openidaas.user.entity;

import com.qoobot.openidaas.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;

/**
 * 角色实体类
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Entity
@Table(name = "roles", indexes = {
    @Index(name = "idx_tenant_id", columnList = "tenantId"),
    @Index(name = "idx_code", columnList = "code")
})
@SQLDelete(sql = "UPDATE roles SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"permissions", "userRoles"})
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private RoleType type = RoleType.CUSTOM;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private RoleScope scope = RoleScope.GLOBAL;

    @Column(name = "level")
    @Builder.Default
    private Integer level = 0;

    @Column(name = "data_scope")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DataScope dataScope = DataScope.ALL;

    @Column(name = "is_system")
    @Builder.Default
    private Boolean isSystem = false;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean deleted = false;

    @ManyToMany(mappedBy = "roles", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserRole> userRoles = new HashSet<>();
}
