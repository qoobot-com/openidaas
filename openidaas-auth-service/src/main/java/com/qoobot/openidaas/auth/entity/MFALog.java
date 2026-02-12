package com.qoobot.openidaas.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MFA验证日志实体
 *
 * @author QooBot
 */
@Data
@TableName("mfa_logs")
public class MFALog {

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
     * MFA类型
     */
    private String factorType;

    /**
     * 验证结果：SUCCESS, FAILURE
     */
    private String result;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 设备信息
     */
    private String userAgent;

    /**
     * 验证时间
     */
    private LocalDateTime verifiedAt;

    /**
     * 失败原因
     */
    private String failureReason;
}
