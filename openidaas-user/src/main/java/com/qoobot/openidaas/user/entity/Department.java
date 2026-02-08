package com.qoobot.openidaas.user.entity;

import com.qoobot.openidaas.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;

/**
 * 部门实体类
 * 
 * 支持树形组织架构管理
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Entity
@Table(name = "departments", indexes = {
    @Index(name = "idx_tenant_id", columnList = "tenantId"),
    @Index(name = "idx_parent_id", columnList = "parentId"),
    @Index(name = "idx_code", columnList = "code")
})
@SQLDelete(sql = "UPDATE departments SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"children", "users"})
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 50)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "parent_id")
    private Long parentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Department parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Department> children = new HashSet<>();

    @Column(name = "level")
    @Builder.Default
    private Integer level = 1;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "leader_id")
    private Long leaderId;

    @Column(length = 50)
    private String leaderName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private DepartmentStatus status = DepartmentStatus.ACTIVE;

    @Column(name = "external_id", length = 100)
    private String externalId;

    @Column(name = "sync_source", length = 50)
    private String syncSource;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean deleted = false;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<User> users = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (level == null) {
            level = parentId == null ? 1 : 2;
        }
    }
}