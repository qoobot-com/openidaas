package com.qoobot.openidaas.core.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.qoobot.openidaas.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

/**
 * 部门领域模型
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("departments")
public class Department extends BaseEntity {

    /**
     * 部门编码
     */
    @TableField("dept_code")
    private String deptCode;

    /**
     * 部门名称
     */
    @TableField("dept_name")
    private String deptName;

    /**
     * 部门描述
     */
    @TableField("description")
    private String description;

    /**
     * 父部门ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 部门负责人ID
     */
    @TableField("leader_id")
    private Long leaderId;

    /**
     * 联系电话
     */
    @TableField("phone")
    private String phone;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

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
     * 层级路径（用于快速查询，格式：/1/2/3/）
     */
    @TableField("tree_path")
    private String treePath;

    /**
     * 子部门集合
     */
    @TableField(exist = false)
    private Set<Department> children;

    /**
     * 父部门
     */
    @TableField(exist = false)
    private Department parent;

    /**
     * 部门下的用户集合
     */
    @TableField(exist = false)
    private Set<User> users;

    /**
     * 部门负责人
     */
    @TableField(exist = false)
    private User leader;

    /**
     * 部门所属租户
     */
    @TableField(exist = false)
    private Tenant tenant;
}
