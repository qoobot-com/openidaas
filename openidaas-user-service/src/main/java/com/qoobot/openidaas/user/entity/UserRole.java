package com.qoobot.openidaas.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户角色关联实体类
 *
 * @author QooBot
 */
@Data
@TableName("user_roles")
public class UserRole {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 作用域类型：1-全局，2-部门，3-项目，4-应用
     */
    private Integer scopeType;

    /**
     * 作用域ID
     */
    private Long scopeId;

    /**
     * 授权人ID
     */
    private Long grantedBy;

    /**
     * 授权原因
     */
    private String grantReason;

    /**
     * 授权时间
     */
    private LocalDateTime grantTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 是否临时授权
     */
    private Integer isTemporary;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
