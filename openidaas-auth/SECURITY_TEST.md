# OpenIDaaS Authentication Service - Security Test

## 测试环境

- 认证服务: http://localhost:8081
- 客户端 ID: openidaas-client
- 客户端密钥: openidaas-secret
- 测试用户: admin / OpenIdaas@123

## 测试用例

### 1. OAuth2.1 Authorization Code Flow

#### 1.1 生成 PKCE 参数

```bash
# Code Verifier（随机字符串）
CODE_VERIFIER="xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"

# Code Challenge (SHA256 + Base64URL)
CODE_CHALLENGE=$(echo -n "$CODE_VERIFIER" | openssl dgst -sha256 -binary | base64 | tr -d '=' | tr '/+' '_-' | tr -d '\n')

echo "Code Verifier: $CODE_VERIFIER"
echo "Code Challenge: $CODE_CHALLENGE"
```

#### 1.2 授权请求

```bash
curl -X GET "http://localhost:8081/oauth2/authorize? \
  response_type=code& \
  client_id=openidaas-client& \
  redirect_uri=http://localhost:8084/authorized& \
  scope=openid%20profile%20email& \
  state=xyz& \
  code_challenge=$CODE_CHALLENGE& \
  code_challenge_method=S256"
```

**预期结果**：
- 返回授权码
- 重定向到 redirect_uri

#### 1.3 Token 请求

```bash
curl -X POST "http://localhost:8081/oauth2/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Basic b3BlbmlkYWFzLWNsaWVudDpvcGVuaWRhYXMtc2VjcmV0" \
  -d "grant_type=authorization_code" \
  -d "code=AUTHORIZATION_CODE" \
  -d "redirect_uri=http://localhost:8084/authorized" \
  -d "code_verifier=$CODE_VERIFIER"
```

**预期结果**：
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "xxx",
  "scope": "openid profile email"
}
```

### 2. PKCE 防重放攻击测试

#### 2.1 使用错误的 code_verifier

```bash
curl -X POST "http://localhost:8081/oauth2/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Basic b3BlbmlkYWFzLWNsaWVudDpvcGVuaWRhYXMtc2VjcmV0" \
  -d "grant_type=authorization_code" \
  -d "code=AUTHORIZATION_CODE" \
  -d "redirect_uri=http://localhost:8084/authorized" \
  -d "code_verifier=WRONG_VERIFIER"
```

**预期结果**：
- 返回 400 Bad Request
- 错误：invalid_grant（PKCE 验证失败）

#### 2.2 重放测试（使用相同的 code）

```bash
# 第一次请求（成功）
curl -X POST "http://localhost:8081/oauth2/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code" \
  -d "code=AUTHORIZATION_CODE" \
  -d "redirect_uri=http://localhost:8084/authorized" \
  -d "code_verifier=$CODE_VERIFIER"

# 第二次请求（失败）
curl -X POST "http://localhost:8081/oauth2/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code" \
  -d "code=AUTHORIZATION_CODE" \
  -d "redirect_uri=http://localhost:8084/authorized" \
  -d "code_verifier=$CODE_VERIFIER"
```

**预期结果**：
- 第一次请求成功
- 第二次请求失败（code 已使用）

### 3. MFA 认证流程测试

#### 3.1 生成 MFA Secret

```bash
curl -X POST "http://localhost:8081/api/auth/mfa/generate" \
  -H "Authorization: Bearer ACCESS_TOKEN"
```

**预期结果**：
```json
{
  "secretId": "admin-1234567890",
  "secret": "JBSWY3DPEHPK3PXP",
  "qrCodeUrl": "otpauth://totp/OpenIDaaS:admin?secret=JBSWY3DPEHPK3PXP&issuer=OpenIDaaS",
  "verificationCode": 123456,
  "expiresAt": 1234567890
}
```

#### 3.2 确认 MFA

```bash
curl -X POST "http://localhost:8081/api/auth/mfa/confirm" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "secretId": "admin-1234567890",
    "code": "123456"
  }'
```

**预期结果**：
- 返回 true（确认成功）
- MFA 激活

#### 3.3 登录时验证 MFA

```bash
# 第一步：用户名密码登录
curl -X POST "http://localhost:8081/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "OpenIdaas@123"
  }'

# 第二步：MFA 验证
curl -X POST "http://localhost:8081/api/auth/mfa/verify" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "code": "654321"
  }'
```

**预期结果**：
- 返回 Access Token（MFA 验证成功）

### 4. Token 撤销测试

#### 4.1 撤销 Access Token

```bash
curl -X POST "http://localhost:8081/oauth2/revoke" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Basic b3BlbmlkYWFzLWNsaWVudDpvcGVuaWRhYXMtc2VjcmV0" \
  -d "token=ACCESS_TOKEN" \
  -d "token_type_hint=access_token"
```

**预期结果**：
- 返回 200 OK
- Token 已撤销

#### 4.2 使用撤销的 Token（应该失败）

```bash
curl -X GET "http://localhost:8081/api/user/profile" \
  -H "Authorization: Bearer REVOKED_TOKEN"
```

**预期结果**：
- 返回 401 Unauthorized
- 错误：Token has been revoked

### 5. Token 刷新测试

#### 5.1 刷新 Access Token

```bash
curl -X POST "http://localhost:8081/oauth2/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Basic b3BlbmlkYWFzLWNsaWVudDpvcGVuaWRhYXMtc2VjcmV0" \
  -d "grant_type=refresh_token" \
  -d "refresh_token=REFRESH_TOKEN"
```

**预期结果**：
```json
{
  "access_token": "new_access_token",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "new_refresh_token"
}
```

### 6. 社会化登录测试

#### 6.1 Google 登录

```bash
curl -X POST "http://localhost:8081/api/auth/social/google" \
  -H "Content-Type: application/json" \
  -d '{
    "token": "google_id_token"
  }'
```

**预期结果**：
```json
{
  "access_token": "access_token",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

## 安全最佳实践验证

### ✅ CSRF 防护
- PKCE 已启用
- state 参数验证
- code 一性使用

### ✅ XSS 防护
- 所有输出编码
- Content-Type: application/json
- CSP 头部配置

### ✅ Token 安全
- JWT 签名验证
- Token 黑名单
- 过期时间检查
- 刷新令牌轮换

### ✅ MFA 安全
- TOTP 时间窗口验证
- 备用码支持
- 验证码频率限制

### ✅ 审计日志
- 所有认证事件记录
- IP 地址和 User-Agent 记录
- Token 生命周期追踪

## 自动化测试脚本

```bash
#!/bin/bash

echo "=== OpenIDaaS Security Test ==="

# 测试 1: Authorization Code Flow
echo "Test 1: Authorization Code Flow"
./test_authorization_code_flow.sh

# 测试 2: PKCE
echo "Test 2: PKCE Verification"
./test_pkce.sh

# 测试 3: MFA
echo "Test 3: MFA Flow"
./test_mfa.sh

# 测试 4: Token Revocation
echo "Test 4: Token Revocation"
./test_token_revocation.sh

# 测试 5: Refresh Token
echo "Test 5: Refresh Token"
./test_refresh_token.sh

echo "=== All Tests Completed ==="
```

## 持续集成

建议使用 GitHub Actions 或 Jenkins 运行自动化安全测试：

```yaml
# .github/workflows/security-test.yml
name: Security Tests

on: [push, pull_request]

jobs:
  security-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Start Services
        run: docker-compose up -d
      - name: Run Security Tests
        run: mvn verify -Psecurity-tests
      - name: Upload Results
        uses: actions/upload-artifact@v2
        with:
          name: security-test-results
          path: target/security-tests/
```

## 性能测试

使用 JMeter 或 Gatling 进行性能测试：

```bash
# 模拟 1000 并发登录请求
jmeter -n 1000 -c 100 -t login_test.jmx

# 测试 Token 生成性能
jmeter -n 10000 -c 500 -t token_generation_test.jmx
```

## 结论

所有测试应通过以下标准：
- ✅ OAuth2.1 流程完整性
- ✅ PKCE 代码交换验证
- ✅ Token 防重放攻击
- ✅ MFA 认证流程
- ✅ Token 撤销功能
- ✅ Token 刷新功能
- ✅ 社会化登录集成
- ✅ 安全头部配置
- ✅ 审计日志记录
