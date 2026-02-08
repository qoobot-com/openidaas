# OpenIDaaS 用户管理模块

## 概述

openidaas-user 模块提供企业级用户管理服务，支持用户生命周期管理、组织架构管理、角色权限管理等核心功能。

## 核心功能

### 1. 用户生命周期管理

- **用户注册/激活**: 支持批量导入和单个注册，邮箱激活
- **用户信息管理**: 完整的用户档案管理，支持扩展属性
- **用户状态管理**: 活跃、停用、锁定、删除等状态管理
- **用户删除/停用**: 软删除机制，支持数据恢复

### 2. 组织架构管理

- **部门管理**: 树形结构组织架构，支持多级部门
- **职位管理**: 员工职位信息管理
- **角色管理**: 角色定义和分配，支持数据范围控制
- **权限分配**: 细粒度权限控制，支持菜单/按钮/API权限

### 3. 用户属性管理

- **基础信息**: 姓名、邮箱、电话、头像等
- **扩展属性**: 自定义字段支持（JSONB存储）
- **文件上传**: 头像/文档上传支持（MinIO）
- **个人设置**: 时区、语言、偏好设置

### 4. 搜索与筛选

- **全文搜索**: PostgreSQL全文搜索 + Elasticsearch集成
- **高级筛选**: 多条件组合查询
- **分页查询**: 高性能分页查询
- **导入导出**: Excel批量导入导出

## 技术架构

### 数据访问层

- **Spring Data JPA**: 主要ORM框架
- **MyBatis Plus**: 复杂查询支持
- **PostgreSQL**: 主数据库
- **Elasticsearch**: 搜索引擎（可选）

### 缓存层

- **Redis**: 用户信息缓存、权限缓存
- **Spring Cache**: 声明式缓存支持
- **Redisson**: 分布式锁支持

### 事务管理

- **@Transactional**: 声明式事务
- **分布式事务**: Seata集成（可选）

## REST API

### 用户管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/users | 创建用户 | user:create |
| GET | /api/users/{id} | 获取用户详情 | user:view |
| GET | /api/users | 分页查询用户 | user:view |
| POST | /api/users/search | 搜索用户 | user:view |
| PUT | /api/users/{id} | 更新用户 | user:update |
| DELETE | /api/users/{id} | 删除用户 | user:delete |
| PUT | /api/users/{id}/activate | 激活用户 | user:activate |
| PUT | /api/users/{id}/disable | 停用用户 | user:disable |
| PUT | /api/users/{id}/lock | 锁定用户 | user:lock |
| PUT | /api/users/{id}/unlock | 解锁用户 | user:unlock |
| PUT | /api/users/{id}/password | 修改密码 | user:password |
| PUT | /api/users/{id}/password/reset | 重置密码 | user:password:reset |
| PUT | /api/users/{id}/roles | 分配角色 | user:role:assign |
| GET | /api/users/{id}/permissions | 获取用户权限 | user:permission:view |
| GET | /api/users/export | 导出用户 | user:export |
| POST | /api/users/import | 导入用户 | user:import |
| GET | /api/users/statistics | 用户统计 | user:statistics |

### 部门管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/departments | 创建部门 | department:create |
| PUT | /api/departments/{id} | 更新部门 | department:update |
| DELETE | /api/departments/{id} | 删除部门 | department:delete |
| GET | /api/departments/{id} | 获取部门详情 | department:view |
| GET | /api/departments/tree | 获取部门树 | department:view |
| PUT | /api/departments/{id}/move | 移动部门 | department:move |

### 角色管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/roles | 创建角色 | role:create |
| PUT | /api/roles/{id} | 更新角色 | role:update |
| DELETE | /api/roles/{id} | 删除角色 | role:delete |
| GET | /api/roles/{id} | 获取角色详情 | role:view |
| GET | /api/roles | 获取所有角色 | role:view |
| GET | /api/roles/search | 搜索角色 | role:view |
| PUT | /api/roles/{id}/permissions | 分配权限 | role:permission:assign |
| GET | /api/roles/{id}/permissions | 获取角色权限 | role:permission:view |

### 权限管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/permissions | 创建权限 | permission:create |
| PUT | /api/permissions/{id} | 更新权限 | permission:update |
| DELETE | /api/permissions/{id} | 删除权限 | permission:delete |
| GET | /api/permissions/{id} | 获取权限详情 | permission:view |
| GET | /api/permissions | 获取所有权限 | permission:view |
| GET | /api/permissions/tree | 获取权限树 | permission:view |
| GET | /api/permissions/menu | 获取菜单权限 | permission:view |

## 性能指标

### 查询性能

- **100万用户规模**: 单页查询 < 50ms
- **全文搜索**: 关键词搜索 < 100ms
- **批量导入**: 10万用户 < 5分钟
- **并发访问**: 1000并发 QPS > 1000

### 缓存策略

- **用户信息**: Redis缓存，TTL 1小时
- **用户权限**: Redis缓存，TTL 1小时
- **部门树**: Redis缓存，TTL 2小时

## 数据库设计

### 主要表结构

#### users（用户表）
- 基础信息：username, password, fullname, nickname
- 联系信息：email, phone, country_code
- 状态信息：status, locked_until, failed_login_attempts
- 组织信息：tenant_id, department_id, manager_id
- 密码安全：password_changed_at, password_expires_at, must_change_password
- MFA支持：mfa_enabled, mfa_secret

#### departments（部门表）
- 树形结构：parent_id, level, sort_order
- 基础信息：name, code, description
- 负责人：leader_id, leader_name
- 联系信息：phone, email, address

#### roles（角色表）
- 基础信息：name, code, description
- 类型范围：type, scope, level, data_scope
- 系统标识：is_system

#### permissions（权限表）
- 树形结构：parent_id
- 资源信息：resource_type, resource_path, http_method
- 菜单配置：sort_order, icon, is_visible

#### user_profiles（用户档案表）
- 个人信息：first_name, last_name, gender, birth_date
- 联系信息：address, city, province, postal_code
- 社交信息：linkedin, twitter, wechat, qq
- 扩展字段：custom_fields (JSONB)

## 测试

### 单元测试

```bash
mvn test
```

### 集成测试

```bash
mvn verify
```

### 性能测试

使用 JMeter 进行性能测试：

```bash
jmeter -n -t user_test.jmx -l results.jtl
```

## 部署

### Docker部署

```bash
docker-compose up -d
```

### Kubernetes部署

```bash
kubectl apply -f k8s/
```

## 配置说明

### application.yml

主要配置项：

- `spring.datasource`: 数据库配置
- `spring.redis`: Redis配置
- `openidaas.user`: 用户模块配置
- `mybatis-plus`: MyBatis Plus配置

### 环境变量

- `DB_URL`: 数据库URL
- `DB_USERNAME`: 数据库用户名
- `DB_PASSWORD`: 数据库密码
- `REDIS_HOST`: Redis主机
- `REDIS_PORT`: Redis端口

## 安全说明

### 密码安全

- 使用BCrypt加密存储
- 强制密码复杂度要求
- 密码过期机制（90天）
- 密码历史记录

### 账户安全

- 登录失败锁定（5次锁定30分钟）
- MFA双因子认证支持
- 会话管理

### API安全

- 基于角色的访问控制（RBAC）
- API权限注解
- CORS配置

## 监控

### 健康检查

```bash
curl http://localhost:8082/actuator/health
```

### 指标监控

Prometheus指标端点：

```
http://localhost:8082/actuator/metrics
```

## 常见问题

### Q: 如何批量导入用户？

A: 使用 `/api/users/import` 接口上传Excel文件。

### Q: 如何自定义用户字段？

A: 使用 `user_attributes` 表存储自定义字段，支持JSONB格式。

### Q: 如何实现数据权限？

A: 通过角色的 `data_scope` 字段配置数据范围：ALL/DEPT_ONLY/SELF。

## 许可证

Apache License 2.0
