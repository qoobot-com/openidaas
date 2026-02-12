package com.qoobot.openidaas.core.domain;

import com.qoobot.openidaas.common.entity.BaseEntity;
import com.qoobot.openidaas.common.enumeration.RoleTypeEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

/**
 * 角色领域模型
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    /**
     * 角色编码
     */
    @Column(name = "role_code", length = 64, nullable = false, unique = true)
    private String roleCode;

    /**
     * 角色名称
     */
    @Column(name = "role_name", length = 100, nullable = false)
    private String roleName;

    /**
     * 角色描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 角色类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", length = 20)
    private RoleTypeEnum roleType;

    /**
     * 是否启用
     */
    @Column(name = "enabled")
    private Boolean enabled = true;

    /**
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 角色拥有的权限集合
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "perm_id")
    )
    private Set<Permission> permissions;

    /**
     * 拥有此角色的用户集合
     */
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<User> users;

    /**
     * 角色关联的数据权限
     */
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DataPermission> dataPermissions;

    /**
     * 角色所属租户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;
}