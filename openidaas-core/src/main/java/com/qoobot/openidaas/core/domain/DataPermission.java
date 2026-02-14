package com.qoobot.openidaas.core.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.qoobot.openidaas.common.entity.BaseEntity;
import com.qoobot.openidaas.common.enumeration.DataScopeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

/**
 * 数据权限领域模型
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("data_permissions")
public class DataPermission extends BaseEntity {

    /**
     * 权限编码
     */
    @TableField("perm_code")
    private String permCode;

    /**
     * 数据范围
     */
    @TableField("data_scope")
    private DataScopeEnum dataScope;

    /**
     * 自定义部门ID集合（逗号分隔）
     */
    @TableField("custom_dept_ids")
    private String customDeptIds;

    /**
     * 角色ID
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * 关联的角色
     */
    @TableField(exist = false)
    private Role role;

    /**
     * 自定义部门集合
     */
    @TableField(exist = false)
    private Set<Department> customDepartments;
}
