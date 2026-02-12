package com.qoobot.openidaas.core.domain;

import com.qoobot.openidaas.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

/**
 * 菜单领域模型
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "menus")
public class Menu extends BaseEntity {

    /**
     * 菜单名称
     */
    @Column(name = "menu_name", length = 50, nullable = false)
    private String menuName;

    /**
     * 菜单标题
     */
    @Column(name = "menu_title", length = 50)
    private String menuTitle;

    /**
     * 菜单路径
     */
    @Column(name = "menu_path", length = 200)
    private String menuPath;

    /**
     * 组件路径
     */
    @Column(name = "component", length = 200)
    private String component;

    /**
     * 菜单图标
     */
    @Column(name = "menu_icon", length = 50)
    private String menuIcon;

    /**
     * 父菜单ID
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 菜单类型（目录/菜单/按钮）
     */
    @Column(name = "menu_type", length = 20)
    private String menuType;

    /**
     * 权限标识
     */
    @Column(name = "permission", length = 100)
    private String permission;

    /**
     * 是否外链
     */
    @Column(name = "external_link")
    private Boolean externalLink = false;

    /**
     * 外链地址
     */
    @Column(name = "link_url", length = 200)
    private String linkUrl;

    /**
     * 是否缓存
     */
    @Column(name = "keep_alive")
    private Boolean keepAlive = false;

    /**
     * 是否隐藏
     */
    @Column(name = "hidden")
    private Boolean hidden = false;

    /**
     * 是否启用
     */
    @Column(name = "enabled")
    private Boolean enabled = true;

    /**
     * 菜单描述
     */
    @Column(name = "description", length = 200)
    private String description;

    /**
     * 子菜单集合
     */
    @OneToMany(mappedBy = "parentMenu", fetch = FetchType.LAZY)
    private Set<Menu> children;

    /**
     * 父菜单
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Menu parentMenu;

    /**
     * 关联的权限
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id")
    private Permission permissionObj;
}