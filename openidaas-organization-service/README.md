# OpenIDaaS Organization Service

组织管理服务，提供组织架构管理、组织树构建、用户组织关系维护等功能。

## 功能特性

- ✅ 组织创建、更新、删除
- ✅ 组织树形结构管理
- ✅ 组织层级和路径自动计算
- ✅ 组织编码唯一性校验
- ✅ 组织移动和重组
- ✅ 用户组织关系管理
- ✅ 组织状态管理（启用/禁用）
- ✅ 批量操作支持
- ✅ 分页查询和条件筛选
- ✅ 本地事务支持
- ✅ 分布式配置（Nacos）
- ✅ 服务注册发现（Nacos）
- ✅ 流量控制（Sentinel）

## 技术栈

- Spring Boot 3.x
- Spring Cloud Alibaba
- MyBatis Plus
- MySQL 8.0
- Redis
- Nacos
- Sentinel
- MapStruct
- Lombok

## API接口

### 组织管理接口

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 创建组织 | POST | `/organizations` | 创建新的组织 |
| 更新组织 | PUT | `/organizations/{id}` | 更新组织信息 |
| 删除组织 | DELETE | `/organizations/{id}` | 删除指定组织 |
| 获取组织详情 | GET | `/organizations/{id}` | 获取组织详细信息 |
| 获取组织树 | GET | `/organizations/tree` | 获取组织树结构 |
| 获取用户组织 | GET | `/organizations/user/{userId}` | 获取用户所属组织 |
| 移动组织 | PUT | `/organizations/{id}/move` | 移动组织到新父组织 |
| 获取组织路径 | GET | `/organizations/{id}/path` | 获取组织完整路径 |
| 检查编码唯一性 | GET | `/organizations/check-code` | 检查组织编码是否唯一 |
| 批量删除组织 | DELETE | `/organizations/batch` | 批量删除多个组织 |
| 启用组织 | PUT | `/organizations/{id}/enable` | 启用指定组织 |
| 禁用组织 | PUT | `/organizations/{id}/disable` | 禁用指定组织 |
| 分页查询组织 | GET | `/organizations/page` | 分页查询组织列表 |

## 数据库表结构

```sql
-- 组织表
CREATE TABLE sys_organization (
    id BIGINT PRIMARY KEY COMMENT '组织ID',
    name VARCHAR(100) NOT NULL COMMENT '组织名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '组织编码',
    type VARCHAR(50) COMMENT '组织类型',
    parent_id BIGINT DEFAULT 0 COMMENT '父组织ID',
    level INT DEFAULT 1 COMMENT '组织层级',
    path VARCHAR(500) COMMENT '组织路径',
    description VARCHAR(500) COMMENT '描述',
    manager_id BIGINT COMMENT '负责人ID',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '联系邮箱',
    status VARCHAR(20) DEFAULT 'ENABLED' COMMENT '状态',
    sort INT DEFAULT 0 COMMENT '排序',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '组织表';

-- 用户组织关联表
CREATE TABLE sys_user_organization (
    id BIGINT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    organization_id BIGINT NOT NULL COMMENT '组织ID',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_org (user_id, organization_id)
) COMMENT '用户组织关联表';
```

## 配置说明

### application.yml 主要配置项

```yaml
server:
  port: 8083
  servlet:
    context-path: /organization

spring:
  application:
    name: openidaas-organization-service
  datasource:
    url: jdbc:mysql://localhost:3306/open_idaas
    username: open
    password: open
  data:
    redis:
      host: localhost
      port: 6379

# Nacos配置
nacos:
  discovery:
    server-addr: localhost:8848
  config:
    server-addr: localhost:8848

# 自定义配置
app:
  organization:
    max-level: 5  # 最大组织层级
    enable-hierarchy: true  # 是否启用层级管理
```

## 快速开始

### 1. 环境准备

确保以下服务已启动：
- MySQL 8.0+
- Redis
- Nacos Server

### 2. 数据库初始化

执行 `db/schema.sql` 中的相关表结构创建语句。

### 3. 启动服务

```bash
# 编译打包
mvn clean package

# 启动服务
java -jar target/openidaas-organization-service-1.0.0-SNAPSHOT.jar
```

或者使用IDE直接运行 `OpenIDaaSOrganizationApplication` 类。

### 4. 验证服务

访问健康检查接口：
```
GET http://localhost:8083/organization/actuator/health
```

## 测试

运行单元测试：
```bash
mvn test
```

运行集成测试：
```bash
mvn verify
```

## 监控和运维

### Actuator端点

- `/actuator/health` - 健康检查
- `/actuator/info` - 应用信息
- `/actuator/metrics` - 性能指标
- `/actuator/prometheus` - Prometheus监控指标
- `/actuator/sentinel` - Sentinel流量控制信息

### 日志配置

日志文件位置：`logs/organization-service.log`

## 注意事项

1. 组织编码必须全局唯一
2. 不允许删除有子组织的父组织
3. 不允许删除有关联用户的组织
4. 组织移动时会自动检测循环引用
5. 支持最多5层组织结构（可通过配置调整）
6. 所有写操作都支持分布式事务

## 版本信息

- 版本：1.0.0-SNAPSHOT
- 最后更新：2026-02-11
- 开发者：Qoobot