/*
 * MFA多因子认证相关表
 */

USE `open_idaas`;

-- ==================== MFA认证数据域 ====================

-- 用户MFA因子表
CREATE TABLE `user_mfa_factors` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '因子ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `factor_type` varchar(32) NOT NULL COMMENT 'MFA类型：TOTP, SMS, EMAIL, BACKUP_CODE, HARDWARE_TOKEN, BIOMETRIC',
  `factor_name` varchar(64) DEFAULT NULL COMMENT 'MFA名称',
  `secret` varchar(255) DEFAULT NULL COMMENT '密钥（加密存储）',
  `backup_info` varchar(255) DEFAULT NULL COMMENT '备用信息（手机号、邮箱等）',
  `is_primary` tinyint DEFAULT '0' COMMENT '是否为主MFA方式：0-否，1-是',
  `status` tinyint NOT NULL DEFAULT '2' COMMENT '状态：1-已配置，2-待验证，3-已禁用，4-已删除',
  `last_used_at` timestamp NULL DEFAULT NULL COMMENT '最后使用时间',
  `failed_attempts` int DEFAULT '0' COMMENT '最后验证失败次数',
  `locked_until` timestamp NULL DEFAULT NULL COMMENT '锁定时间',
  `verification_count` bigint DEFAULT '0' COMMENT '验证次数',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_factor_type` (`factor_type`),
  KEY `idx_status` (`status`),
  KEY `idx_is_primary` (`is_primary`),
  CONSTRAINT `fk_user_mfa_factors_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户MFA认证因子表';

-- MFA备用码表
CREATE TABLE `mfa_backup_codes` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '备用码ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `factor_id` bigint NOT NULL COMMENT 'MFA因子ID',
  `code_hash` varchar(64) NOT NULL COMMENT '备用码哈希值',
  `is_used` tinyint DEFAULT '0' COMMENT '是否已使用：0-否，1-是',
  `used_at` timestamp NULL DEFAULT NULL COMMENT '使用时间',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_factor_id` (`factor_id`),
  KEY `idx_is_used` (`is_used`),
  CONSTRAINT `fk_mfa_backup_codes_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_mfa_backup_codes_factor_id` FOREIGN KEY (`factor_id`) REFERENCES `user_mfa_factors` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MFA备用码表';

-- MFA验证日志表
CREATE TABLE `mfa_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `factor_id` bigint DEFAULT NULL COMMENT 'MFA因子ID',
  `factor_type` varchar(32) DEFAULT NULL COMMENT 'MFA类型',
  `result` varchar(16) NOT NULL COMMENT '验证结果：SUCCESS, FAILURE',
  `client_ip` varchar(64) DEFAULT NULL COMMENT '客户端IP',
  `user_agent` varchar(512) DEFAULT NULL COMMENT '设备信息',
  `verified_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '验证时间',
  `failure_reason` varchar(255) DEFAULT NULL COMMENT '失败原因',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_factor_id` (`factor_id`),
  KEY `idx_result` (`result`),
  KEY `idx_verified_at` (`verified_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MFA验证日志表';

-- 添加索引优化
CREATE INDEX `idx_mfa_logs_user_result_time` ON `mfa_logs`(`user_id`, `result`, `verified_at`);
