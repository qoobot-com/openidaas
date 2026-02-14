package com.qoobot.openidaas.core.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qoobot.openidaas.common.entity.BaseEntity;
import com.qoobot.openidaas.common.enumeration.RoleTypeEnum;
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
@TableName("roles")
public class Role extends BaseEntity {

    /**
     * 角色编码
     */
    @TableField("role_code")
    private String roleCode;

    /**
     * 角色名称
     */
    @TableField("role_name")
    private String roleName;

    /**
     * 角色描述
     */
    @TableField("description")
    private String description;

    /**
     * 角色类型
     */
    @TableField("role_type")
    private RoleTypeEnum roleType;

    /**
     * 是否启用
     */
    @TableField("enabled")
    private Boolean enabled = true;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder = 0;

    /**
     * 角色拥有的权限集合（非数据库字段）
     */
    @TableField(exist = false)
    private Set<Permission> permissions;

    /**
     * 拥有此角色的用户集合（非数据库字段）
     */
    @TableField(exist = false)
    private Set<User> users;

    /**
     * 角色关联的数据权限（非数据库字段）
     */
    @TableField(exist = false)
    private Set<DataPermission> dataPermissions;

    /**
     * 角色所属租户（非数据库字段）
     */
    @TableField(exist = false)
    private Tenant tenant;
}
