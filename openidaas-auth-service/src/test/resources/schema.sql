-- MFA因子表
CREATE TABLE IF NOT EXISTS `user_mfa_factors` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `factor_type` VARCHAR(50) NOT NULL COMMENT 'MFA类型：TOTP, SMS, EMAIL, BACKUP_CODE',
    `factor_name` VARCHAR(100) COMMENT 'MFA名称',
    `secret` VARCHAR(500) COMMENT '密钥（加密存储）',
    `backup_info` VARCHAR(255) COMMENT '备用信息',
    `is_primary` INT NOT NULL DEFAULT 0 COMMENT '是否为主MFA',
    `status` INT NOT NULL DEFAULT 2 COMMENT '状态：1-已配置，2-待验证，3-已禁用，4-已删除',
    `last_used_at` TIMESTAMP COMMENT '最后使用时间',
    `failed_attempts` INT NOT NULL DEFAULT 0 COMMENT '失败次数',
    `locked_until` TIMESTAMP COMMENT '锁定时间',
    `verification_count` BIGINT NOT NULL DEFAULT 0 COMMENT '验证次数',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_factor_type` (`factor_type`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户MFA认证因子表';

-- MFA备用码表
CREATE TABLE IF NOT EXISTS `mfa_backup_codes` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `factor_id` BIGINT NOT NULL COMMENT 'MFA因子ID',
    `code_hash` VARCHAR(64) NOT NULL COMMENT '备用码哈希',
    `is_used` INT NOT NULL DEFAULT 0 COMMENT '是否已使用',
    `used_at` TIMESTAMP COMMENT '使用时间',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_user_id_factor_id` (`user_id`, `factor_id`),
    INDEX `idx_code_hash` (`code_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MFA备用码表';

-- MFA日志表
CREATE TABLE IF NOT EXISTS `mfa_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `factor_id` BIGINT COMMENT 'MFA因子ID',
    `factor_type` VARCHAR(50) COMMENT 'MFA类型',
    `client_ip` VARCHAR(50) COMMENT '客户端IP',
    `result` VARCHAR(20) NOT NULL COMMENT '结果：SUCCESS, FAILURE',
    `failure_reason` VARCHAR(255) COMMENT '失败原因',
    `verified_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '验证时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_result` (`result`),
    INDEX `idx_verified_at` (`verified_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MFA验证日志表';
