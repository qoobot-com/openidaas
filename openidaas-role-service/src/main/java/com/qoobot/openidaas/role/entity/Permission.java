package com.qoobot.openidaas.role.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qoobot.openidaas.role.enumeration.PermissionType;
import lombok.Data;

/**
 * 权限实体类
 *
 * @author QooBot
 */
@Data
@TableName("permissions")
public class Permission {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 权限编码
     */
    private String permCode;

    /**
     * 权限名称
     */
    private String permName;

    /**
     * 权限类型：menu, button, api
     */
    private String permType;

    /**
     * 权限路径
     */
    private String path;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 权限描述
     */
    private String description;

    /**
     * 父权限ID
     */
    private Long parentId;

    /**
     * 图标
     */
    private String icon;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 是否外链
     */
    private Integer externalLink;

    /**
     * 是否隐藏
     */
    private Integer hidden;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 是否启用
     */
    private Integer enabled;

    /**
     * 是否内置权限
     */
    private Integer isBuiltin;

    /**
     * 子权限列表（不映射到数据库）
     */
    @TableField(exist = false)
    private java.util.List<Permission> children;

    /**
     * 获取权限类型
     */
    public PermissionType getPermissionType() {
        return PermissionType.fromCode(this.permType);
    }

    /**
     * 设置权限类型
     */
    public void setPermissionType(PermissionType permissionType) {
        this.permType = permissionType.getCode();
    }

    /**
     * 是否外链
     */
    public boolean isExternalLink() {
        return Integer.valueOf(1).equals(this.externalLink);
    }

    /**
     * 是否隐藏
     */
    public boolean isHidden() {
        return Integer.valueOf(1).equals(this.hidden);
    }

    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return Integer.valueOf(1).equals(this.enabled);
    }

    /**
     * 是否内置权限
     */
    public boolean isBuiltin() {
        return Integer.valueOf(1).equals(this.isBuiltin);
    }
}
