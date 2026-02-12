# 数据库初始化说明

## 概述

IDaaS 系统的数据库初始化分为两个文件：
1. `schema.sql` - 数据库表结构定义
2. `init_data.sql` - 初始数据插入

## 执行步骤

### 1. 执行表结构创建

```bash
mysql -u root -p < db/schema.sql
```

或登录 MySQL 后执行：

```sql
source /path/to/openidaas/db/schema.sql;
```

### 2. 执行初始数据插入

```bash
mysql -u root -p < db/init_data.sql
```

或登录 MySQL 后执行：

```sql
source /path/to/openidaas/db/init_data.sql;
```

## 初始数据说明

### 1. 租户

| 租户编码 | 租户名称 | 联系邮箱 |
|---------|---------|---------|
| DEFAULT | 默认租户 | admin@openidaas.com |

### 2. 组织架构

#### 根部门
- **QooBot科技** (ROOT)

#### 一级部门
- 技术中心 (TECH)
- 产品中心 (PRODUCT)
- 人力资源部 (HR)
- 财务部 (FINANCE)
- 销售部 (SALES)

#### 二级部门（技术中心下）
- 研发部 (TECH_DEV)
- 测试部 (TECH_TEST)
- 运维部 (TECH_OPS)

#### 三级部门（研发部下）
- 后端开发组 (DEV_BACKEND)
- 前端开发组 (DEV_FRONTEND)
- 移动开发组 (DEV_MOBILE)

### 3. 职位

| 职位编码 | 职位名称 | 职级 | 所属部门 |
|---------|---------|------|---------|
| CTO | 首席技术官 | P20 | 技术中心 |
| ARCHITECT | 系统架构师 | P18 | 研发部 |
| TECH_LEAD | 技术主管 | P16 | 研发部 |
| SENIOR_DEV | 高级开发工程师 | P14 | 研发部 |
| DEV_ENGINEER | 开发工程师 | P12 | 研发部 |
| JUNIOR_DEV | 初级开发工程师 | P10 | 研发部 |
| HR_DIRECTOR | 人力资源总监 | P18 | 人力资源部 |
| HR_MANAGER | 人事经理 | P16 | 人力资源部 |
| HR_SPECIALIST | 人事专员 | P12 | 人力资源部 |

### 4. 默认用户

**注意**: 所有用户初始密码均为 `Admin@123`

| 用户名 | 邮箱 | 手机 | 姓名 | 角色 | 部门 |
|-------|------|------|------|------|------|
| admin | admin@openidaas.com | - | 系统管理员 | 超级管理员 | QooBot科技 |
| hr_admin | hr_admin@openidaas.com | 13800138001 | 人事管理员 | HR管理员 | 人力资源部 |
| dept_manager | dept_manager@openidaas.com | 13800138002 | 部门经理 | 部门经理 | 研发部 |
| developer | developer@openidaas.com | 13800138003 | 开发工程师 | 普通用户 | 研发部 |
| auditor | auditor@openidaas.com | 13800138004 | 审计员 | 审计员 | QooBot科技 |
| test_user | test_user@openidaas.com | 13800138005 | 测试用户 | 普通用户 | 研发部 |

### 5. 内置角色

| 角色编码 | 角色名称 | 类型 | 描述 |
|---------|---------|------|------|
| SUPER_ADMIN | 超级管理员 | 系统角色 | 拥有所有权限 |
| ADMIN | 管理员 | 系统角色 | 拥有大部分管理权限 |
| HR_ADMIN | 人事管理员 | 系统角色 | 负责用户和组织管理 |
| DEPT_MANAGER | 部门经理 | 系统角色 | 可管理部门内用户 |
| AUDITOR | 审计员 | 系统角色 | 可查看审计日志 |
| USER | 普通用户 | 系统角色 | 基础权限 |

### 6. 权限

#### 功能权限（17个）

| 权限编码 | 权限名称 | 资源类型 | 操作 |
|---------|---------|---------|------|
| USER_READ | 用户读取 | USER | READ |
| USER_WRITE | 用户写入 | USER | WRITE |
| USER_DELETE | 用户删除 | USER | DELETE |
| USER_DEPARTMENT_ASSIGN | 用户部门分配 | USER | ASSIGN |
| USER_ROLE_ASSIGN | 用户角色分配 | USER | ASSIGN |
| DEPARTMENT_MANAGE | 部门管理 | DEPARTMENT | MANAGE |
| POSITION_MANAGE | 职位管理 | POSITION | MANAGE |
| ROLE_READ | 角色读取 | ROLE | READ |
| ROLE_WRITE | 角色写入 | ROLE | WRITE |
| PERMISSION_READ | 权限读取 | PERMISSION | READ |
| PERMISSION_WRITE | 权限写入 | PERMISSION | WRITE |
| ORG_READ | 组织读取 | ORGANIZATION | READ |
| ORG_WRITE | 组织写入 | ORGANIZATION | WRITE |
| PASSWORD_RESET | 密码重置 | AUTH | RESET |
| AUDIT_READ | 审计读取 | AUDIT | READ |
| SECURITY_EVENT_READ | 安全事件读取 | SECURITY | READ |
| SYSTEM_ADMIN | 系统管理 | SYSTEM | ADMIN |

#### 数据权限（6个）

| 权限编码 | 权限名称 | 数据范围 |
|---------|---------|---------|
| DATA_USER_SELF | 用户数据本人可见 | 本人 |
| DATA_USER_DEPT | 用户数据本部门可见 | 本部门 |
| DATA_USER_SUB_DEPT | 用户数据本部门及下属可见 | 本部门及下属 |
| DATA_USER_ALL | 用户数据全部可见 | 全部 |
| DATA_DEPARTMENT_ALL | 部门数据全部可见 | 全部 |
| DATA_ROLE_ALL | 角色数据全部可见 | 全部 |

### 7. 系统配置

#### 安全配置
- `security.password.min_length`: 8
- `security.password.require_uppercase`: true
- `security.password.require_lowercase`: true
- `security.password.require_digit`: true
- `security.password.require_special`: true
- `security.login.max_attempts`: 5
- `security.login.lockout_duration`: 1800
- `security.session.timeout`: 1800
- `security.mfa.enabled`: true

#### 认证配置
- `token.access_validity`: 7200 (2小时)
- `token.refresh_validity`: 604800 (7天)
- `password.reset_token_validity`: 3600 (1小时)

#### 审计配置
- `audit.log.retention_days`: 365 (1年)

#### 组织配置
- `organization.max_level_depth`: 10

### 8. 应用

| 应用Key | 应用名称 | 类型 | 首页地址 | 描述 |
|--------|---------|------|---------|------|
| openidaas-admin | IDaaS管理后台 | Web | http://localhost:3000 | IDaaS系统管理控制台 |
| openidaas-portal | IDaaS用户门户 | Web | http://localhost:3001 | 用户自助服务门户 |
| demo-app-1 | 示例应用1 | Web | http://demo.example.com | 演示应用1 |
| demo-app-2 | 示例应用2 | API | http://api.example.com | 演示API应用 |

### 9. OAuth2 客户端

| Client ID | Client Secret | 应用 | 授权类型 | 权限范围 |
|-----------|--------------|------|---------|---------|
| admin-client | $2a$12$... | 管理后台 | authorization_code,password,refresh_token,client_credentials | openid,profile,email |
| portal-client | $2a$12$... | 用户门户 | authorization_code,refresh_token | openid,profile,email |

**注意**: Client Secret 使用 BCrypt 加密，原始密钥为 `Admin@123`

### 10. 外部系统映射

| 系统编码 | 系统名称 | 类型 |
|---------|---------|------|
| LDAP_CORP | 企业LDAP | LDAP |
| HR_SYSTEM | HR系统 | HR系统 |
| ACTIVE_DIRECTORY | AD域 | LDAP |

## 密码说明

所有初始账户的默认密码均为：**Admin@123**

### 密码要求（根据系统配置）
- 最小长度：8位
- 必须包含：大写字母、小写字母、数字、特殊字符

### 首次登录建议

首次登录后，建议立即修改默认密码。系统要求：
- 定期修改密码（根据 `security.password.max_age` 配置）
- 符合密码强度要求

## 重新初始化

如果需要清空数据库并重新初始化：

```bash
# 删除数据库
mysql -u root -p -e "DROP DATABASE IF EXISTS open_idaas;"

# 重新执行初始化脚本
mysql -u root -p < db/schema.sql
mysql -u root -p < db/init_data.sql
```

## 数据库备份

### 备份数据库

```bash
mysqldump -u root -p open_idaas > backup/openidaas_$(date +%Y%m%d_%H%M%S).sql
```

### 恢复数据库

```bash
mysql -u root -p open_idaas < backup/openidaas_20240101_120000.sql
```

## 注意事项

1. **执行顺序**: 必须先执行 `schema.sql`，再执行 `init_data.sql`
2. **BCrypt 密码**: 系统使用 BCrypt 加密，每次加密结果不同但验证一致
3. **外键约束**: 初始化数据已考虑外键关系，确保插入顺序正确
4. **重复执行**: 脚本使用 `ON DUPLICATE KEY UPDATE`，可安全重复执行
5. **生产环境**: 生产环境部署前务必修改所有默认密码

## 故障排查

### 问题1: 脚本执行报错

**原因**: 数据库连接权限不足
**解决**: 确保MySQL用户有 CREATE、INSERT、UPDATE 权限

### 问题2: 外键约束错误

**原因**: 表结构未创建或数据顺序错误
**解决**: 确保先执行 schema.sql，再执行 init_data.sql

### 问题3: 用户无法登录

**原因**: 密码哈希不匹配或账户状态异常
**解决**: 检查 users 表的 status 字段和 password_hash 字段

```sql
-- 检查用户状态
SELECT id, username, email, status, failed_login_attempts 
FROM users 
WHERE username = 'admin';
```

## 相关文档

- [数据库设计文档](./DATABASE_DESIGN.md)
- [API文档](./API_REFERENCE.md)
- [部署指南](./DEPLOYMENT.md)
