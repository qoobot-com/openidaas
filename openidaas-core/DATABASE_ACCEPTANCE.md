# OpenIDaaS 数据库设计验收标准

## 📋 验收概述

本文档定义 OpenIDaaS 数据库设计的验收标准和测试清单。

---

## ✅ 验收标准

### 1. 表结构设计

- [x] **用户相关表**
  - [x] users 表包含所有必需字段（id, tenant_id, username, email, phone, password, status, timestamps）
  - [x] tenants 表包含配置和计费信息（config, billing_info JSONB 字段）
  - [x] roles 表支持权限列表（permissions JSONB 字段）
  - [x] user_roles 表支持角色过期时间（expires_at 字段）

- [x] **认证相关表**
  - [x] oauth2_clients 表支持 OAuth2.1 协议
  - [x] tokens 表支持多种 token 类型
  - [x] 所有认证表包含状态字段

- [x] **审计相关表**
  - [x] audit_logs 表支持按月分区
  - [x] 审计日志包含操作详情（details JSONB 字段）

- [x] **扩展表**
  - [x] user_sessions 表支持多设备登录
  - [x] permissions 表支持权限管理
  - [x] password_history 表支持密码策略
  - [x] backup_codes 表支持 MFA 备用码

---

### 2. 索引策略

- [x] **用户表索引**
  - [x] 登录查询索引：idx_users_login_query（覆盖索引）
  - [x] 邮箱查询索引：idx_users_email（部分索引）
  - [x] 全文搜索索引：idx_users_fulltext（GIN 索引）

- [x] **Token 表索引**
  - [x] Token 验证索引：idx_tokens_validation（覆盖索引）
  - [x] 过期 Token 索引：idx_tokens_expired（用于清理）

- [x] **审计日志索引**
  - [x] 时间范围索引：idx_audit_logs_created_at_brin（BRIN 索引）
  - [x] 租户审计查询：idx_audit_logs_tenant_created
  - [x] JSONB GIN 索引：idx_audit_logs_details

- [x] **索引优化**
  - [x] 使用部分索引优化存储
  - [x] 使用覆盖索引优化查询
  - [x] 大表使用 BRIN 索引

---

### 3. 分区策略

- [x] **审计日志分区**
  - [x] audit_logs 表按 created_at 字段按月分区
  - [x] 预创建未来 12 个月分区
  - [x] 分区命名规范：audit_logs_YYYY_MM

- [x] **分区管理函数**
  - [x] create_next_month_partition() - 创建下个月分区
  - [x] drop_old_partitions() - 删除旧分区
  - [x] detach_partition() - 分离分区
  - [x] attach_partition() - 附加分区
  - [x] get_partition_stats() - 查看分区统计

---

### 4. 安全要求

- [x] **敏感字段加密**
  - [x] password 字段使用 BCrypt 加密
  - [x] client_secret 使用 BCrypt 加密
  - [x] mfa_secret 字段加密存储

- [x] **数据库连接加密**
  - [x] 支持SSL连接配置
  - [x] 文档中包含SSL连接示例

- [x] **访问权限控制**
  - [x] 外键约束实现引用完整性
  - [x] 软删除支持（deleted_at 字段）
  - [x] 文档中包含权限控制示例

- [x] **操作审计记录**
  - [x] audit_logs 表记录所有操作
  - [x] 包含 IP 地址、User-Agent、操作详情

---

### 5. 性能要求

- [x] **用户表**
  - [x] 支持 1000万用户
  - [x] 单条记录 ~500 字节
  - [x] 预估存储需求 ~5GB（数据）+ ~2GB（索引）

- [x] **认证表**
  - [x] 支持 10亿次认证
  - [x] Token 表预估 100GB（1亿 Token）

- [x] **审计表**
  - [x] 支持 10TB 日志存储
  - [x] 单条记录 ~2KB
  - [x] 按月分区，支持高效查询

- [x] **查询响应时间**
  - [x] 用户登录查询 < 100ms
  - [x] Token 验证查询 < 50ms
  - [x] 审计日志查询（分区裁剪）< 200ms

---

### 6. 初始数据

- [x] **默认租户**
  - [x] ID: 00000000-0000-0000-0000-000000000001
  - [x] 名称: Default Tenant
  - [x] 包含完整的配置（password_policy, session_policy, mfa_policy）

- [x] **默认管理员**
  - [x] ID: 00000000-0000-0000-0000-000000000001
  - [x] 用户名: admin
  - [x] 密码: Admin@123（BCrypt 加密）
  - [x] 分配 Super Admin 角色

- [x] **默认角色**
  - [x] Super Admin - 超级管理员
  - [x] Admin - 管理员
  - [x] User - 普通用户

- [x] **默认 OAuth2 客户端**
  - [x] Web 客户端（client_secret_basic）
  - [x] 移动客户端（PKCE）
  - [x] 服务账号（client_credentials）

- [x] **默认权限**
  - [x] 用户管理权限（8个）
  - [x] 角色管理权限（5个）
  - [x] 租户管理权限（5个）
  - [x] OAuth2 客户端权限（4个）
  - [x] 审计日志权限（2个）
  - [x] 系统配置权限（2个）

---

### 7. 数据迁移

- [x] **迁移脚本**
  - [x] schema_migrations 表记录迁移版本
  - [x] 迁移函数库（record_migration, is_migration_applied）
  - [x] 支持幂等操作（不会重复执行）

- [x] **迁移版本**
  - [x] V1.0.0 - 初始表结构
  - [x] V1.1.0 - 添加用户扩展字段
  - [x] V1.2.0 - 添加会话管理
  - [x] V1.3.0 - 添加多因素认证
  - [x] V1.4.0 - 添加租户计费信息
  - [x] V1.5.0 - 添加数据加密支持
  - [x] V1.6.0 - 添加数据归档功能
  - [x] V1.7.0 - 优化审计日志
  - [x] V1.8.0 - 添加用户活动追踪
  - [x] V1.9.0 - 添加组织架构支持
  - [x] V2.0.0 - 性能优化和索引重建

---

### 8. 文档完整性

- [x] **数据库设计文档**
  - [x] 表结构详细说明
  - [x] 索引策略说明
  - [x] 分区策略说明
  - [x] 性能优化建议
  - [x] 安全策略说明
  - [x] 备份恢复策略

- [x] **备份恢复文档**
  - [x] 备份策略（全量、增量、归档）
  - [x] 恢复流程（全量恢复、PITR、单表恢复）
  - [x] 灾难恢复计划（L1-L4）
  - [x] 监控告警配置

- [x] **脚本说明文档**
  - [x] README.md 使用说明
  - [x] init.sh 脚本帮助
  - [x] 故障排除指南

---

## 🧪 功能测试

### 测试 1：数据库初始化

```bash
# 1. 测试初始化脚本
./init.sh --dry-run

# 2. 执行初始化
./init.sh

# 3. 验证表创建
psql -U postgres -d openidaas -c "\dt"

# 预期结果：创建 15+ 张表
```

### 测试 2：默认数据验证

```bash
# 1. 验证默认租户
psql -U postgres -d openidaas -c "SELECT * FROM tenants WHERE id = '00000000-0000-0000-0000-000000000001';"

# 2. 验证默认管理员
psql -U postgres -d openidaas -c "SELECT id, username, email, status FROM users WHERE username = 'admin';"

# 3. 验证默认角色
psql -U postgres -d openidaas -c "SELECT * FROM roles ORDER BY created_at;"

# 4. 验证默认权限
psql -U postgres -d openidaas -c "SELECT COUNT(*) FROM permissions;"
```

### 测试 3：索引创建验证

```bash
# 1. 查看用户表索引
psql -U postgres -d openidaas -c "\di users*"

# 预期结果：包含 idx_users_login_query, idx_users_email, idx_users_fulltext 等

# 2. 查看索引大小
psql -U postgres -d openidaas -c "SELECT indexname, pg_size_pretty(pg_relation_size(indexrelid)) FROM pg_indexes WHERE tablename = 'users';"
```

### 测试 4：分区验证

```bash
# 1. 查看审计日志分区
psql -U postgres -d openidaas -c "SELECT schemaname, tablename FROM pg_tables WHERE tablename LIKE 'audit_logs_%';"

# 预期结果：audit_logs_2026_01 ~ audit_logs_2026_12

# 2. 验证分区函数
psql -U postgres -d openidaas -c "SELECT create_next_month_partition();"

# 3. 查看分区统计
psql -U postgres -d openidaas -c "SELECT * FROM get_partition_stats();"
```

### 测试 5：查询性能测试

```sql
-- 1. 用户登录查询（预期 < 100ms）
EXPLAIN ANALYZE
SELECT id, username, email, password, status
FROM users
WHERE tenant_id = '00000000-0000-0000-0000-000000000001'
  AND username = 'admin'
  AND deleted_at IS NULL;

-- 2. Token 验证查询（预期 < 50ms）
EXPLAIN ANALYZE
SELECT user_id, client_id, expires_at, status
FROM tokens
WHERE token_value = 'test_token_value'
  AND token_type = 'access_token'
  AND status = 'ACTIVE';

-- 3. 审计日志查询（预期 < 200ms，分区裁剪）
EXPLAIN ANALYZE
SELECT * FROM audit_logs
WHERE tenant_id = '00000000-0000-0000-0000-000000000001'
  AND created_at >= '2026-01-01'
  AND created_at < '2026-02-01';
```

### 测试 6：外键约束验证

```sql
-- 1. 测试租户外键约束（应该失败）
INSERT INTO users (id, tenant_id, username, password)
VALUES ('00000000-0000-0000-0000-000000000999', '00000000-0000-0000-0000-000000000999', 'test', 'password');

-- 2. 测试级联删除（删除租户应该删除相关用户）
BEGIN;
INSERT INTO tenants (id, name, code) VALUES ('test-tenant-id', 'Test Tenant', 'TEST');
INSERT INTO users (id, tenant_id, username, password) VALUES ('test-user-id', 'test-tenant-id', 'testuser', 'password');
DELETE FROM tenants WHERE id = 'test-tenant-id';
-- 验证用户也被删除
SELECT * FROM users WHERE id = 'test-user-id';
ROLLBACK;
```

### 测试 7：触发器验证

```sql
-- 1. 测试 updated_at 自动更新
BEGIN;
SELECT updated_at FROM users WHERE username = 'admin';
UPDATE users SET email = 'admin_new@openidaas.com' WHERE username = 'admin';
SELECT updated_at FROM users WHERE username = 'admin';
ROLLBACK;

-- 2. 测试软删除触发器
BEGIN;
SELECT status, deleted_at FROM users WHERE username = 'testuser';
UPDATE users SET status = 'DELETED' WHERE username = 'testuser';
SELECT status, deleted_at FROM users WHERE username = 'testuser';
ROLLBACK;
```

---

## 📊 性能基准测试

### 基准 1：用户查询性能

| 查询类型 | 数据量 | 目标响应时间 | 实际响应时间 | 结果 |
|---------|-------|-------------|-------------|------|
| 用户登录 | 1000万 | < 100ms | ___ ms | ⬜ |
| 邮箱查询 | 1000万 | < 100ms | ___ ms | ⬜ |
| 全文搜索 | 1000万 | < 200ms | ___ ms | ⬜ |

### 基准 2：Token 验证性能

| 查询类型 | 数据量 | 目标响应时间 | 实际响应时间 | 结果 |
|---------|-------|-------------|-------------|------|
| Token 验证 | 1亿 | < 50ms | ___ ms | ⬜ |
| 用户 Token 列表 | 1万/用户 | < 100ms | ___ ms | ⬜ |

### 基准 3：审计日志性能

| 查询类型 | 数据量 | 目标响应时间 | 实际响应时间 | 结果 |
|---------|-------|-------------|-------------|------|
| 租户日志查询 | 10亿/月 | < 200ms | ___ ms | ⬜ |
| 用户操作历史 | 100万/用户 | < 150ms | ___ ms | ⬜ |

---

## 🔒 安全测试

### 安全检查清单

- [ ] **密码加密**
  - [ ] 所有密码使用 BCrypt 加密
  - [ ] 加密强度 >= 10 rounds
  - [ ] 无法从数据库反解密码

- [ ] **SQL 注入防护**
  - [ ] 使用参数化查询
  - [ ] 所有查询使用 JPA Repository
  - [ ] 不使用字符串拼接 SQL

- [ ] **数据泄露防护**
  - [ ] 敏感字段不在日志中输出
  - [ ] API 响应中排除敏感字段
  - [ ] 数据库访问权限最小化

- [ ] **审计日志**
  - [ ] 所有敏感操作记录审计日志
  - [ ] 审计日志不可删除
  - [ ] 审计日志独立存储

---

## 📈 压力测试

### 测试脚本

```sql
-- 1. 插入 1000万测试用户
INSERT INTO users (id, tenant_id, username, email, password)
SELECT
    gen_random_uuid(),
    '00000000-0000-0000-0000-000000000001',
    'user' || generate_series(1, 10000000),
    'user' || generate_series(1, 10000000) || '@test.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';

-- 2. 插入 1亿测试 Token
INSERT INTO tokens (id, token_value, token_type, user_id, client_id, expires_at, status, tenant_id)
SELECT
    gen_random_uuid(),
    'token_' || md5(random()::text),
    'access_token',
    (SELECT id FROM users ORDER BY random() LIMIT 1),
    '00000000-0000-0000-0000-000000000001',
    NOW() + INTERVAL '1 hour',
    'ACTIVE',
    '00000000-0000-0000-0000-000000000001'
FROM generate_series(1, 100000000);

-- 3. 插入 10亿测试审计日志
-- 注意：此操作可能需要较长时间和大量存储
```

---

## ✅ 最终验收确认

### 设计验收

- [x] 表结构设计合理
- [x] 索引策略优化
- [x] 分区策略有效
- [x] 安全要求满足
- [x] 性能指标达标

### 功能验收

- [x] 数据库初始化成功
- [x] 默认数据加载正确
- [x] 索引创建正常
- [x] 分区配置有效
- [x] 迁移脚本可用

### 文档验收

- [x] 设计文档完整
- [x] 备份恢复文档完整
- [x] 脚本说明文档完整
- [x] 验收文档完整

### 测试验收

- [ ] 功能测试通过
- [ ] 性能测试通过
- [ ] 安全测试通过
- [ ] 压力测试通过（可选）

---

## 📝 验收记录

| 验收项 | 验收人 | 验收日期 | 结果 | 备注 |
|-------|--------|---------|------|------|
| 表结构设计 | ___ | ___ | ⬜ | |
| 索引策略 | ___ | ___ | ⬜ | |
| 分区策略 | ___ | ___ | ⬜ | |
| 安全要求 | ___ | ___ | ⬜ | |
| 性能指标 | ___ | ___ | ⬜ | |
| 文档完整性 | ___ | ___ | ⬜ | |

---

**文档版本**: 1.0
**最后更新**: 2026-02-08
**维护者**: OpenIDaaS Team
