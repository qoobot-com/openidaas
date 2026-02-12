package com.qoobot.openidaas.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qoobot.openidaas.auth.enumeration.MFAType;
import com.qoobot.openidaas.auth.enumeration.MFAStatus;
import com.qoobot.openidaas.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户MFA认证因子实体
 *
 * @author QooBot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_mfa_factors")
public class UserMFAFactor extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * MFA类型：TOTP, SMS, EMAIL, BACKUP_CODE, HARDWARE_TOKEN, BIOMETRIC
     */
    private String factorType;

    /**
     * MFA名称
     */
    private String factorName;

    /**
     * 密钥（加密存储）
     */
    private String secret;

    /**
     * 备用信息（如手机号、邮箱等）
     */
    private String backupInfo;

    /**
     * 是否为主MFA方式
     */
    private Integer isPrimary;

    /**
     * 状态：1-已配置，2-待验证，3-已禁用，4-已删除
     */
    private Integer status;

    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedAt;

    /**
     * 最后验证失败次数
     */
    private Integer failedAttempts;

    /**
     * 锁定时间
     */
    private LocalDateTime lockedUntil;

    /**
     * 验证次数
     */
    private Long verificationCount;

    /**
     * 是否获取MFA类型
     */
    public MFAType getMFAType() {
        return MFAType.fromCode(this.factorType);
    }

    /**
     * 设置MFA类型
     */
    public void setMFAType(MFAType mfaType) {
        this.factorType = mfaType.getCode();
    }

    /**
     * 是否已配置
     */
    public boolean isActive() {
        return this.status != null && this.status == MFAStatus.ACTIVE.getCode();
    }

    /**
     * 是否待验证
     */
    public boolean isPending() {
        return this.status != null && this.status == MFAStatus.PENDING.getCode();
    }

    /**
     * 是否为主MFA方式
     */
    public boolean isPrimary() {
        return this.isPrimary != null && this.isPrimary == 1;
    }

    /**
     * 是否被锁定
     */
    public boolean isLocked() {
        return this.lockedUntil != null && this.lockedUntil.isAfter(LocalDateTime.now());
    }
}
