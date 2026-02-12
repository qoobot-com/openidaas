package com.qoobot.openidaas.core.domain;

import com.qoobot.openidaas.common.entity.BaseEntity;
import jakarta.persistence.*;
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
@Entity
@Table(name = "departments")
public class Department extends BaseEntity {

    /**
     * 部门编码
     */
    @Column(name = "dept_code", length = 64, nullable = false, unique = true)
    private String deptCode;

    /**
     * 部门名称
     */
    @Column(name = "dept_name", length = 100, nullable = false)
    private String deptName;

    /**
     * 部门描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 父部门ID
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 部门负责人ID
     */
    @Column(name = "leader_id")
    private Long leaderId;

    /**
     * 联系电话
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 邮箱
     */
    @Column(name = "email", length = 100)
    private String email;

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
     * 层级路径（用于快速查询，格式：/1/2/3/）
     */
    @Column(name = "tree_path", length = 500)
    private String treePath;

    /**
     * 子部门集合
     */
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private Set<Department> children;

    /**
     * 父部门
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Department parent;

    /**
     * 部门下的用户集合
     */
    @ManyToMany(mappedBy = "departments", fetch = FetchType.LAZY)
    private Set<User> users;

    /**
     * 部门负责人
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", insertable = false, updatable = false)
    private User leader;

    /**
     * 部门所属租户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;
}