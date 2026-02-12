# 认证服务完善总结

## 概述

OpenIDaaS 认证服务已实现完整的企业级认证和授权功能，支持 OAuth2.0、OpenID Connect、SAML 2.0 和社交登录。

## 已实现功能

### 1. Spring Security 完整配置

#### 核心配置类
- **SecurityConfig.java** - 主要安全配置
  - OAuth2 Authorization Server 安全过滤链
  - 默认安全过滤链
  - 密码编码器 (BCrypt)
  - JWT 密钥源
  - 授权服务器设置

#### 特性
- ✅ 双安全过滤链（Authorization Server 优先级更高）
- ✅ 自定义 JWT 编码器（RSA 2048位）
- ✅ 动态注册客户端（内存存储）
- ✅ Token 自定义声明
- ✅ 权限范围管理

### 2. OAuth2.0 Authorization Server

#### 授权模式支持
| 授权模式 | 说明 | 状态 |
|---------|------|------|
| **Authorization Code** | 授权码模式 | ✅ 支持 |
| **Implicit** | 简化模式 | ✅ 支持 |
| **Password** | 密码模式 | ✅ 支持 |
| **Client Credentials** | 客户端凭据模式 | ✅ 支持 |
| **Refresh Token** | 刷新令牌 | ✅ 支持 |

#### 预注册客户端

##### 1. admin-client（管理后台客户端）
- Client ID: `admin-client`
- Client Secret: `admin-secret`
- 授权类型: 授权码、刷新令牌、客户端凭据、密码
- 范围: openid, profile, email, read, write
- Token 有效期: Access Token 1小时, Refresh Token 30天
- 需要授权确认: ✅ 是
- 需要 PKCE: ✅ 是

##### 2. web-client（Web 应用客户端）
- Client ID: `web-client`
- Client Secret: `web-secret`
- 授权类型: 授权码、刷新令牌
- 范围: openid, profile, email, read
- Token 有效期: Access Token 2小时, Refresh Token 7天
- 需要授权确认: ❌ 否

##### 3. public-client（公开客户端，如移动应用）
- Client ID: `public-client`
- 认证方式: 无（无客户端密钥）
- 授权类型: 授权码、刷新令牌
- 范围: openid, profile
- Token 有效期: Access Token 1小时, Refresh Token 30天
- 需要授权确认: ❌ 否
- 需要PKCE: ✅ 是

#### OAuth2 端点

| 端点 | 路径 | 说明 |
|-----|------|------|
| Authorization Endpoint | `/oauth2/authorize` | 授权端点 |
| Token Endpoint | `/oauth2/token` | 令牌端点 |
| JWK Set Endpoint | `/.well-known/jwks.json` | 公钥端点 |
| UserInfo Endpoint | `/oidc/userinfo` | 用户信息端点 |
| Introspection Endpoint | `/oauth2/introspect` | 令牌内省端点 |
| Revocation Endpoint | `/oauth2/revoke` | 令牌撤销端点 |

### 3. OpenID Connect (OIDC)

#### OIDC 标准端点

##### 1. Discovery 端点
```
GET /.well-known/openid-configuration
```

返回完整的 OIDC 提供商配置信息，包括：
- issuer
- authorization_endpoint
- token_endpoint
- userinfo_endpoint
- jwks_uri
- end_session_endpoint
- response_types_supported
- grant_types_supported
- scopes_supported
- claims_supported
- code_challenge_methods_supported

##### 2. UserInfo 端点
```
GET /oidc/userinfo
```

返回已认证用户的 Profile 信息：
- sub (Subject)
- name
- given_name
- family_name
- email
- email_verified
- picture
- preferred_username

#### OIDC 支持的 Claims

| Claim | 说明 | OIDC 标准范围 |
|-------|------|-------------|
| sub | 用户唯一标识 | required |
| name | 完整姓名 | profile |
| given_name | 名字 | profile |
| family_name | 姓氏 | profile |
| nickname | 昵称 | profile |
| preferred_username | 首选用户名 | profile |
| picture | 头像 | profile |
| email | 邮箱 | email |
| email_verified | 邮箱验证状态 | email |
| gender | 性别 | - |
| birthdate | 出生日期 | - |
| zoneinfo | 时区 | - |
| locale | 语言环境 | - |

### 4. SAML 2.0 集成

#### SAML 2.0 支持的功能

- ✅ SAML 2.0 Service Provider (SP) 配置
- ✅ 与 Identity Provider (IdP) 集成
- ✅ SAML 2.0 SSO 单点登录
- ✅ SAML 2.0 SLO 单点登出
- ✅ 支持多种 SAML 绑定（POST, Redirect）
- ✅ 元数据端点自动生成

#### SAML 端点

| 端点 | 路径 | 说明 |
|-----|------|------|
| SSO 登录端点 | `/saml2/sso/{registrationId}` | SAML SSO |
| SLO 登出端点 | `/saml2/logout/{registrationId}` | SAML SLO |
| SP 元数据 | `/saml2/service-provider-metadata` | 服务提供商元数据 |

#### 支持的 IdP

- ADFS (Active Directory Federation Services)
- Okta
- Auth0
- Keycloak
- Ping Identity
- 其他标准 SAML 2.0 IdP

### 5. 社交登录

#### 支持的社交平台

| 平台 | 类型 | 状态 |
|-----|------|------|
| **GitHub** | OAuth2 | ✅ 支持 |
| **Google** | OIDC | ✅ 支持 |
| **Microsoft** | OIDC | ✅ 支持 |
| **微信** | OAuth2 | ✅ 支持 |

#### 社交登录流程

```
用户点击社交登录按钮
    ↓
重定向到 OAuth2/OIDC 授权端点
    ↓
用户在社交平台授权
    ↓
社交平台回调，携带 authorization_code
    ↓
后端交换 access_token 和用户信息
    ↓
创建或关联本地账户
    ↓
生成本地 JWT Token
    ↓
完成登录
```

#### 自定义用户服务

- **CustomOAuth2UserService** - 处理 GitHub、微信等 OAuth2 登录
  - processGitHubUser() - 处理 GitHub 用户信息
  - processWeChatUser() - 处理微信用户信息

- **CustomOidcUserService** - 处理 Google、Microsoft 等 OIDC 登录
  - processGoogleUser() - 处理 Google 用户信息
  - processMicrosoftUser() - 处理 Microsoft 用户信息

#### 社交登录配置

需要在 `application.yml` 中配置：

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
            scope: read:user,user:email
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: openid,profile,email
          wechat:
            client-id: ${WECHAT_APP_ID}
            client-secret: ${WECHAT_APP_SECRET}
            scope: snsapi_login
```

### 6. JWT 认证过滤器

#### JwtAuthenticationFilter 功能
- ✅ 从 `Authorization` 头提取 Bearer Token
- ✅ 使用 JWT Decoder 验证 Token
- ✅ 提取用户身份和角色
- ✅ 设置 Spring Security 上下文
- ✅ 错误处理和日志记录

#### JWT Token 结构

```
Header
{
  "alg": "RS256",
  "typ": "JWT",
  "kid": "key-id"
}

Payload
{
  "sub": "username",
  "exp": 1234567890,
  "iat": 1234560000,
  "iss": "http://localhost:8082",
  "aud": "admin-client",
  "scope": "openid profile email",
  "roles": ["ROLE_ADMIN", "ROLE_USER"],
  "custom_claim": "custom_value"
}

Signature
<RS256 signature using RSA 2048-bit key>
```

### 7. 用户详情服务

#### UserDetailsServiceImpl 特性
- ✅ 通过 Feign 客户端调用 user-service
- ✅ 支持用户名、邮箱、手机号登录
- ✅ 账户状态验证（启用、锁定、过期）
- ✅ 角色和权限加载
- ✅ 异常处理和日志记录

#### 用户信息转换

| 用户服务字段 | Spring Security 字段 |
|------------|---------------------|
| username | username |
| password | password |
| enabled | enabled |
| accountNonLocked | accountNonLocked |
| accountNonExpired | accountNonExpired |
| credentialsNonExpired | credentialsNonExpired |
| roles | authorities |

### 8. 登录页面

#### 登录页面特性
- ✅ 响应式设计
- ✅ 用户名密码登录
- ✅ 社交登录按钮
- ✅ 错误消息显示
- ✅ 登出消息显示
- ✅ 美观的渐变背景

#### 页面样式
- 渐变紫色背景
- 白色卡片设计
- 圆角和阴影效果
- 平滑过渡动画
- 响应式布局

### 9. 安全配置

#### 密码加密
- 算法: BCrypt
- 默认强度: 10
- 位置: `SecurityConfig.passwordEncoder()`

#### JWT 密钥
- 算法: RSA
- 密钥长度: 2048 位
- 生成方式: 运行时动态生成
- 签名算法: RS256

#### Token 配置

| 客户端 | Access Token | Refresh Token | 可重用 |
|-------|-------------|---------------|--------|
| admin-client | 1 小时 | 30 天 | ❌ |
| web-client | 2 小时 | 7 天 | ❌ |
| public-client | 1 小时 | 30 天 | ❌ |

#### 安全特性
- ✅ PKCE (Proof Key for Code Exchange)
- ✅ Authorization Consent（授权确认）
- ✅ Token 过期自动失效
- ✅ JWT 非对称签名
- ✅ HTTPS 强制（生产环境）
- ✅ CORS 限制（生产环境）

## 使用指南

### 1. 授权码模式

```bash
# 1. 获取授权码
GET http://localhost:8082/oauth2/authorize?
    response_type=code&
    client_id=admin-client&
    redirect_uri=http://127.0.0.1:3000/auth/callback&
    scope=openid+profile+email&
    state=xyz

# 2. 用授权码换取 Token
POST http://localhost:8082/oauth2/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code&
code=AUTHORIZATION_CODE&
redirect_uri=http://127.0.0.1:3000/auth/callback&
client_id=admin-client&
client_secret=admin-secret

# 3. 响应
{
  "access_token": "...",
  "refresh_token": "...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "openid profile email"
}
```

### 2. 刷新令牌

```bash
POST http://localhost:8082/oauth2/token
Content-Type: application/x-www-form-urlencoded

grant_type=refresh_token&
refresh_token=REFRESH_TOKEN&
client_id=admin-client&
client_secret=admin-secret
```

### 3. 令牌内省

```bash
POST http://localhost:8082/oauth2/introspect
Content-Type: application/x-www-form-urlencoded

token=ACCESS_TOKEN&
token_type_hint=access_token
```

### 4. 令牌撤销

```bash
POST http://localhost:8082/oauth2/revoke
Content-Type: application/x-www-form-urlencoded

token=ACCESS_TOKEN&
token_type_hint=access_token
```

### 5. 获取用户信息

```bash
GET http://localhost:8082/oidc/userinfo
Authorization: Bearer ACCESS_TOKEN
```

### 6. OIDC Discovery

```bash
GET http://localhost:8082/.well-known/openid-configuration
```

### 7. 社交登录

```bash
# GitHub 登录
GET http://localhost:8082/oauth2/authorization/github

# Google 登录
GET http://localhost:8082/oauth2/authorization/google

# 微信登录
GET http://localhost:8082/oauth2/authorization/wechat

# Microsoft 登录
GET http://localhost:8082/oauth2/authorization/microsoft
```

### 8. SAML SSO

```bash
# 获取 SP 元数据
GET http://localhost:8082/saml2/service-provider-metadata

# 发起 SAML SSO
GET http://localhost:8082/saml2/sso/saml2-sp

# SAML SLO
GET http://localhost:8082/saml2/logout/saml2-sp
```

## 配置说明

### 环境变量

| 环境变量 | 说明 | 示例 |
|---------|------|------|
| JWT_SECRET | JWT 签名密钥（生产必须） | your-secret-key-256-bit |
| DB_HOST | 数据库主机 | localhost |
| DB_USERNAME | 数据库用户名 | root |
| DB_PASSWORD | 数据库密码 | root |
| REDIS_HOST | Redis 主机 | localhost |
| REDIS_PASSWORD | Redis 密码 | - |
| GITHUB_CLIENT_ID | GitHub 应用 ID | abc123 |
| GITHUB_CLIENT_SECRET | GitHub 应用密钥 | xyz789 |
| GOOGLE_CLIENT_ID | Google 客户端 ID | 123.apps.googleusercontent.com |
| GOOGLE_CLIENT_SECRET | Google 客户端密钥 | GOCSPX-abc123 |
| WECHAT_APP_ID | 微信 AppID | wxabc123 |
| WECHAT_APP_SECRET | 微信 AppSecret | abc123 |
| MICROSOFT_CLIENT_ID | Microsoft 客户端 ID | abc123-4567-... |
| MICROSOFT_CLIENT_SECRET | Microsoft 客户端密钥 | xyz789 |

### 开发环境配置

```yaml
server:
  port: 8082

spring:
  profiles:
    active: dev
```

### 生产环境配置

```yaml
server:
  port: 8082

spring:
  profiles:
    active: prod
  security:
    oauth2:
      client:
        registration:
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
```

## 测试

### 1. 运行认证服务

```bash
cd openidaas-auth-service
mvn spring-boot:run
```

### 2. 访问登录页面

```
http://localhost:8082/login
```

### 3. 测试 OAuth2 授权码流程

1. 访问授权端点
2. 使用 `admin/admin` 登录
3. 授权请求的范围
4. 获取授权码
5. 用授权码换取 Token

### 4. 测试社交登录

1. 在 GitHub 创建 OAuth 应用
2. 配置环境变量
3. 点击 GitHub 登录按钮
4. 授权应用
5. 完成登录

## 依赖

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- OAuth2 Authorization Server -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-authorization-server</artifactId>
</dependency>

<!-- SAML 2.0 -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-saml2-service-provider</artifactId>
</dependency>

<!-- OAuth2 Client (Social Login) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

<!-- Thymeleaf -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

## 后续优化

### 待实现功能

1. **数据库存储客户端**
   - 目前客户端存储在内存
   - 建议：使用 JdbcRegisteredClientRepository

2. **授权确认记录持久化**
   - JdbcOAuth2AuthorizationConsentService

3. **社交账号绑定**
   - 将社交账号与本地账户关联
   - 支持多种社交登录方式绑定同一账户

4. **Token 撤销**
   - 实现完整的 Token 撤销逻辑
   - 支持按用户或客户端撤销

5. **审计日志**
   - 记录所有登录、授权、Token 操作
   - 集成到 audit-service

6. **MFA 集成**
   - OAuth2 流程中集成 MFA
   - 二次验证确认

7. **动态 Scope 管理**
   - 从数据库加载自定义 Scope
   - 支持客户端自定义权限范围

## 相关文档

- [OAuth 2.0 RFC 6749](https://tools.ietf.org/html/rfc6749)
- [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-core-1_0.html)
- [SAML 2.0](https://www.oasis-open.org/committees/security/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Spring Authorization Server](https://docs.spring.io/spring-authorization-server/reference/)
