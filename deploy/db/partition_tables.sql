-- ============================================
-- OpenIDaaS 数据库分区表脚本
-- ============================================
-- 执行前请先备份数据库
-- ============================================

USE open_idaas;

-- ============================================
-- 1. audit_logs表按月分区
-- ============================================

-- 删除原表(如果有数据,请先备份)
-- DROP TABLE IF EXISTS audit_logs;

-- 创建分区表
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    username VARCHAR(64),
    operation VARCHAR(128) NOT NULL COMMENT '操作类型',
    request_data TEXT COMMENT '请求数据(脱敏)',
    response_data TEXT COMMENT '响应数据(脱敏)',
    ip VARCHAR(64) COMMENT 'IP地址',
    user_agent VARCHAR(512) COMMENT '用户代理',
    status_code INT COMMENT 'HTTP状态码',
    error_message TEXT COMMENT '错误信息',
    duration_ms BIGINT COMMENT '执行耗时(毫秒)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_time (user_id, created_at),
    INDEX idx_operation_time (operation, created_at),
    INDEX idx_ip_time (ip, created_at),
    INDEX idx_timestamp (created_at)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='审计日志表-按月分区'
PARTITION BY RANGE (TO_DAYS(created_at)) (
    PARTITION p202601 VALUES LESS THAN (TO_DAYS('2026-02-01')),
    PARTITION p202602 VALUES LESS THAN (TO_DAYS('2026-03-01')),
    PARTITION p202603 VALUES LESS THAN (TO_DAYS('2026-04-01')),
    PARTITION p202604 VALUES LESS THAN (TO_DAYS('2026-05-01')),
    PARTITION p202605 VALUES LESS THAN (TO_DAYS('2026-06-01')),
    PARTITION p202606 VALUES LESS THAN (TO_DAYS('2026-07-01')),
    PARTITION p202607 VALUES LESS THAN (TO_DAYS('2026-08-01')),
    PARTITION p202608 VALUES LESS THAN (TO_DAYS('2026-09-01')),
    PARTITION p202609 VALUES LESS THAN (TO_DAYS('2026-10-01')),
    PARTITION p202610 VALUES LESS THAN (TO_DAYS('2026-11-01')),
    PARTITION p202611 VALUES LESS THAN (TO_DAYS('2026-12-01')),
    PARTITION p202612 VALUES LESS THAN (TO_DAYS('2027-01-01')),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);

-- ============================================
-- 2. mfa_logs表按月分区
-- ============================================

CREATE TABLE IF NOT EXISTS mfa_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    username VARCHAR(64),
    mfa_type VARCHAR(32) NOT NULL COMMENT 'MFA类型: TOTP/SMS/EMAIL',
    factor_id BIGINT COMMENT 'MFA因子ID',
    success TINYINT(1) NOT NULL COMMENT '是否成功',
    ip VARCHAR(64) COMMENT 'IP地址',
    user_agent VARCHAR(512) COMMENT '用户代理',
    error_message VARCHAR(512) COMMENT '错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_time (user_id, created_at),
    INDEX idx_type_time (mfa_type, created_at),
    INDEX idx_success_time (success, created_at),
    INDEX idx_timestamp (created_at)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='MFA日志表-按月分区'
PARTITION BY RANGE (TO_DAYS(created_at)) (
    PARTITION p202601 VALUES LESS THAN (TO_DAYS('2026-02-01')),
    PARTITION p202602 VALUES LESS THAN (TO_DAYS('2026-03-01')),
    PARTITION p202603 VALUES LESS THAN (TO_DAYS('2026-04-01')),
    PARTITION p202604 VALUES LESS THAN (TO_DAYS('2026-05-01')),
    PARTITION p202605 VALUES LESS THAN (TO_DAYS('2026-06-01')),
    PARTITION p202606 VALUES LESS THAN (TO_DAYS('2026-07-01')),
    PARTITION p202607 VALUES LESS THAN (TO_DAYS('2026-08-01')),
    PARTITION p202608 VALUES LESS THAN (TO_DAYS('2026-09-01')),
    PARTITION p202609 VALUES LESS THAN (TO_DAYS('2026-10-01')),
    PARTITION p202610 VALUES LESS THAN (TO_DAYS('2026-11-01')),
    PARTITION p202611 VALUES LESS THAN (TO_DAYS('2026-12-01')),
    PARTITION p202612 VALUES LESS THAN (TO_DAYS('2027-01-01')),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);

-- ============================================
-- 3. login_sessions表按月分区
-- ============================================

CREATE TABLE IF NOT EXISTS login_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    username VARCHAR(64),
    token VARCHAR(512) NOT NULL,
    ip VARCHAR(64),
    user_agent VARCHAR(512),
    device_info VARCHAR(256),
    location VARCHAR(128),
    login_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    logout_at TIMESTAMP NULL,
    expired_at TIMESTAMP NULL,
    INDEX idx_user_token (user_id, token(255)),
    INDEX idx_timestamp (login_at),
    INDEX idx_user_time (user_id, login_at)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='登录会话表-按月分区'
PARTITION BY RANGE (TO_DAYS(login_at)) (
    PARTITION p202601 VALUES LESS THAN (TO_DAYS('2026-02-01')),
    PARTITION p202602 VALUES LESS THAN (TO_DAYS('2026-03-01')),
    PARTITION p202603 VALUES LESS THAN (TO_DAYS('2026-04-01')),
    PARTITION p202604 VALUES LESS THAN (TO_DAYS('2026-05-01')),
    PARTITION p202605 VALUES LESS THAN (TO_DAYS('2026-06-01')),
    PARTITION p202606 VALUES LESS THAN (TO_DAYS('2026-07-01')),
    PARTITION p202607 VALUES LESS THAN (TO_DAYS('2026-08-01')),
    PARTITION p202608 VALUES LESS THAN (TO_DAYS('2026-09-01')),
    PARTITION p202609 VALUES LESS THAN (TO_DAYS('2026-10-01')),
    PARTITION p202610 VALUES LESS THAN (TO_DAYS('2026-11-01')),
    PARTITION p202611 VALUES LESS THAN (TO_DAYS('2026-12-01')),
    PARTITION p202612 VALUES LESS THAN (TO_DAYS('2027-01-01')),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);

-- ============================================
-- 4. security_events表按月分区
-- ============================================

CREATE TABLE IF NOT EXISTS security_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_type VARCHAR(64) NOT NULL COMMENT '事件类型',
    event_level VARCHAR(32) NOT NULL COMMENT '事件级别: INFO/WARNING/ERROR/CRITICAL',
    user_id BIGINT,
    username VARCHAR(64),
    ip VARCHAR(64),
    description TEXT COMMENT '事件描述',
    detail_data JSON COMMENT '事件详情(JSON格式)',
    resolved TINYINT(1) DEFAULT 0 COMMENT '是否已处理',
    resolved_by BIGINT,
    resolved_at TIMESTAMP NULL,
    resolved_note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_type_time (event_type, created_at),
    INDEX idx_level_time (event_level, created_at),
    INDEX idx_user_time (user_id, created_at),
    INDEX idx_timestamp (created_at),
    INDEX idx_resolved (resolved)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='安全事件表-按月分区'
PARTITION BY RANGE (TO_DAYS(created_at)) (
    PARTITION p202601 VALUES LESS THAN (TO_DAYS('2026-02-01')),
    PARTITION p202602 VALUES LESS THAN (TO_DAYS('2026-03-01')),
    PARTITION p202603 VALUES LESS THAN (TO_DAYS('2026-04-01')),
    PARTITION p202604 VALUES LESS THAN (TO_DAYS('2026-05-01')),
    PARTITION p202605 VALUES LESS THAN (TO_DAYS('2026-06-01')),
    PARTITION p202606 VALUES LESS THAN (TO_DAYS('2026-07-01')),
    PARTITION p202607 VALUES LESS THAN (TO_DAYS('2026-08-01')),
    PARTITION p202608 VALUES LESS THAN (TO_DAYS('2026-09-01')),
    PARTITION p202609 VALUES LESS THAN (TO_DAYS('2026-10-01')),
    PARTITION p202610 VALUES LESS THAN (TO_DAYS('2026-11-01')),
    PARTITION p202611 VALUES LESS THAN (TO_DAYS('2026-12-01')),
    PARTITION p202612 VALUES LESS THAN (TO_DAYS('2027-01-01')),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);

-- ============================================
-- 5. 创建存储过程:自动添加下月分区
-- ============================================

DELIMITER $$

DROP PROCEDURE IF EXISTS `add_next_month_partition`$$

CREATE PROCEDURE `add_next_month_partition`(IN table_name VARCHAR(64))
BEGIN
    DECLARE next_month_date DATE;
    DECLARE partition_name VARCHAR(32);
    DECLARE partition_value VARCHAR(64);
    DECLARE pmax_exists INT;

    -- 检查是否存在pmax分区
    SELECT COUNT(*)
    INTO pmax_exists
    FROM information_schema.partitions
    WHERE table_schema = DATABASE()
        AND table_name = table_name
        AND partition_name = 'pmax';

    IF pmax_exists > 0 THEN
        -- 计算下个月的第一天
        SET next_month_date = DATE_ADD(DATE_FORMAT(NOW(), '%Y-%m-01'), INTERVAL 1 MONTH);
        SET partition_name = CONCAT('p', DATE_FORMAT(next_month_date, '%Y%m'));

        -- 删除pmax分区
        SET @sql = CONCAT('ALTER TABLE ', table_name, ' DROP PARTITION pmax');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;

        -- 添加新分区
        SET @sql = CONCAT(
            'ALTER TABLE ', table_name,
            ' ADD PARTITION (PARTITION ', partition_name,
            ' VALUES LESS THAN (TO_DAYS(\'',
            DATE_ADD(next_month_date, INTERVAL 1 MONTH),
            '\')))'
        );
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;

        -- 重新添加pmax分区
        SET @sql = CONCAT('ALTER TABLE ', table_name, ' ADD PARTITION (PARTITION pmax VALUES LESS THAN MAXVALUE)');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;

        SELECT CONCAT('Added partition ', partition_name, ' for table ', table_name) AS result;
    ELSE
        SELECT CONCAT('Table ', table_name, ' does not have pmax partition') AS result;
    END IF;
END$$

DELIMITER ;

-- ============================================
-- 6. 创建事件:每月自动添加新分区
-- ============================================

DROP EVENT IF EXISTS `auto_add_partition_event`$$

CREATE EVENT `auto_add_partition_event`
ON SCHEDULE EVERY 1 MONTH
STARTS CONCAT(DATE_FORMAT(DATE_ADD(NOW(), INTERVAL 1 MONTH), '%Y-%m'), '-01 02:00:00')
DO
BEGIN
    CALL add_next_month_partition('audit_logs');
    CALL add_next_month_partition('mfa_logs');
    CALL add_next_month_partition('login_sessions');
    CALL add_next_month_partition('security_events');
END$$

DELIMITER ;

-- ============================================
-- 7. 创建存储过程:清理过期分区
-- ============================================

DELIMITER $$

DROP PROCEDURE IF EXISTS `drop_old_partition`$$

CREATE PROCEDURE `drop_old_partition`(IN table_name VARCHAR(64), IN months_to_keep INT)
BEGIN
    DECLARE cutoff_date DATE;
    DECLARE done INT DEFAULT FALSE;
    DECLARE partition_name VARCHAR(32);
    DECLARE partition_value VARCHAR(64);
    DECLARE cursor1 CURSOR FOR
        SELECT partition_name, partition_description
        FROM information_schema.partitions
        WHERE table_schema = DATABASE()
            AND table_name = table_name
            AND partition_name != 'pmax'
            AND partition_name != 'p202601' -- 保留第一个分区
        ORDER BY partition_ordinal_position ASC;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    SET cutoff_date = DATE_SUB(DATE_FORMAT(NOW(), '%Y-%m-01'), INTERVAL months_to_keep MONTH);

    OPEN cursor1;
    read_loop: LOOP
        FETCH cursor1 INTO partition_name, partition_value;
        IF done THEN
            LEAVE read_loop;
        END IF;

        -- 解析分区描述中的日期
        SET @partition_date = DATE_FORMAT(
            STR_TO_DATE(partition_value, 'TO_DAYS(\'%Y-%m-%d\')'),
            '%Y-%m-%d'
        );

        -- 如果分区日期早于截止日期,则删除
        IF @partition_date < cutoff_date THEN
            SET @sql = CONCAT('ALTER TABLE ', table_name, ' DROP PARTITION ', partition_name);
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;

            SELECT CONCAT('Dropped partition ', partition_name, ' from table ', table_name) AS result;
        END IF;
    END LOOP;
    CLOSE cursor1;
END$$

DELIMITER ;

-- ============================================
-- 8. 查看分区信息
-- ============================================

-- 查看所有分区表
SELECT
    table_name,
    partition_name,
    partition_expression,
    partition_description,
    table_rows
FROM information_schema.partitions
WHERE table_schema = 'open_idaas'
    AND partition_name IS NOT NULL
ORDER BY table_name, partition_ordinal_position;

-- 查看分区大小
SELECT
    table_name,
    partition_name,
    ROUND(data_length / 1024 / 1024, 2) AS data_mb,
    ROUND(index_length / 1024 / 1024, 2) AS index_mb,
    ROUND((data_length + index_length) / 1024 / 1024, 2) AS total_mb
FROM information_schema.partitions
WHERE table_schema = 'open_idaas'
    AND partition_name IS NOT NULL
ORDER BY table_name, partition_ordinal_position;

-- ============================================
-- 9. 手动添加新分区示例
-- ============================================

-- CALL add_next_month_partition('audit_logs');

-- ============================================
-- 10. 手动清理旧分区示例(保留6个月)
-- ============================================

-- CALL drop_old_partition('audit_logs', 6);

-- ============================================
-- 注意事项
-- ============================================
-- 1. 分区表不支持外键约束
-- 2. 分区键必须包含在主键和唯一索引中
-- 3. 每个表最多支持8192个分区
-- 4. 删除分区比删除数据快得多
-- 5. 建议在业务低峰期执行分区操作
-- 6. 定期监控分区数据量,必要时归档历史数据
-- ============================================
