# OpenIDaaS Core Module

## 📦 模块概述

`openidaas-core` 是 OpenIDaaS 系统的核心模块，提供：

- **数据库表结构设计** - 完整的 PostgreSQL 数据库架构
- **数据初始化脚本** - 数据库初始化和迁移脚本
- **文档** - 数据库设计、备份恢复、验收标准文档

---

## 🚀 快速开始

### 数据库初始化

```bash
# 进入数据库脚本目录
cd openidaas-core/src/main/resources/db

# 赋予执行权限
chmod +x init.sh

# 执行初始化
./init.sh
```

### 手动初始化

```bash
# 1. 创建数据库
createdb -U postgres openidaas

# 2. 执行 SQL 文件（按顺序）
psql -U postgres -d openidaas -f schema.sql
psql -U postgres -d openidaas -f index.sql
psql -U postgres -d openidaas -f partition.sql
psql -U postgres -d openidaas -f init-data.sql
psql -U postgres -d openidaas -f migration.sql
```

---

## 📁 目录结构

```
openidaas-core/
├── src/main/resources/db/
│   ├── schema.sql              # 数据库表结构
│   ├── index.sql               # 索引创建脚本
│   ├── partition.sql           # 分区创建脚本
│   ├── init-data.sql           # 初始数据脚本
│   ├── migration.sql           # 数据迁移脚本
│   ├── init.sh                 # 数据库初始化脚本
│   └── README.md               # 脚本使用说明
├── DATABASE_DESIGN.md          # 数据库设计文档
├── DATABASE_BACKUP_RECOVERY.md # 备份恢复文档
├── DATABASE_ACCEPTANCE.md       # 验收标准文档
└── README.md                   # 本文件
```

---

## 📊 数据库架构

### 核心表

| 表名 | 说明 | 记录数预估 |
|------|------|-----------|
| tenants | 租户表 | 10,000 |
| users | 用户表 | 10,000,000 |
| roles | 角色表 | 100,000 |
| user_roles | 用户角色关联表 | 20,000,000 |
| permissions | 权限表 | 1,000 |

### 认证表

| 表名 | 说明 | 记录数预估 |
|------|------|-----------|
| oauth2_clients | OAuth2 客户端表 | 50,000 |
| tokens | Token 表 | 100,000,000 |
| user_sessions | 用户会话表 | 50,000,000 |
| password_history | 密码历史表 | 50,000,000 |

### 审计表

| 表名 | 说明 | 记录数预估 |
|------|------|-----------|
| audit_logs | 审计日志表（按月分区） | 10,000,000,000 |

---

## 🎯 性能指标

| 指标 | 目标 | 说明 |
|------|------|------|
| 用户表容量 | 1000万用户 | 支持1000万用户规模 |
| 认证请求 | 10亿次 | 支持10亿次认证请求 |
| 审计日志 | 10TB | 支持10TB审计日志存储 |
| 查询响应 | < 100ms | 常用查询响应时间 |
| Token 验证 | < 50ms | Token 验证响应时间 |

---

## 🔐 安全特性

- ✅ 密码 BCrypt 加密
- ✅ 敏感字段加密存储
- ✅ SSL 连接支持
- ✅ 操作审计日志
- ✅ 软删除支持
- ✅ 外键约束

---

## 📚 文档

### 数据库设计

- [数据库设计文档](DATABASE_DESIGN.md) - 详细的数据库设计说明
- [备份恢复文档](DATABASE_BACKUP_RECOVERY.md) - 备份策略和恢复流程
- [验收标准文档](DATABASE_ACCEPTANCE.md) - 验收标准和测试清单
- [脚本使用说明](src/main/resources/db/README.md) - SQL 脚本使用说明

---

## 🔧 分区管理

### 创建下个月分区

```sql
SELECT create_next_month_partition();
```

### 删除旧分区

```sql
-- 删除 12 个月前的分区
SELECT drop_old_partitions(12);
```

### 查看分区统计

```sql
SELECT * FROM get_partition_stats();
```

---

## 📈 监控查询

### 查看表大小

```sql
SELECT
    relname AS table_name,
    pg_size_pretty(pg_total_relation_size(relid)) AS total_size
FROM pg_catalog.pg_statio_user_tables
ORDER BY pg_total_relation_size(relid) DESC;
```

### 查看索引使用情况

```sql
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan AS index_scans
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;
```

---

## 🛠️ 故障排除

### 连接数据库失败

```bash
# 检查 PostgreSQL 服务状态
sudo systemctl status postgresql

# 启动 PostgreSQL 服务
sudo systemctl start postgresql
```

### 执行 SQL 文件失败

```bash
# 使用 verbose 模式执行
psql -v ON_ERROR_STOP=1 -U postgres -d openidaas -f schema.sql
```

---

## 📝 默认管理员凭证

```
Username: admin
Password: Admin@123
Email:    admin@openidaas.com
```

**⚠️ 重要**: 初始化完成后，请立即修改默认管理员密码！

---

## 🆘 获取帮助

- 查看数据库设计文档
- 查看备份恢复文档
- 查看 SQL 脚本 README
- 提交 Issue 到项目仓库

---

**版本**: 1.0
**更新时间**: 2026-02-08
**维护者**: OpenIDaaS Team
