package com.qoobot.openidaas.core.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.qoobot.openidaas.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典项领域模型
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dict_items")
public class DictItem extends BaseEntity {

    /**
     * 字典项标签
     */
    @TableField("item_label")
    private String itemLabel;

    /**
     * 字典项值
     */
    @TableField("item_value")
    private String itemValue;

    /**
     * 字典项描述
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
     * 所属字典
     */
    @TableField(exist = false)
    private Dict dict;
}
