package com.qoobot.openidaas.organization.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.qoobot.openidaas.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 组织实体类
 *
 * @author Qoobot
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_organization")
public class Organization extends BaseEntity {

    /**
     * 组织名称
     */
    private String name;

    /**
     * 组织编码
     */
    private String code;

    /**
     * 组织类型
     */
    private String type;

    /**
     * 父组织ID
     */
    private Long parentId;

    /**
     * 组织层级
     */
    private Integer level;

    /**
     * 组织路径
     */
    private String path;

    /**
     * 描述
     */
    private String description;

    /**
     * 负责人ID
     */
    private Long managerId;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 联系邮箱
     */
    private String email;

    /**
     * 状态
     */
    private String status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否叶子节点
     */
    @TableField(exist = false)
    private Boolean leaf;

    /**
     * 子组织数量
     */
    @TableField(exist = false)
    private Integer childrenCount;

    /**
     * 子组织列表
     */
    @TableField(exist = false)
    private List<Organization> children;

}