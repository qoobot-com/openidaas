package com.qoobot.openidaas.core.domain;

import com.qoobot.openidaas.common.entity.BaseEntity;
import com.qoobot.openidaas.common.enumeration.DataScopeEnum;
import jakarta.persistence.*;
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
@Entity
@Table(name = "data_permissions")
public class DataPermission extends BaseEntity {

    /**
     * 权限编码
     */
    @Column(name = "perm_code", length = 128, nullable = false)
    private String permCode;

    /**
     * 数据范围
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "data_scope", length = 20)
    private DataScopeEnum dataScope;

    /**
     * 自定义部门ID集合（逗号分隔）
     */
    @Column(name = "custom_dept_ids", length = 1000)
    private String customDeptIds;

    /**
     * 角色ID
     */
    @Column(name = "role_id")
    private Long roleId;

    /**
     * 关联的角色
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private Role role;

    /**
     * 自定义部门集合
     */
    @Transient
    private Set<Department> customDepartments;
}