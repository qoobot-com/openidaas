package com.qoobot.openidaas.organization.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qoobot.openidaas.common.entity.BaseEntity;
import com.qoobot.openidaas.common.enumeration.StatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门实体类
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("departments")
public class Department extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 部门编码（大写字母数字下划线）
     */
    private String deptCode;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 父部门ID，0表示根部门
     */
    private Long parentId;

    /**
     * 层级路径，如：1/2/3
     */
    private String levelPath;

    /**
     * 层级深度
     */
    private Integer levelDepth;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 部门经理ID
     */
    private Long managerId;

    /**
     * 部门描述
     */
    private String description;

    /**
     * 状态：1-启用，2-禁用
     */
    private Integer status;

    /**
     * 子部门列表（不映射到数据库）
     */
    @TableField(exist = false)
    private java.util.List<Department> children;

    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return StatusEnum.ENABLED.getCode().equals(this.status);
    }
}
