package com.qoobot.openidaas.core.domain;

import com.qoobot.openidaas.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典项领域模型
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "dict_items")
public class DictItem extends BaseEntity {

    /**
     * 字典项标签
     */
    @Column(name = "item_label", length = 100, nullable = false)
    private String itemLabel;

    /**
     * 字典项值
     */
    @Column(name = "item_value", length = 100, nullable = false)
    private String itemValue;

    /**
     * 字典项描述
     */
    @Column(name = "description", length = 200)
    private String description;

    /**
     * 是否启用
     */
    @Column(name = "enabled")
    private Boolean enabled = true;

    /**
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 所属字典
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dict_id", nullable = false)
    private Dict dict;
}