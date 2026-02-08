# OpenIDaaS Authentication Service

## 概述

企业级认证服务，实现 OAuth2.1/OIDC 标准协议。

## 功能特性

### OAuth2.1/OIDC 支持
- ✅ Authorization Code Flow
- ✅ Client Credentials Flow
- ✅ PKCE (Proof Key for Code Exchange)
- ✅ JWT Access Tokens
- ✅ Refresh Tokens
- ✅ ID Tokens (OIDC)

### 多协议支持
- ✅ OIDC (OpenID Connect)
- ✅ SAML2.0（规划中）
- ✅ CAS（规划中）
- ✅ LDAP（规划中）

### 认证策略
- ✅ 密码认证（BCrypt + Argon2）
- ✅ 多因子认证 (MFA/TOTP)
- ✅ 社会化登录（Google/微信/钉钉）
- ✅ 证书认证 (X.509)（规划中）

### Token 管理
- ✅ Token 生成/验证
- ✅ Token 刷新/撤销
- ✅ Token 缓存管理（Redis）
- ✅ Token 审计日志
- ✅ 单点登录（所有设备登出）

## 技术栈

- **框架**: Spring Boot 4.0.2
- **认证**: Spring Security 6.2.0
- **OAuth2**: Spring Authorization Server 1.3.0
- **Token**: JWT + JWK
- **加密**: BCrypt + Argon2
- **缓存**: Redis (Redisson)
- **MFA**: Google Authenticator

## API 端点

### 认证端点

| 方法 | 端点 | 说明 |
|------|--------|------|
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/refresh | 刷新令牌 |
| POST | /api/auth/logout | 登出 |
| POST | /api/auth/mfa/generate | 生成 MFA Secret |
| POST | /api/auth/mfa/confirm | 确认并激活 MFA |
| DELETE | /api/auth/mfa/disable | 禁用 MFA |
| POST | /api/auth/social/google | Google 登录 |
| POST | /api/auth/social/wechat | 微信登录 |
| DELETE | /api/auth/tokens/all | 撤销所有 Token |
| GET | /api/auth/tokens/active | 获取活跃 Token |

### OAuth2 端点

| 端点 | 说明 |
|--------|------|
| /oauth2/authorize | 授权端点 |
| /oauth2/token | 令牌端点 |
| /oauth2/jwks | JWK 端点 |
| /oauth2/revoke | 撤销端点 |
| /oauth2/introspect | 内省端点 |
| /.well-known/openid-configuration | OIDC 配置 |

## 配置

### application.yml

```yaml
openidaas:
  auth:
    jwt:
      secret: your-secret-key
      expiration: 86400  # 24小时
      refresh-expiration: 2592000  # 30天
    
    mfa:
      enabled: true
      issuer: OpenIDaaS
      code-digits: 6
    
    social:
      google:
        enabled: true
        client-id: your-google-client-id
        client-secret: your-google-client-secret
      wechat:
        enabled: true
        app-id: your-wechat-app-id
        app-secret: your-wechat-app-secret
```

## OAuth2 流程示例

### Authorization Code Flow

```http
GET /oauth2/authorize?
    response_type=code&
    client_id=openidaas-client&
    redirect_uri=http://localhost:8084/authorized&
    scope=openid profile email&
    state=xyz&
    code_challenge=CODE_CHALLENGE&
    code_challenge_method=S256
```

### Token Request

```http
POST /oauth2/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code&
code=AUTHORIZATION_CODE&
redirect_uri=http://localhost:8084/authorized&
client_id=openidaas-client&
client_secret=openidaas-secret&
code_verifier=CODE_VERIFIER
```

## PKCE 流程

1. 客户端生成 `code_verifier`（43-128字符的随机字符串）
2. 客户端计算 `code_challenge`（SHA256(code_verifier) + Base64URL）
3. 授权请求包含 `code_challenge` 和 `code_challenge_method=S256`
4. Token 请求包含 `code_verifier`
5. 服务器验证 `code_verifier` 和 `code_challenge` 的匹配关系

## MFA 流程

1. 用户启用 MFA：
   ```http
   POST /api/auth/mfa/generate
   ```
   返回：secret、QR Code URL

2. 用户扫描 QR Code 或输入手动 code

3. 确认 MFA：
   ```http
   POST /api/auth/mfa/confirm
   {
     "username": "user",
     "secretId": "xxx",
     "code": "123456"
   }
   ```

## 社会化登录流程

### Google 登录

```http
POST /api/auth/social/google
{
  "token": "google-id-token"
}
```

### 微信登录

```http
POST /api/auth/social/wechat
{
  "token": "wechat-auth-code"
}
```

## 安全特性

- ✅ CSRF 防护（通过 PKCE）
- ✅ XSS 防护（输出编码）
- ✅ Token 黑名单
- ✅ 令牌指纹
- ✅ 设备管理
- ✅ 审计日志
- ✅ 密码强度验证
- ✅ 速率限制（待实现）

## 运行

```bash
mvn spring-boot:run
```

## 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify
```

## 许可证

Apache License 2.0
