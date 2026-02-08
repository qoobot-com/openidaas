# OpenIDaaS Tenant Module

## 概述

OpenIDaaS 租户管理模块，提供租户管理功能。

## 功能特性

- 租户 CRUD 操作
- 租户查询
- 租户用户数量管理
- 租户状态管理

## 端点

- `GET /api/tenants/{id}` - 获取租户详情
- `GET /api/tenants` - 分页查询租户
- `POST /api/tenants` - 创建租户
- `PUT /api/tenants/{id}` - 更新租户
- `DELETE /api/tenants/{id}` - 删除租户

## 许可证

Apache License 2.0
