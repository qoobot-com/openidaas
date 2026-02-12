package com.qoobot.openidaas.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户扩展属性实体类
 *
 * @author QooBot
 */
@Data
@TableName("user_attributes")
public class UserAttribute {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 属性键
     */
    private String attrKey;

    /**
     * 属性值
     */
    private String attrValue;

    /**
     * 属性类型：STRING, INTEGER, BOOLEAN, DATE, JSON
     */
    private String attrType;

    /**
     * 是否敏感属性
     */
    private Integer isSensitive;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
