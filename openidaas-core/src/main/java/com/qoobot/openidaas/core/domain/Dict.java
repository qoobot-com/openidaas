package com.qoobot.openidaas.core.domain;

import com.qoobot.openidaas.common.entity.BaseEntity;
import jakarta.persistence.*;
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
@Entity
@Table(name = "dicts")
public class Dict extends BaseEntity {

    /**
     * 字典名称
     */
    @Column(name = "dict_name", length = 100, nullable = false)
    private String dictName;

    /**
     * 字典编码
     */
    @Column(name = "dict_code", length = 50, nullable = false, unique = true)
    private String dictCode;

    /**
     * 字典描述
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
     * 字典项集合
     */
    @OneToMany(mappedBy = "dict", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<DictItem> dictItems;
}