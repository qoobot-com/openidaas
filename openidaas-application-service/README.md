# 应用管理服务 (Application Service)

## 概述

应用管理服务负责管理 OpenIDaaS 系统中的第三方应用集成，支持 OAuth2.0、SAML 2.0 等多种协议。

## 功能特性

### 1. 应用管理
- ✅ 应用注册和管理
- ✅ 应用类型支持（Web、移动、API、桌面、服务应用）
- ✅ 应用密钥生成和管理
- ✅ 重定向URI配置
- ✅ 应用Logo和主页URL管理
- ✅ 应用状态管理（启用/禁用）

### 2. OAuth2.0客户端管理
- ✅ OAuth2客户端注册
- ✅ 客户端ID和密钥管理
- ✅ 授权类型配置（authorization_code、implicit、refresh_token、client_credentials、password）
- ✅ 权限范围（scope）配置
- ✅ 访问令牌有效期配置
- ✅ 刷新令牌有效期配置
- ✅ 自动批准配置

### 3. SAML 2.0服务提供商管理
- ✅ SAML SP实体ID配置
- ✅ 断言消费服务URL (ACS URL) 配置
- ✅ X.509证书管理
- ✅ SAML元数据URL配置

### 4. 查询和检索
- ✅ 应用分页查询
- ✅ 按应用名称模糊查询
- ✅ 按应用类型筛选
- ✅ 按状态筛选
- ✅ 按所有者筛选
- ✅ 根据应用密钥获取应用

## 技术架构

### 后端技术栈
- **框架**: Spring Boot 3.5.10
- **持久化**: MyBatis-Plus 3.5.7
- **数据库**: MySQL 8.0
- **服务发现**: Nacos
- **API文档**: SpringDoc OpenAPI

### 前端技术栈
- **框架**: Vue 3.4 + TypeScript
- **UI组件**: Element Plus
- **HTTP客户端**: Axios
- **路由**: Vue Router 4

## API端点

### 应用管理
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/applications` | 创建应用 |
| PUT | `/api/applications` | 更新应用 |
| DELETE | `/api/applications/{appId}` | 删除应用 |
| GET | `/api/applications/{appId}` | 获取应用详情 |
| GET | `/api/applications/app-key/{appKey}` | 根据应用密钥获取应用 |
| POST | `/api/applications/query` | 分页查询应用 |

### OAuth2客户端管理
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/applications/oauth2/clients` | 创建OAuth2客户端 |
| PUT | `/api/applications/oauth2/clients` | 更新OAuth2客户端 |
| DELETE | `/api/applications/oauth2/clients/{clientId}` | 删除OAuth2客户端 |
| GET | `/api/applications/oauth2/clients/{clientId}` | 获取OAuth2客户端详情 |
| GET | `/api/applications/oauth2/clients/client-id/{clientId}` | 根据客户端ID获取OAuth2客户端 |

### SAML服务提供商管理
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/applications/saml/sp` | 创建SAML SP |
| PUT | `/api/applications/saml/sp` | 更新SAML SP |
| DELETE | `/api/applications/saml/sp/{spId}` | 删除SAML SP |
| GET | `/api/applications/saml/sp/{spId}` | 获取SAML SP详情 |

## 数据库表结构

### applications
应用信息主表，存储应用基本信息。

### oauth2_clients
OAuth2客户端配置表，存储OAuth2.0客户端配置。

### saml_service_providers
SAML服务提供商配置表，存储SAML 2.0 SP配置。

## 前端页面

### ApplicationList.vue
应用列表页面，支持：
- 多条件搜索（应用名称、类型、状态）
- 分页查询
- 查看详情
- 编辑应用
- 删除应用

### ApplicationForm.vue
应用表单页面，支持：
- 新增/编辑应用
- 表单验证
- OAuth2和SAML配置

### AppDetail.vue
应用详情页面，展示：
- 完整的应用信息
- OAuth2配置（如果已配置）
- SAML配置（如果已配置）
- 一键复制应用密钥

## 配置说明

### application.yml
```yaml
server:
  port: 8085  # 服务端口

spring:
  application:
    name: openidaas-application-service
  datasource:
    url: jdbc:mysql://localhost:3306/open_idaas
    username: root
    password: root
  data:
    redis:
      host: localhost
      port: 6379
```

## 启动说明

### 后端启动
```bash
# 编译项目
mvn clean install

# 启动服务
cd openidaas-application-service
mvn spring-boot:run
```

### 前端启动
```bash
cd openidaas-admin-ui
npm install
npm run dev
```

访问路径: http://localhost:5173/application/list

## 集成示例

### OAuth2授权码模式流程
1. 应用使用 `client_id` 和 `redirect_uri` 重定向用户到授权端点
2. 用户授权后，获得授权码
3. 应用使用授权码换取访问令牌
4. 使用访问令牌访问受保护资源

### SAML SSO流程
1. 用户访问应用（SP）
2. SP将用户重定向到身份提供商（IDP）
3. 用户在IDP处完成认证
4. IDP将SAML断言发送回SP的ACS URL
5. SP验证断言并建立会话

## 安全建议

1. **客户端密钥**: 存储时使用BCrypt加密
2. **访问令牌有效期**: 建议设置为1小时（3600秒）
3. **刷新令牌有效期**: 建议设置为30天（2592000秒）
4. **重定向URI**: 必须验证，防止开放重定向攻击
5. **权限范围**: 遵循最小权限原则

## 后续扩展

- [ ] 支持更多OAuth2授权流程（JWT承载、设备码）
- [ ] 支持OIDC（OpenID Connect）
- [ ] 支持更多SAML绑定类型
- [ ] 支持应用配额管理
- [ ] 支持应用使用统计
- [ ] 支持API密钥管理

## 许可证

Apache License 2.0
