-- ============================================================================
-- OpenIDaaS 数据迁移脚本
-- 用于版本升级和数据迁移
-- 创建时间: 2026-02-08
-- ============================================================================

-- 设置时区为 UTC
SET TIME ZONE 'UTC';

-- ============================================================================
-- 版本控制表
-- ============================================================================

CREATE TABLE IF NOT EXISTS schema_migrations (
    version VARCHAR(255) PRIMARY KEY,
    description TEXT,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    checksum VARCHAR(255)
);

-- ============================================================================
-- 迁移函数库
-- ============================================================================

-- 记录迁移版本
CREATE OR REPLACE FUNCTION record_migration(version VARCHAR, description VARCHAR DEFAULT NULL, checksum VARCHAR DEFAULT NULL)
RETURNS VOID AS $$
BEGIN
    INSERT INTO schema_migrations (version, description, checksum)
    VALUES (version, description, checksum)
    ON CONFLICT (version) DO NOTHING;
END;
$$ LANGUAGE plpgsql;

-- 检查迁移是否已应用
CREATE OR REPLACE FUNCTION is_migration_applied(version VARCHAR)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM schema_migrations WHERE schema_migrations.version = version
    );
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 迁移脚本版本 V1.0.0 - 初始表结构
-- ============================================================================

DO $$
BEGIN
    IF NOT is_migration_applied('V1.0.0') THEN

        -- V1.0.0 的迁移逻辑已在 schema.sql 中实现
        -- 这里只记录版本

        PERFORM record_migration('V1.0.0', 'Initial database schema', 'sha256_placeholder');

        RAISE NOTICE 'Migration V1.0.0 applied successfully';
    ELSE
        RAISE NOTICE 'Migration V1.0.0 already applied';
    END IF;
END
$$;

-- ============================================================================
-- 迁移脚本版本 V1.1.0 - 添加用户扩展字段
-- ============================================================================

DO $$
BEGIN
    IF NOT is_migration_applied('V1.1.0') THEN

        -- 添加用户扩展字段
        ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(512);
        ALTER TABLE users ADD COLUMN IF NOT EXISTS first_name VARCHAR(64);
        ALTER TABLE users ADD COLUMN IF NOT EXISTS last_name VARCHAR(64);
        ALTER TABLE users ADD COLUMN IF NOT EXISTS display_name VARCHAR(128);
        ALTER TABLE users ADD COLUMN IF NOT EXISTS locale VARCHAR(16) DEFAULT 'zh-CN';
        ALTER TABLE users ADD COLUMN IF NOT EXISTS timezone VARCHAR(64) DEFAULT 'Asia/Shanghai';
        ALTER TABLE users ADD COLUMN IF NOT EXISTS attributes JSONB DEFAULT '{}'::jsonb;

        -- 添加扩展字段索引
        CREATE INDEX IF NOT EXISTS idx_users_display_name ON users(display_name) WHERE deleted_at IS NULL;
        CREATE INDEX IF NOT EXISTS idx_users_locale ON users(locale);
        CREATE INDEX IF NOT EXISTS idx_users_attributes ON users USING GIN(attributes);

        PERFORM record_migration('V1.1.0', 'Add user extended fields', 'sha256_placeholder');

        RAISE NOTICE 'Migration V1.1.0 applied successfully';
    ELSE
        RAISE NOTICE 'Migration V1.1.0 already applied';
    END IF;
END
$$;

-- ============================================================================
-- 迁移脚本版本 V1.2.0 - 添加会话管理
-- ============================================================================

DO $$
BEGIN
    IF NOT is_migration_applied('V1.2.0') THEN

        -- V1.2.0 的迁移逻辑已在 schema.sql 中实现 (user_sessions 表)
        -- 这里只记录版本

        PERFORM record_migration('V1.2.0', 'Add session management', 'sha256_placeholder');

        RAISE NOTICE 'Migration V1.2.0 applied successfully';
    ELSE
        RAISE NOTICE 'Migration V1.2.0 already applied';
    END IF;
END
$$;

-- ============================================================================
-- 迁移脚本版本 V1.3.0 - 添加多因素认证
-- ============================================================================

DO $$
BEGIN
    IF NOT is_migration_applied('V1.3.0') THEN

        -- V1.3.0 的迁移逻辑已在 schema.sql 中实现 (backup_codes 表, users.mfa_enabled 等)
        -- 这里只记录版本

        PERFORM record_migration('V1.3.0', 'Add multi-factor authentication', 'sha256_placeholder');

        RAISE NOTICE 'Migration V1.3.0 applied successfully';
    ELSE
        RAISE NOTICE 'Migration V1.3.0 already applied';
    END IF;
END
$$;

-- ============================================================================
-- 迁移脚本版本 V1.4.0 - 添加租户计费信息
-- ============================================================================

DO $$
BEGIN
    IF NOT is_migration_applied('V1.4.0') THEN

        -- 为租户表添加更多计费相关字段
        ALTER TABLE tenants ADD COLUMN IF NOT EXISTS subscription_start_date DATE;
        ALTER TABLE tenants ADD COLUMN IF NOT EXISTS subscription_end_date DATE;
        ALTER TABLE tenants ADD COLUMN IF NOT EXISTS user_count INTEGER DEFAULT 0;
        ALTER TABLE tenants ADD COLUMN IF NOT EXISTS storage_used BIGINT DEFAULT 0;
        ALTER TABLE tenants ADD COLUMN IF NOT EXISTS api_calls_count BIGINT DEFAULT 0;

        -- 添加计费索引
        CREATE INDEX IF NOT EXISTS idx_tenants_subscription_end ON tenants(subscription_end_date);
        CREATE INDEX IF NOT EXISTS idx_tenants_user_count ON tenants(user_count);

        PERFORM record_migration('V1.4.0', 'Add tenant billing information', 'sha256_placeholder');

        RAISE NOTICE 'Migration V1.4.0 applied successfully';
    ELSE
        RAISE NOTICE 'Migration V1.4.0 already applied';
    END IF;
END
$$;

-- ============================================================================
-- 迁移脚本版本 V1.5.0 - 添加数据加密支持
-- ============================================================================

DO $$
BEGIN
    IF NOT is_migration_applied('V1.5.0') THEN

        -- 创建加密密钥表
        CREATE TABLE IF NOT EXISTS encryption_keys (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            tenant_id UUID NOT NULL,
            key_name VARCHAR(128) NOT NULL,
            key_version INTEGER NOT NULL DEFAULT 1,
            key_algorithm VARCHAR(32) NOT NULL DEFAULT 'AES-256-GCM',
            key_value BYTEA NOT NULL,
            iv BYTEA,
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            expires_at TIMESTAMP,
            is_active BOOLEAN DEFAULT TRUE
        );

        CREATE INDEX IF NOT EXISTS idx_encryption_keys_tenant_name ON encryption_keys(tenant_id, key_name);
        CREATE INDEX IF NOT EXISTS idx_encryption_keys_active ON encryption_keys(is_active) WHERE is_active = TRUE;

        -- 为敏感字段添加加密标记
        ALTER TABLE users ADD COLUMN IF NOT EXISTS email_encrypted BOOLEAN DEFAULT FALSE;
        ALTER TABLE users ADD COLUMN IF NOT EXISTS phone_encrypted BOOLEAN DEFAULT FALSE;

        ALTER TABLE oauth2_clients ADD COLUMN IF NOT EXISTS client_secret_encrypted BOOLEAN DEFAULT FALSE;

        PERFORM record_migration('V1.5.0', 'Add data encryption support', 'sha256_placeholder');

        RAISE NOTICE 'Migration V1.5.0 applied successfully';
    ELSE
        RAISE NOTICE 'Migration V1.5.0 already applied';
    END IF;
END
$$;

-- ============================================================================
-- 迁移脚本版本 V1.6.0 - 添加数据归档功能
-- ============================================================================

DO $$
BEGIN
    IF NOT is_migration_applied('V1.6.0') THEN

        -- 创建归档任务表
        CREATE TABLE IF NOT EXISTS archive_tasks (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            task_name VARCHAR(255) NOT NULL,
            task_type VARCHAR(64) NOT NULL,
            source_table VARCHAR(128) NOT NULL,
            target_schema VARCHAR(128),
            target_table VARCHAR(128),
            archive_date DATE NOT NULL,
            status VARCHAR(32) NOT NULL,
            started_at TIMESTAMP,
            completed_at TIMESTAMP,
            records_count INTEGER,
            error_message TEXT,
            created_by UUID
        );

        CREATE INDEX IF NOT EXISTS idx_archive_tasks_status ON archive_tasks(status);
        CREATE INDEX IF NOT EXISTS idx_archive_tasks_type ON archive_tasks(task_type);
        CREATE INDEX IF NOT EXISTS idx_archive_tasks_date ON archive_tasks(archive_date);

        PERFORM record_migration('V1.6.0', 'Add data archiving functionality', 'sha256_placeholder');

        RAISE NOTICE 'Migration V1.6.0 applied successfully';
    ELSE
        RAISE NOTICE 'Migration V1.6.0 already applied';
    END IF;
END
$$;

-- ============================================================================
-- 迁移脚本版本 V1.7.0 - 优化审计日志
-- ============================================================================

DO $$
BEGIN
    IF NOT is_migration_applied('V1.7.0') THEN

        -- 为审计日志添加更多字段
        ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS tenant_name VARCHAR(128);
        ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS username VARCHAR(64);
        ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS session_id UUID;

        -- 添加审计日志摘要字段 (用于快速查询)
        ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS summary TEXT;

        -- 添加审计日志标签
        ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS tags TEXT[];

        -- 创建标签的 GIN 索引
        CREATE INDEX IF NOT EXISTS idx_audit_logs_tags ON audit_logs USING GIN(tags);

        PERFORM record_migration('V1.7.0', 'Optimize audit logs', 'sha256_placeholder');

        RAISE NOTICE 'Migration V1.7.0 applied successfully';
    ELSE
        RAISE NOTICE 'Migration V1.7.0 already applied';
    END IF;
END
$$;

-- ============================================================================
-- 迁移脚本版本 V1.8.0 - 添加用户活动追踪
-- ============================================================================

DO $$
BEGIN
    IF NOT is_migration_applied('V1.8.0') THEN

        -- 创建用户活动表
        CREATE TABLE IF NOT EXISTS user_activities (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            user_id UUID NOT NULL,
            tenant_id UUID NOT NULL,
            activity_type VARCHAR(64) NOT NULL,
            activity_name VARCHAR(128),
            metadata JSONB DEFAULT '{}'::jsonb,
            ip_address VARCHAR(45),
            device_type VARCHAR(32),
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        );

        CREATE INDEX IF NOT EXISTS idx_user_activities_user_id ON user_activities(user_id);
        CREATE INDEX IF NOT EXISTS idx_user_activities_tenant_id ON user_activities(tenant_id);
        CREATE INDEX IF NOT EXISTS idx_user_activities_type ON user_activities(activity_type);
        CREATE INDEX IF NOT EXISTS idx_user_activities_created_at ON user_activities(created_at DESC);

        -- 为用户表添加最后活动时间
        ALTER TABLE users ADD COLUMN IF NOT EXISTS last_activity_at TIMESTAMP;

        PERFORM record_migration('V1.8.0', 'Add user activity tracking', 'sha256_placeholder');

        RAISE NOTICE 'Migration V1.8.0 applied successfully';
    ELSE
        RAISE NOTICE 'Migration V1.8.0 already applied';
    END IF;
END
$$;

-- ============================================================================
-- 迁移脚本版本 V1.9.0 - 添加组织架构支持
-- ============================================================================

DO $$
BEGIN
    IF NOT is_migration_applied('V1.9.0') THEN

        -- 创建部门/组织表
        CREATE TABLE IF NOT EXISTS departments (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            tenant_id UUID NOT NULL,
            parent_id UUID,
            name VARCHAR(128) NOT NULL,
            code VARCHAR(64) NOT NULL,
            description TEXT,
            level INTEGER NOT NULL DEFAULT 0,
            sort_order INTEGER DEFAULT 0,
            manager_id UUID,
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            deleted_at TIMESTAMP
        );

        -- 创建用户部门关联表
        CREATE TABLE IF NOT EXISTS user_departments (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            user_id UUID NOT NULL,
            department_id UUID NOT NULL,
            is_primary BOOLEAN DEFAULT FALSE,
            position VARCHAR(128),
            joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            left_at TIMESTAMP
        );

        -- 添加部门索引
        CREATE INDEX IF NOT EXISTS idx_departments_tenant_id ON departments(tenant_id);
        CREATE INDEX IF NOT EXISTS idx_departments_parent_id ON departments(parent_id);
        CREATE INDEX IF NOT EXISTS idx_departments_level ON departments(level);
        CREATE INDEX IF NOT EXISTS idx_departments_code ON departments(code);

        -- 添加用户部门关联索引
        CREATE INDEX IF NOT EXISTS idx_user_departments_user_id ON user_departments(user_id);
        CREATE INDEX IF NOT EXISTS idx_user_departments_department_id ON user_departments(department_id);
        CREATE INDEX IF NOT EXISTS idx_user_departments_primary ON user_departments(user_id, is_primary) WHERE is_primary = TRUE;

        -- 为用户表添加主部门
        ALTER TABLE users ADD COLUMN IF NOT EXISTS primary_department_id UUID;
        ALTER TABLE users ADD COLUMN IF NOT EXISTS position VARCHAR(128);

        PERFORM record_migration('V1.9.0', 'Add organization structure support', 'sha256_placeholder');

        RAISE NOTICE 'Migration V1.9.0 applied successfully';
    ELSE
        RAISE NOTICE 'Migration V1.9.0 already applied';
    END IF;
END
$$;

-- ============================================================================
-- 迁移脚本版本 V2.0.0 - 性能优化和索引重建
-- ============================================================================

DO $$
BEGIN
    IF NOT is_migration_applied('V2.0.0') THEN

        -- 并发重建索引 (生产环境建议使用 CONCURRENTLY)
        -- REINDEX INDEX CONCURRENTLY idx_users_tenant_username;
        -- REINDEX INDEX CONCURRENTLY idx_tokens_user_id;
        -- REINDEX INDEX CONCURRENTLY idx_audit_logs_created_at;

        -- 分析表统计信息
        ANALYZE users;
        ANALYZE tokens;
        ANALYZE audit_logs;
        ANALYZE roles;
        ANALYZE user_roles;
        ANALYZE oauth2_clients;
        ANALYZE user_sessions;

        -- 清理死元组
        VACUUM ANALYZE users;
        VACUUM ANALYZE tokens;
        VACUUM ANALYZE audit_logs;

        PERFORM record_migration('V2.0.0', 'Performance optimization and index rebuild', 'sha256_placeholder');

        RAISE NOTICE 'Migration V2.0.0 applied successfully';
    ELSE
        RAISE NOTICE 'Migration V2.0.0 already applied';
    END IF;
END
$$;

-- ============================================================================
-- 查看已应用的迁移版本
-- ============================================================================

SELECT
    version,
    description,
    applied_at,
    checksum
FROM schema_migrations
ORDER BY version;

-- ============================================================================
-- 迁移脚本执行完成
-- ============================================================================
