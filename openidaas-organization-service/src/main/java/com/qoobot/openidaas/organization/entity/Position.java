package com.qoobot.openidaas.organization.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qoobot.openidaas.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 职位实体类
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("positions")
public class Position extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 职位编码（大写字母数字下划线）
     */
    private String positionCode;

    /**
     * 职位名称
     */
    private String positionName;

    /**
     * 所属部门ID
     */
    private Long deptId;

    /**
     * 职级等级
     */
    private Integer level;

    /**
     * 职级
     */
    private String jobGrade;

    /**
     * 汇报对象职位ID
     */
    private Long reportsTo;

    /**
     * 是否管理岗位
     */
    private Integer isManager;

    /**
     * 职位描述
     */
    private String description;
}
