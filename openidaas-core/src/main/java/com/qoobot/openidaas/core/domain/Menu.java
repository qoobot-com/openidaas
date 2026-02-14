package com.qoobot.openidaas.core.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.qoobot.openidaas.common.entity.BaseEntity;
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
@TableName("menus")
public class Menu extends BaseEntity {

    /**
     * 菜单名称
     */
    @TableField("menu_name")
    private String menuName;

    /**
     * 菜单标题
     */
    @TableField("menu_title")
    private String menuTitle;

    /**
     * 菜单路径
     */
    @TableField("menu_path")
    private String menuPath;

    /**
     * 组件路径
     */
    @TableField("component")
    private String component;

    /**
     * 菜单图标
     */
    @TableField("menu_icon")
    private String menuIcon;

    /**
     * 父菜单ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder = 0;

    /**
     * 菜单类型（目录/菜单/按钮）
     */
    @TableField("menu_type")
    private String menuType;

    /**
     * 权限标识
     */
    @TableField("permission")
    private String permission;

    /**
     * 是否外链
     */
    @TableField("external_link")
    private Boolean externalLink = false;

    /**
     * 外链地址
     */
    @TableField("link_url")
    private String linkUrl;

    /**
     * 是否缓存
     */
    @TableField("keep_alive")
    private Boolean keepAlive = false;

    /**
     * 是否隐藏
     */
    @TableField("hidden")
    private Boolean hidden = false;

    /**
     * 是否启用
     */
    @TableField("enabled")
    private Boolean enabled = true;

    /**
     * 菜单描述
     */
    @TableField("description")
    private String description;

    /**
     * 子菜单集合
     */
    @TableField(exist = false)
    private Set<Menu> children;

    /**
     * 父菜单
     */
    @TableField(exist = false)
    private Menu parentMenu;

    /**
     * 关联的权限
     */
    @TableField(exist = false)
    private Permission permissionObj;
}
