# OpenIDaaS 数据库设计文档

## 📋 目录

- [概述](#概述)
- [表结构设计](#表结构设计)
- [索引策略](#索引策略)
- [分区策略](#分区策略)
- [性能优化](#性能优化)
- [安全策略](#安全策略)
- [备份恢复](#备份恢复)

---

## 概述

### 数据库信息

- **数据库**: PostgreSQL 15+
- **字符集**: UTF8
- **时区**: UTC
- **连接池**: HikariCP
- **ORM**: Spring Data JPA

### 设计目标

- 支持 **1000万** 用户
- 支持 **10亿次** 认证请求
- 支持 **10TB** 审计日志存储
- 查询响应时间 **< 100ms**

---

## 表结构设计

### 核心表概览

| 表名 | 说明 | 预估记录数 |
|------|------|-----------|
| tenants | 租户表 | 10,000 |
| users | 用户表 | 10,000,000 |
| roles | 角色表 | 100,000 |
| user_roles | 用户角色关联表 | 20,000,000 |
| oauth2_clients | OAuth2 客户端表 | 50,000 |
| tokens | Token 表 | 100,000,000 |
| audit_logs | 审计日志表 | 10,000,000,000 (分区) |
| user_sessions | 用户会话表 | 50,000,000 |
| permissions | 权限表 | 1,000 |

### 表结构详细说明

#### 1. tenants (租户表)

```sql
CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    name VARCHAR(128) NOT NULL,           -- 租户名称
    code VARCHAR(64) NOT NULL,            -- 租户编码
    domain VARCHAR(255),                  -- 租户域名
    status ENUM('ACTIVE','INACTIVE','SUSPENDED'),
    config JSONB,                         -- 租户配置
    billing_info JSONB,                   -- 计费信息
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
```

**设计要点**:
- 使用 UUID 作为主键，避免自增 ID 泄露
- JSONB 类型存储配置，灵活扩展
- 软删除支持 (deleted_at)
- 唯一约束: name, code

#### 2. users (用户表)

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,              -- 租户ID (外键)
    username VARCHAR(64) NOT NULL,        -- 用户名 (租户内唯一)
    email VARCHAR(128),                   -- 邮箱 (租户内唯一)
    phone VARCHAR(32),                    -- 手机号 (租户内唯一)
    password VARCHAR(255) NOT NULL,       -- BCrypt 加密密码
    status ENUM('ACTIVE','INACTIVE','LOCKED','DELETED'),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    last_login_at TIMESTAMP,
    login_count INTEGER DEFAULT 0,
    deleted_at TIMESTAMP,
    mfa_enabled BOOLEAN DEFAULT FALSE,     -- 是否启用 MFA
    mfa_secret VARCHAR(255),              -- MFA 密钥
    email_verified BOOLEAN,
    phone_verified BOOLEAN
);
```

**设计要点**:
- 租户隔离：tenant_id + username/email/phone 组合唯一
- 密码使用 BCrypt 加密，不可逆
- MFA 支持 (TOTP)
- 软删除支持
- 登录统计：last_login_at, login_count

**性能估算**:
- 单条记录: ~500 字节
- 1000万用户: ~5GB
- 索引开销: ~2GB

#### 3. roles (角色表)

```sql
CREATE TABLE roles (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(64) NOT NULL,
    code VARCHAR(32) NOT NULL,
    description TEXT,
    permissions JSONB,                   -- 权限列表
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

**设计要点**:
- 租户隔离：tenant_id + code 唯一
- JSONB 存储权限列表，灵活扩展
- 支持权限继承和组合

#### 4. user_roles (用户角色关联表)

```sql
CREATE TABLE user_roles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    assigned_at TIMESTAMP,
    assigned_by UUID,                     -- 分配人ID
    expires_at TIMESTAMP                  -- 过期时间 (可选)
);
```

**设计要点**:
- 支持临时角色分配 (expires_at)
- 记录分配历史 (assigned_at, assigned_by)
- 复合唯一索引: user_id + role_id + expires_at

#### 5. oauth2_clients (OAuth2 客户端表)

```sql
CREATE TABLE oauth2_clients (
    id UUID PRIMARY KEY,
    client_id VARCHAR(255) UNIQUE NOT NULL,
    client_secret VARCHAR(255) NOT NULL,  -- BCrypt 加密
    client_name VARCHAR(255) NOT NULL,
    client_authentication_methods JSONB,
    authorization_grant_types JSONB,
    redirect_uris JSONB,
    scopes JSONB,
    require_proof_key BOOLEAN,
    require_authorization_consent BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

**设计要点**:
- 支持 OAuth2.1 协议
- client_id 全局唯一
- client_secret 加密存储
- JSONB 存储协议参数

#### 6. tokens (Token 表)

```sql
CREATE TABLE tokens (
    id UUID PRIMARY KEY,
    token_value TEXT NOT NULL,            -- Token 值 (JWT)
    token_type VARCHAR(32) NOT NULL,      -- access_token, refresh_token
    user_id UUID,
    client_id UUID NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    issued_at TIMESTAMP,
    status ENUM('ACTIVE','REVOKED','EXPIRED'),
    tenant_id UUID NOT NULL
);
```

**设计要点**:
- 支持 Access Token 和 Refresh Token
- 记录过期时间和状态
- 索引优化：token_value, user_id, expires_at
- 定期清理过期 Token

**性能估算**:
- 单条记录: ~1KB
- 1亿 Token: ~100GB
- 分区策略：按时间或租户分区

#### 7. audit_logs (审计日志表)

```sql
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    user_id UUID,
    action VARCHAR(64) NOT NULL,          -- 操作动作
    resource VARCHAR(255),                -- 操作资源
    ip_address VARCHAR(45),
    user_agent TEXT,
    details JSONB,                        -- 操作详情
    created_at TIMESTAMP,
    result VARCHAR(32) NOT NULL,
    duration_ms INTEGER
) PARTITION BY RANGE (created_at);
```

**设计要点**:
- **按月分区**：audit_logs_YYYY_MM
- JSONB 存储详细信息
- 支持审计日志查询和导出
- 自动清理旧分区

**性能估算**:
- 单条记录: ~2KB
- 10亿日志: ~2TB
- 分区存储，查询高效

#### 8. user_sessions (用户会话表)

```sql
CREATE TABLE user_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    session_token VARCHAR(255) UNIQUE NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    device_type VARCHAR(32),
    device_id VARCHAR(128),
    location JSONB,
    created_at TIMESTAMP,
    last_activity_at TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    status VARCHAR(32) DEFAULT 'ACTIVE'
);
```

**设计要点**:
- 支持多设备登录
- 记录设备信息和位置
- 会话过期自动清理
- 支持强制下线

---

## 索引策略

### 索引类型

| 索引类型 | 用途 | 示例 |
|---------|------|------|
| B-tree | 普通查询 | idx_users_username |
| GIN | 全文搜索/JSONB | idx_users_fulltext |
| BRIN | 大表时间范围 | idx_audit_logs_created_at_brin |
| Partial | 条件索引 | idx_users_active |
| Covering | 覆盖索引 | idx_users_login_query |

### 关键索引

#### 用户表索引

```sql
-- 主键索引 (自动创建)
CREATE UNIQUE INDEX users_pkey ON users(id);

-- 登录查询索引 (覆盖索引)
CREATE INDEX idx_users_login_query
    ON users(tenant_id, username, status, password, id)
    WHERE deleted_at IS NULL;

-- 邮箱查询索引
CREATE INDEX idx_users_email
    ON users(email)
    WHERE email IS NOT NULL AND deleted_at IS NULL;

-- 全文搜索索引
CREATE INDEX idx_users_fulltext
    ON users USING GIN(
        to_tsvector('simple',
            COALESCE(username, '') || ' ' ||
            COALESCE(email, '') || ' ' ||
            COALESCE(phone, '')
        )
    );
```

#### Token 表索引

```sql
-- Token 验证索引 (覆盖索引)
CREATE INDEX idx_tokens_validation
    ON tokens(token_value, token_type, status, expires_at, user_id, client_id)
    WHERE status = 'ACTIVE';

-- 用户 Token 查询
CREATE INDEX idx_tokens_user_status
    ON tokens(user_id, status)
    WHERE user_id IS NOT NULL;

-- 过期 Token 索引 (用于清理)
CREATE INDEX idx_tokens_expired
    ON tokens(expires_at, status)
    WHERE status = 'ACTIVE' AND expires_at < CURRENT_TIMESTAMP;
```

#### 审计日志索引

```sql
-- 时间范围 BRIN 索引 (大表优化)
CREATE INDEX idx_audit_logs_created_at_brin
    ON audit_logs USING BRIN(created_at);

-- 租户审计查询
CREATE INDEX idx_audit_logs_tenant_created
    ON audit_logs(tenant_id, created_at DESC);

-- JSONB GIN 索引
CREATE INDEX idx_audit_logs_details
    ON audit_logs USING GIN(details);
```

### 索引最佳实践

1. **选择性原则**: 只为高选择性列创建索引
2. **覆盖索引**: 包含查询所需的所有列
3. **部分索引**: 只索引满足条件的行
4. **定期维护**: VACUUM 和 REINDEX
5. **监控使用**: 使用 pg_stat_user_indexes

---

## 分区策略

### 审计日志按月分区

```sql
-- 分区表定义
CREATE TABLE audit_logs (
    ...
) PARTITION BY RANGE (created_at);

-- 创建月度分区
CREATE TABLE audit_logs_2026_01 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');

CREATE TABLE audit_logs_2026_02 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');

-- ... 更多分区
```

### 自动分区管理

#### 创建下个月分区

```sql
CREATE OR REPLACE FUNCTION create_next_month_partition()
RETURNS VOID AS $$
BEGIN
    -- 自动创建下个月的分区
    -- 参考 partition.sql
END;
$$ LANGUAGE plpgsql;
```

#### 删除旧分区

```sql
CREATE OR REPLACE FUNCTION drop_old_partitions(months_to_keep INTEGER DEFAULT 12)
RETURNS VOID AS $$
BEGIN
    -- 删除超过保留期的旧分区
    -- 参考 partition.sql
END;
$$ LANGUAGE plpgsql;
```

### 分区优势

- **查询性能**: 分区裁剪，只扫描相关分区
- **维护效率**: 可单独对分区进行 VACUUM、备份
- **归档方便**: 可将旧分区分离为归档表
- **存储优化**: 不同分区可使用不同存储

---

## 性能优化

### 查询优化

#### 1. 使用 EXPLAIN ANALYZE

```sql
EXPLAIN ANALYZE
SELECT * FROM users
WHERE tenant_id = 'xxx' AND username = 'admin';
```

#### 2. 避免 SELECT *

```sql
-- 不推荐
SELECT * FROM users WHERE id = 'xxx';

-- 推荐
SELECT id, username, email, status
FROM users WHERE id = 'xxx';
```

#### 3. 使用批量操作

```sql
-- 批量插入
INSERT INTO users (tenant_id, username, ...)
VALUES
    (uuid1, 'user1', ...),
    (uuid2, 'user2', ...),
    (uuid3, 'user3', ...);
```

### 连接池配置

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20          # 最大连接数
      minimum-idle: 5                # 最小空闲连接
      connection-timeout: 30000     # 连接超时 (30s)
      idle-timeout: 600000           # 空闲超时 (10min)
      max-lifetime: 1800000          # 最大生命周期 (30min)
```

### 缓存策略

#### Redis 缓存

- 用户信息缓存: TTL 30分钟
- Token 黑名单: TTL Token 过期时间
- 权限信息: TTL 1小时
- 会话信息: TTL 会话过期时间

#### 查询缓存

```sql
-- 使用 MATERIALIZED VIEW
CREATE MATERIALIZED VIEW mv_user_stats AS
SELECT tenant_id, COUNT(*) as user_count
FROM users
WHERE deleted_at IS NULL
GROUP BY tenant_id;

-- 刷新物化视图
REFRESH MATERIALIZED VIEW mv_user_stats;
```

### 监控指标

```sql
-- 查看表大小
SELECT
    relname AS table_name,
    pg_size_pretty(pg_total_relation_size(relid)) AS total_size
FROM pg_catalog.pg_statio_user_tables
ORDER BY pg_total_relation_size(relid) DESC;

-- 查看索引使用情况
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan AS index_scans,
    idx_tup_read AS tuples_read,
    idx_tup_fetch AS tuples_fetched
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;
```

---

## 安全策略

### 敏感字段加密

#### 1. 密码加密

```java
// 使用 BCrypt 加密
String encodedPassword = passwordEncoder.encode(rawPassword);
```

#### 2. 密钥加密

```sql
-- 使用 pgcrypto 扩展
CREATE EXTENSION pgcrypto;

-- 加密
SELECT pgp_sym_encrypt('sensitive_data', 'encryption_key');

-- 解密
SELECT pgp_sym_decrypt(encrypted_data::bytea, 'encryption_key');
```

### 数据库连接加密

```yaml
spring:
  datasource:
    url: jdbc:postgresql://host:5432/openidaas?sslmode=require
    ssl:
      mode: require
      cert: /path/to/cert.pem
      key: /path/to/key.pem
```

### 访问权限控制

```sql
-- 创建只读用户
CREATE ROLE readonly_user WITH LOGIN PASSWORD 'xxx';

-- 授权
GRANT USAGE ON SCHEMA public TO readonly_user;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO readonly_user;

-- 创建应用用户
CREATE ROLE app_user WITH LOGIN PASSWORD 'xxx';
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_user;
```

### 审计日志

```sql
-- 启用 PostgreSQL 审计日志
ALTER SYSTEM SET log_statement = 'all';
ALTER SYSTEM SET log_duration = on;
ALTER SYSTEM SET log_line_prefix = '%t %u %d %r ';

-- 重载配置
SELECT pg_reload_conf();
```

---

## 备份恢复

### 备份策略

#### 1. 全量备份 (每天)

```bash
# 使用 pg_dump 全量备份
pg_dump -h host -U postgres -d openidaas -F c -f backup_$(date +%Y%m%d).dump

# 压缩备份
gzip backup_$(date +%Y%m%d).dump
```

#### 2. 增量备份 (WAL 归档)

```postgresql
-- postgresql.conf
archive_mode = on
archive_command = 'cp %p /backup/wal/%f'
wal_level = replica
```

#### 3. 定期备份脚本

```bash
#!/bin/bash
# backup.sh

BACKUP_DIR="/backup/openidaas"
DATE=$(date +%Y%m%d_%H%M%S)

# 全量备份
pg_dump -h localhost -U postgres -d openidaas -F c \
    -f "$BACKUP_DIR/full_$DATE.dump"

# 压缩
gzip "$BACKUP_DIR/full_$DATE.dump"

# 删除7天前的备份
find $BACKUP_DIR -name "full_*.dump.gz" -mtime +7 -delete
```

### 恢复策略

#### 1. 从全量备份恢复

```bash
# 恢复全量备份
pg_restore -h localhost -U postgres -d openidaas_new \
    -F c backup_20260208.dump

-- 或使用管道恢复
gunzip -c backup_20260208.dump.gz | pg_restore -h localhost -U postgres -d openidaas
```

#### 2. 时间点恢复 (PITR)

```bash
# 使用 pgbackrest 或 barman
pgbackrest --stanza=openidaas restore \
    --delta \
    --target="2026-02-08 12:00:00"
```

### 备份验证

```bash
# 定期验证备份可恢复性
pg_restore -l backup_20260208.dump > /dev/null
if [ $? -eq 0 ]; then
    echo "Backup is valid"
else
    echo "Backup is corrupted"
    # 发送告警
fi
```

---

## 附录

### 数据库配置建议

```postgresql
# postgresql.conf

# 连接配置
max_connections = 200
shared_buffers = 4GB
effective_cache_size = 12GB
maintenance_work_mem = 1GB
work_mem = 16MB

# WAL 配置
wal_buffers = 16MB
min_wal_size = 1GB
max_wal_size = 4GB
checkpoint_completion_target = 0.9

# 查询优化
random_page_cost = 1.1
effective_io_concurrency = 200

# 日志配置
logging_collector = on
log_directory = 'log'
log_filename = 'postgresql-%Y-%m-%d_%H%M%S.log'
log_min_duration_statement = 1000  -- 记录执行超过1秒的查询
```

### 性能测试 SQL

```sql
-- 用户登录查询性能测试
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, username, email, password, status
FROM users
WHERE tenant_id = 'xxx' AND username = 'admin';

-- Token 验证查询性能测试
EXPLAIN (ANALYZE, BUFFERS)
SELECT user_id, client_id, expires_at, status
FROM tokens
WHERE token_value = 'xxx' AND token_type = 'access_token';

-- 审计日志查询性能测试 (分区裁剪)
EXPLAIN (ANALYZE, BUFFERS)
SELECT * FROM audit_logs
WHERE tenant_id = 'xxx'
  AND created_at >= '2026-01-01'
  AND created_at < '2026-02-01';
```

---

**文档版本**: 1.0
**最后更新**: 2026-02-08
**维护者**: OpenIDaaS Team
