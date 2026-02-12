package com.qoobot.openidaas.role.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qoobot.openidaas.role.enumeration.ScopeType;
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
     * 获取作用域类型
     */
    public ScopeType getScopeTypeEnum() {
        return ScopeType.fromCode(this.scopeType);
    }

    /**
     * 设置作用域类型
     */
    public void setScopeTypeEnum(ScopeType scopeType) {
        this.scopeType = scopeType.getCode();
    }

    /**
     * 是否临时授权
     */
    public boolean isTemporary() {
        return Integer.valueOf(1).equals(this.isTemporary);
    }

    /**
     * 是否已过期
     */
    public boolean isExpired() {
        return expireTime != null && expireTime.isBefore(LocalDateTime.now());
    }
}
