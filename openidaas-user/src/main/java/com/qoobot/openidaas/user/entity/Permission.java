package com.qoobot.openidaas.user.entity;

import com.qoobot.openidaas.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;

/**
 * 权限实体类
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Entity
@Table(name = "permissions", indexes = {
    @Index(name = "idx_code", columnList = "code"),
    @Index(name = "idx_resource_type", columnList = "resourceType")
})
@SQLDelete(sql = "UPDATE permissions SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"roles", "children"})
public class Permission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 200)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "parent_id")
    private Long parentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Permission parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Permission> children = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    @Builder.Default
    private ResourceType resourceType = ResourceType.MENU;

    @Column(name = "resource_path", length = 500)
    private String resourcePath;

    @Column(name = "http_method", length = 20)
    private String httpMethod;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "icon", length = 100)
    private String icon;

    @Column(name = "is_visible")
    @Builder.Default
    private Boolean isVisible = true;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean deleted = false;

    @ManyToMany(mappedBy = "permissions")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
