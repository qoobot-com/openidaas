# OpenIDaaS 数据库脚本说明

本目录包含 OpenIDaaS 系统的数据库脚本文件。

## 📁 文件列表

| 文件名 | 说明 |
|--------|------|
| `schema.sql` | 数据库表结构定义 |
| `index.sql` | 索引创建脚本 |
| `partition.sql` | 分区创建脚本 |
| `init-data.sql` | 初始数据脚本 |
| `migration.sql` | 数据迁移脚本 |
| `init.sh` | 数据库初始化脚本 |
| `README.md` | 本说明文件 |

---

## 🚀 快速开始

### 方法一：使用初始化脚本（推荐）

```bash
# 1. 赋予执行权限
chmod +x init.sh

# 2. 执行初始化（使用默认配置）
./init.sh

# 3. 使用自定义配置
./init.sh -H localhost -P 5432 -u postgres -d openidaas
```

### 方法二：手动执行 SQL 文件

```bash
# 1. 创建数据库
createdb -U postgres openidaas

# 2. 按顺序执行 SQL 文件
psql -U postgres -d openidaas -f schema.sql
psql -U postgres -d openidaas -f index.sql
psql -U postgres -d openidaas -f partition.sql
psql -U postgres -d openidaas -f init-data.sql
psql -U postgres -d openidaas -f migration.sql
```

---

## 📋 执行顺序

SQL 文件必须按以下顺序执行：

1. **schema.sql** - 创建所有表结构和基础约束
2. **index.sql** - 创建索引以优化查询性能
3. **partition.sql** - 创建审计日志的月度分区
4. **init-data.sql** - 加载初始数据（默认管理员、角色、权限等）
5. **migration.sql** - 执行数据迁移（可选）

---

## 🔧 环境变量配置

可以在执行脚本前设置以下环境变量：

```bash
export DB_HOST="localhost"
export DB_PORT="5432"
export DB_USER="postgres"
export DB_NAME="openidaas"
export PGPASSWORD="your_password"
```

---

## 📦 表结构说明

### 核心表

- **tenants** - 租户表
- **users** - 用户表
- **roles** - 角色表
- **user_roles** - 用户角色关联表

### 认证表

- **oauth2_clients** - OAuth2 客户端表
- **tokens** - Token 表
- **user_sessions** - 用户会话表

### 安全表

- **permissions** - 权限表
- **password_history** - 密码历史表
- **password_reset_tokens** - 密码重置令牌表
- **backup_codes** - 备用验证码表

### 审计表

- **audit_logs** - 审计日志表（按月分区）

### 组织表

- **departments** - 部门表
- **user_departments** - 用户部门关联表

---

## 🎯 分区管理

### 查看分区

```sql
SELECT
    tablename AS partition_name,
    schemaname AS schema
FROM pg_tables
WHERE tablename LIKE 'audit_logs_%'
ORDER BY tablename;
```

### 创建下个月分区

```sql
SELECT create_next_month_partition();
```

### 删除旧分区

```sql
-- 删除 12 个月前的分区
SELECT drop_old_partitions(12);
```

### 分离分区（用于归档）

```sql
SELECT detach_partition('2026-01-01'::date);
```

---

## 🔍 性能优化

### 分析表统计信息

```sql
ANALYZE;
```

### 清理死元组

```sql
VACUUM FULL;
```

### 重建索引

```sql
-- 生产环境建议使用 CONCURRENTLY
-- REINDEX INDEX CONCURRENTLY idx_users_username;
REINDEX DATABASE openidaas;
```

---

## 📊 监控查询

### 查看表大小

```sql
SELECT
    relname AS table_name,
    pg_size_pretty(pg_total_relation_size(relid)) AS total_size,
    pg_size_pretty(pg_relation_size(relid)) AS table_size,
    pg_size_pretty(pg_indexes_size(relid)) AS indexes_size
FROM pg_catalog.pg_statio_user_tables
ORDER BY pg_total_relation_size(relid) DESC
LIMIT 10;
```

### 查看索引使用情况

```sql
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan AS index_scans,
    idx_tup_read AS tuples_read,
    idx_tup_fetch AS tuples_fetched
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC
LIMIT 10;
```

### 查看慢查询

```sql
SELECT
    query,
    calls,
    total_time,
    mean_time,
    max_time
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;
```

---

## 🛠️ 故障排除

### 问题 1：连接数据库失败

**错误信息**: `connection to server on socket "/tmp/.s.PGSQL.5432" failed`

**解决方案**:

```bash
# 检查 PostgreSQL 服务状态
sudo systemctl status postgresql

# 启动 PostgreSQL 服务
sudo systemctl start postgresql

# 检查连接配置
sudo vi /etc/postgresql/15/main/pg_hba.conf
```

### 问题 2：执行 SQL 文件失败

**错误信息**: `relation "xxx" does not exist`

**解决方案**:

1. 检查 SQL 文件执行顺序是否正确
2. 确认前序步骤已成功完成
3. 查看详细错误信息

```bash
# 使用 verbose 模式执行
psql -v ON_ERROR_STOP=1 -U postgres -d openidaas -f schema.sql
```

### 问题 3：权限不足

**错误信息**: `permission denied for table xxx`

**解决方案**:

```sql
-- 授予应用用户权限
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO openidaas_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO openidaas_user;
```

---

## 🔐 安全建议

1. **修改默认密码**: 初始化完成后，立即修改默认管理员密码

2. **使用 SSL 连接**: 配置数据库使用 SSL 加密连接

```bash
# JDBC URL 示例
jdbc:postgresql://host:5432/openidaas?sslmode=require
```

3. **限制访问权限**: 配置 pg_hba.conf 只允许特定 IP 访问

```bash
# /etc/postgresql/15/main/pg_hba.conf
host    openidaas    openidaas_user    192.168.1.0/24    md5
```

4. **定期备份**: 配置自动备份策略

5. **审计日志**: 启用数据库审计日志

```sql
ALTER SYSTEM SET log_statement = 'all';
ALTER SYSTEM SET log_duration = on;
SELECT pg_reload_conf();
```

---

## 📚 相关文档

- [数据库设计文档](../../DATABASE_DESIGN.md)
- [备份恢复文档](../../DATABASE_BACKUP_RECOVERY.md)

---

## 🆘 获取帮助

如果遇到问题，可以：

1. 查看日志文件
2. 使用 `--dry-run` 选项测试
3. 查看 PostgreSQL 官方文档
4. 提交 Issue 到项目仓库

---

**版本**: 1.0
**更新时间**: 2026-02-08
**维护者**: OpenIDaaS Team
