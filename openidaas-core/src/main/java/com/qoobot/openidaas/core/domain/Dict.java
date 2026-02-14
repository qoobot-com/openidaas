package com.qoobot.openidaas.core.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.qoobot.openidaas.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

/**
 * 字典领域模型
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dicts")
public class Dict extends BaseEntity {

    /**
     * 字典名称
     */
    @TableField("dict_name")
    private String dictName;

    /**
     * 字典编码
     */
    @TableField("dict_code")
    private String dictCode;

    /**
     * 字典描述
     */
    @TableField("description")
    private String description;

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
     * 字典项集合
     */
    @TableField(exist = false)
    private Set<DictItem> dictItems;
}
