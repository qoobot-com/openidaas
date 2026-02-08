-- ============================================================================
-- OpenIDaaS 数据库分区策略
-- 创建时间: 2026-02-08
-- ============================================================================

-- 设置时区为 UTC
SET TIME ZONE 'UTC';

-- ============================================================================
-- 审计日志表月度分区
-- ============================================================================

-- 为 audit_logs 表创建月度分区 (按时间范围)
-- 从 2026-01-01 开始创建分区

-- 2026年1月分区
CREATE TABLE audit_logs_2026_01 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');

-- 2026年2月分区
CREATE TABLE audit_logs_2026_02 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');

-- 2026年3月分区
CREATE TABLE audit_logs_2026_03 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-03-01') TO ('2026-04-01');

-- 2026年4月分区
CREATE TABLE audit_logs_2026_04 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-04-01') TO ('2026-05-01');

-- 2026年5月分区
CREATE TABLE audit_logs_2026_05 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-05-01') TO ('2026-06-01');

-- 2026年6月分区
CREATE TABLE audit_logs_2026_06 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');

-- 2026年7月分区
CREATE TABLE audit_logs_2026_07 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');

-- 2026年8月分区
CREATE TABLE audit_logs_2026_08 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-08-01') TO ('2026-09-01');

-- 2026年9月分区
CREATE TABLE audit_logs_2026_09 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-09-01') TO ('2026-10-01');

-- 2026年10月分区
CREATE TABLE audit_logs_2026_10 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-10-01') TO ('2026-11-01');

-- 2026年11月分区
CREATE TABLE audit_logs_2026_11 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-11-01') TO ('2026-12-01');

-- 2026年12月分区
CREATE TABLE audit_logs_2026_12 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-12-01') TO ('2027-01-01');

-- ============================================================================
-- 自动分区管理函数
-- ============================================================================

-- 创建下个月分区的函数
CREATE OR REPLACE FUNCTION create_next_month_partition()
RETURNS VOID AS $$
DECLARE
    current_month DATE := date_trunc('month', CURRENT_DATE);
    next_month_start DATE := current_month + INTERVAL '1 month';
    next_month_end DATE := next_month_start + INTERVAL '1 month';
    partition_name TEXT := 'audit_logs_' || to_char(next_month_start, 'YYYY_MM');
BEGIN
    -- 检查分区是否已存在
    IF NOT EXISTS (
        SELECT 1
        FROM pg_tables
        WHERE tablename = partition_name
    ) THEN
        EXECUTE format(
            'CREATE TABLE %I PARTITION OF audit_logs
             FOR VALUES FROM (%L) TO (%L)',
            partition_name, next_month_start, next_month_end
        );
        RAISE NOTICE 'Created partition: %', partition_name;
    ELSE
        RAISE NOTICE 'Partition % already exists', partition_name;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- 创建批量分区函数 (未来N个月)
CREATE OR REPLACE FUNCTION create_future_partitions(months_to_create INTEGER DEFAULT 12)
RETURNS VOID AS $$
DECLARE
    i INTEGER;
    partition_name TEXT;
    partition_start DATE;
    partition_end DATE;
    current_month DATE := date_trunc('month', CURRENT_DATE);
BEGIN
    FOR i IN 1..months_to_create LOOP
        partition_start := current_month + (i || ' months')::INTERVAL;
        partition_end := partition_start + INTERVAL '1 month';
        partition_name := 'audit_logs_' || to_char(partition_start, 'YYYY_MM');

        IF NOT EXISTS (
            SELECT 1
            FROM pg_tables
            WHERE tablename = partition_name
        ) THEN
            EXECUTE format(
                'CREATE TABLE %I PARTITION OF audit_logs
                 FOR VALUES FROM (%L) TO (%L)',
                partition_name, partition_start, partition_end
            );
            RAISE NOTICE 'Created partition: %', partition_name;
        END IF;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 分区清理函数
-- ============================================================================

-- 删除旧分区函数 (保留N个月的数据)
CREATE OR REPLACE FUNCTION drop_old_partitions(months_to_keep INTEGER DEFAULT 12)
RETURNS VOID AS $$
DECLARE
    partition_name TEXT;
    partition_date DATE;
    cutoff_date DATE := date_trunc('month', CURRENT_DATE - (months_to_keep || ' months')::INTERVAL);
BEGIN
    FOR partition_name IN
        SELECT tablename
        FROM pg_tables
        WHERE tablename LIKE 'audit_logs_%'
        ORDER BY tablename
    LOOP
        -- 从分区名称中提取日期
        BEGIN
            partition_date := to_date(
                replace(partition_name, 'audit_logs_', ''),
                'YYYY_MM'
            );

            IF partition_date < cutoff_date THEN
                EXECUTE format('DROP TABLE IF EXISTS %I CASCADE', partition_name);
                RAISE NOTICE 'Dropped old partition: %', partition_name;
            END IF;
        EXCEPTION WHEN OTHERS THEN
            -- 忽略解析错误
            NULL;
        END;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 分区分离函数 (用于归档)
-- ============================================================================

-- 将指定月份的分区分离为普通表
CREATE OR REPLACE FUNCTION detach_partition(target_date DATE)
RETURNS TEXT AS $$
DECLARE
    partition_name TEXT := 'audit_logs_' || to_char(target_date, 'YYYY_MM');
    archive_table_name TEXT := 'audit_logs_archive_' || to_char(target_date, 'YYYY_MM');
BEGIN
    -- 检查分区是否存在
    IF NOT EXISTS (
        SELECT 1
        FROM pg_tables
        WHERE tablename = partition_name
    ) THEN
        RETURN 'ERROR: Partition ' || partition_name || ' does not exist';
    END IF;

    -- 分离分区
    EXECUTE format(
        'ALTER TABLE audit_logs DETACH PARTITION %I',
        partition_name
    );

    -- 重命名为归档表
    EXECUTE format(
        'ALTER TABLE %I RENAME TO %I',
        partition_name, archive_table_name
    );

    RETURN 'SUCCESS: Detached partition ' || partition_name || ' to ' || archive_table_name;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 分区附加函数 (用于恢复)
-- ============================================================================

-- 将归档表重新附加为分区
CREATE OR REPLACE FUNCTION attach_partition(target_date DATE)
RETURNS TEXT AS $$
DECLARE
    archive_table_name TEXT := 'audit_logs_archive_' || to_char(target_date, 'YYYY_MM');
    partition_name TEXT := 'audit_logs_' || to_char(target_date, 'YYYY_MM');
    partition_start DATE := date_trunc('month', target_date);
    partition_end DATE := partition_start + INTERVAL '1 month';
BEGIN
    -- 检查归档表是否存在
    IF NOT EXISTS (
        SELECT 1
        FROM pg_tables
        WHERE tablename = archive_table_name
    ) THEN
        RETURN 'ERROR: Archive table ' || archive_table_name || ' does not exist';
    END IF;

    -- 重命名为分区
    EXECUTE format(
        'ALTER TABLE %I RENAME TO %I',
        archive_table_name, partition_name
    );

    -- 附加分区
    EXECUTE format(
        'ALTER TABLE audit_logs ATTACH PARTITION %I
         FOR VALUES FROM (%L) TO (%L)',
        partition_name, partition_start, partition_end
    );

    RETURN 'SUCCESS: Attached partition ' || partition_name;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 分区统计函数
-- ============================================================================

-- 查看分区统计信息
CREATE OR REPLACE FUNCTION get_partition_stats()
RETURNS TABLE (
    partition_name TEXT,
    row_count BIGINT,
    total_size TEXT,
    index_size TEXT,
    date_range TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        t.tablename AS partition_name,
        pg_stat_get_tuples_returned(c.oid) AS row_count,
        pg_size_pretty(pg_total_relation_size(c.oid)) AS total_size,
        pg_size_pretty(pg_indexes_size(c.oid)) AS index_size,
        CASE
            WHEN t.tablename = 'audit_logs' THEN 'DEFAULT'
            ELSE 'PARTITION'
        END AS date_range
    FROM pg_tables t
    JOIN pg_class c ON c.relname = t.tablename
    WHERE t.tablename LIKE 'audit_logs%' OR t.tablename = 'audit_logs'
    ORDER BY t.tablename;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 自动维护任务 (使用 pg_cron 扩展)
-- ============================================================================

-- 注意：需要先安装 pg_cron 扩展
-- CREATE EXTENSION IF NOT EXISTS pg_cron;

-- 每月1号自动创建下个月的分区
-- SELECT cron.schedule('create-monthly-partition', '0 0 1 * *', 'SELECT create_next_month_partition();');

-- 每月1号自动删除超过12个月的旧分区
-- SELECT cron.schedule('drop-old-partitions', '0 1 1 * *', 'SELECT drop_old_partitions(12);');

-- ============================================================================
-- 创建未来12个月的分区
-- ============================================================================

-- 自动创建未来12个月的分区
SELECT create_future_partitions(12);

-- ============================================================================
-- 分区管理完成
-- ============================================================================

-- 查看当前分区
SELECT
    tablename AS partition_name,
    schemaname AS schema
FROM pg_tables
WHERE tablename LIKE 'audit_logs_%'
ORDER BY tablename;

-- 查看分区统计信息
SELECT * FROM get_partition_stats();
