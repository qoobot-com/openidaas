package com.qoobot.openidaas.core.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.qoobot.openidaas.common.entity.BaseEntity;
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
@TableName("permissions")
public class Permission extends BaseEntity {

    /**
     * 权限编码
     */
    @TableField("perm_code")
    private String permCode;

    /**
     * 权限名称
     */
    @TableField("perm_name")
    private String permName;

    /**
     * 权限类型（menu-菜单，button-按钮，api-API接口）
     */
    @TableField("perm_type")
    private String permType;

    /**
     * 权限路径（用于前端路由或API路径）
     */
    @TableField("path")
    private String path;

    /**
     * 请求方法（GET,POST,PUT,DELETE等）
     */
    @TableField("method")
    private String method;

    /**
     * 权限描述
     */
    @TableField("description")
    private String description;

    /**
     * 父权限ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 组件路径（前端组件路径）
     */
    @TableField("component")
    private String component;

    /**
     * 是否外链
     */
    @TableField("external_link")
    private Boolean externalLink = false;

    /**
     * 是否隐藏
     */
    @TableField("hidden")
    private Boolean hidden = false;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder = 0;

    /**
     * 是否启用
     */
    @TableField("enabled")
    private Boolean enabled = true;

    /**
     * 拥有此权限的角色集合
     */
    @TableField(exist = false)
    private Set<Role> roles;

    /**
     * 拥有此权限的用户集合
     */
    @TableField(exist = false)
    private Set<User> users;

    /**
     * 子权限集合
     */
    @TableField(exist = false)
    private Set<Permission> children;

    /**
     * 父权限
     */
    @TableField(exist = false)
    private Permission parent;

    /**
     * 关联的菜单
     */
    @TableField(exist = false)
    private Menu menu;

    /**
     * 权限所属租户
     */
    @TableField(exist = false)
    private Tenant tenant;
}
