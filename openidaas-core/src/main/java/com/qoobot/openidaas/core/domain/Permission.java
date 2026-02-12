package com.qoobot.openidaas.core.domain;

import com.qoobot.openidaas.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

/**
 * 权限领域模型
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity {

    /**
     * 权限编码
     */
    @Column(name = "perm_code", length = 128, nullable = false, unique = true)
    private String permCode;

    /**
     * 权限名称
     */
    @Column(name = "perm_name", length = 100, nullable = false)
    private String permName;

    /**
     * 权限类型（menu-菜单，button-按钮，api-API接口）
     */
    @Column(name = "perm_type", length = 20)
    private String permType;

    /**
     * 权限路径（用于前端路由或API路径）
     */
    @Column(name = "path", length = 255)
    private String path;

    /**
     * 请求方法（GET,POST,PUT,DELETE等）
     */
    @Column(name = "method", length = 10)
    private String method;

    /**
     * 权限描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 父权限ID
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 图标
     */
    @Column(name = "icon", length = 50)
    private String icon;

    /**
     * 组件路径（前端组件路径）
     */
    @Column(name = "component", length = 255)
    private String component;

    /**
     * 是否外链
     */
    @Column(name = "external_link")
    private Boolean externalLink = false;

    /**
     * 是否隐藏
     */
    @Column(name = "hidden")
    private Boolean hidden = false;

    /**
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 是否启用
     */
    @Column(name = "enabled")
    private Boolean enabled = true;

    /**
     * 拥有此权限的角色集合
     */
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles;

    /**
     * 拥有此权限的用户集合
     */
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<User> users;

    /**
     * 子权限集合
     */
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private Set<Permission> children;

    /**
     * 父权限
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Permission parent;

    /**
     * 关联的菜单
     */
    @OneToOne(mappedBy = "permissionObj", fetch = FetchType.LAZY)
    private Menu menu;

    /**
     * 权限所属租户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;
}