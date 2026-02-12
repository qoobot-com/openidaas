package com.qoobot.openidaas.role.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qoobot.openidaas.common.entity.BaseEntity;
import com.qoobot.openidaas.common.enumeration.StatusEnum;
import com.qoobot.openidaas.role.enumeration.RoleType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体类
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("roles")
public class Role extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色类型：1-系统角色，2-自定义角色
     */
    private Integer roleType;

    /**
     * 父角色ID
     */
    private Long parentId;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 是否内置角色
     */
    private Integer isBuiltin;

    /**
     * 状态：1-启用，2-禁用
     */
    private Integer enabled;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 子角色列表（不映射到数据库）
     */
    @TableField(exist = false)
    private java.util.List<Role> children;

    /**
     * 获取角色类型
     */
    public RoleType getRoleTypeEnum() {
        return RoleType.fromCode(this.roleType);
    }

    /**
     * 设置角色类型
     */
    public void setRoleTypeEnum(RoleType roleType) {
        this.roleType = roleType.getCode();
    }

    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return Integer.valueOf(1).equals(this.enabled);
    }

    /**
     * 是否内置角色
     */
    public boolean isBuiltin() {
        return Integer.valueOf(1).equals(this.isBuiltin);
    }
}
