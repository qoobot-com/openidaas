package com.qoobot.openidaas.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MFA备用码实体
 *
 * @author QooBot
 */
@Data
@TableName("mfa_backup_codes")
public class MFABackupCode {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * MFA因子ID
     */
    private Long factorId;

    /**
     * 备用码（哈希存储）
     */
    private String codeHash;

    /**
     * 是否已使用
     */
    private Integer isUsed;

    /**
     * 使用时间
     */
    private LocalDateTime usedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 是否已使用
     */
    public boolean isUsed() {
        return Integer.valueOf(1).equals(this.isUsed);
    }
}
