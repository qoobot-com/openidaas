# OpenIDaaS 安全模块文档

## 概述

openidaas-security 模块提供企业级安全防护功能，包括认证安全、授权安全、传输安全和审计安全。

## 核心能力

### 1. 认证安全

#### 防暴力破解
- **Rate Limiting**: 基于令牌桶算法的限流
- **登录失败锁定**: 失败5次锁定30分钟
- **IP限流**: 登录接口每IP 5次/分钟

#### 密码策略
- **强度验证**: 最小8位，包含大小写字母和数字
- **过期管理**: 90天自动过期
- **历史记录**: 禁止使用最近5次密码
- **强度评分**: 实时密码强度评估

#### 多因子认证 (MFA)
- **TOTP支持**: Google Authenticator兼容
- **QR码生成**: 自动生成二维码
- **窗口期验证**: 支持时间偏差

#### 风险登录检测
- **异常IP检测**: 识别异常登录位置
- **行为分析**: 异常行为标记
- **实时告警**: 安全事件即时通知

### 2. 授权安全

#### RBAC权限模型
- **基于角色的访问控制**
- **角色层次结构**
- **权限继承**
- **权限分配**

#### ABAC属性权限
- **基于属性的访问控制**
- **动态权限判断**
- **数据范围控制**

#### 数据权限控制
- **全部数据**
- **本部门及以下**
- **仅本部门**
- **仅本人**

#### 接口权限验证
- **方法级注解**
- **URL拦截**
- **动态权限加载**

### 3. 传输安全

#### TLS 1.3加密
- **HTTPS强制**
- **HSTS头**
- **安全头配置**

#### JWT Token保护
- **Token签名**
- **Token刷新**
- **Token黑名单**
- **过期管理**

#### CORS安全配置
- **白名单控制**
- **凭证管理**
- **预检缓存**

#### CSP安全策略
- **内容安全策略**
- **XSS防护**
- **CSRF防护**

### 4. 审计安全

#### 操作日志记录
- **用户操作**
- **系统操作**
- **敏感操作标记**
- **异步写入**

#### 安全日志分析
- **Elasticsearch存储**
- **日志查询**
- **统计分析**
- **异常检测**

#### 异常行为检测
- **登录异常**
- **权限异常**
- **数据异常**
- **实时告警**

#### 合规报告生成
- **审计报告**
- **合规检查**
- **风险评估**

## 技术实现

### 认证安全
- **Spring Security**: 认证框架
- **Bucket4j**: 限流算法
- **BCrypt**: 密码加密
- **Google Authenticator**: MFA

### 密码策略
- **Passay**: 密码验证
- **自定义策略**: 强度评估

### MFA支持
- **ZXing**: QR码生成
- **Google Authenticator**: TOTP

### 审计日志
- **Spring AOP**: 切面编程
- **Elasticsearch**: 日志存储

### 防护机制
- **CSRF**: Spring Security内置
- **XSS**: CSP头
- **SQL注入**: JPA参数化查询

## 配置说明

### 安全配置属性

```yaml
openidaas:
  security:
    jwt:
      secret: your-secret-key
      expiration-hours: 24
      refresh-expiration-days: 7

    password:
      min-length: 8
      require-uppercase: true
      expire-days: 90
      max-failed-attempts: 5
      lock-minutes: 30

    rate-limit:
      enabled: true
      login-capacity: 5
      api-capacity: 100

    mfa:
      enabled: true
      code-length: 6

    audit:
      enabled: true
      retention-days: 90
```

## 使用示例

### 使用@AuditLog注解

```java
@Service
public class UserService {

    @AuditLog(
        operation = "CREATE_USER",
        module = "USER",
        description = "创建用户",
        riskLevel = RiskLevel.MEDIUM
    )
    public User createUser(CreateUserRequest request) {
        // 业务逻辑
    }
}
```

### 密码验证

```java
@Autowired
private PasswordPolicyValidator passwordValidator;

public void changePassword(String newPassword) {
    passwordValidator.validate(newPassword);
    // 密码策略验证通过
}
```

### MFA验证

```java
@Autowired
private MfaService mfaService;

// 生成MFA密钥
MfaKeyInfo keyInfo = mfaService.generateSecret("username");

// 验证MFA代码
boolean valid = mfaService.verifyCode(secret, code);
```

### 限流检查

```java
@Autowired
private RateLimitService rateLimitService;

boolean allowed = rateLimitService.tryAcquire(
    "user:123",
    "api",
    100,
    Duration.ofSeconds(1),
    50
);
```

## 安全测试

### 暴力破解防护验证

```bash
# 测试登录限流
for i in {1..10}; do
  curl -X POST http://localhost:8085/api/auth/login \
    -d '{"username":"test","password":"wrong"}'
done
# 预期：第6次请求返回429 Too Many Requests
```

### 密码策略执行验证

```bash
# 测试弱密码
curl -X POST http://localhost:8085/api/users/password \
  -H "Authorization: Bearer TOKEN" \
  -d '{"newPassword":"123"}'
# 预期：返回密码策略违规错误
```

### MFA认证流程验证

```bash
# 生成MFA密钥
curl http://localhost:8085/api/auth/mfa/generate \
  -H "Authorization: Bearer TOKEN"

# 验证MFA代码
curl -X POST http://localhost:8085/api/auth/mfa/verify \
  -H "Authorization: Bearer TOKEN" \
  -d '{"code":"123456"}'
```

### 安全审计日志验证

```bash
# 查询审计日志
curl http://localhost:9200/audit-logs-*/_search
```

## 性能优化

### 缓存策略
- **JWT黑名单**: Redis缓存，TTL与Token过期时间一致
- **登录失败计数**: Redis缓存，TTL 1小时
- **账户锁定**: Redis缓存，TTL 30分钟

### 异步处理
- **审计日志**: 异步写入Elasticsearch
- **安全事件**: 异步处理

### 连接池优化
- **Redis连接池**: 最大50连接
- **数据库连接池**: 根据负载调整

## 监控告警

### 关键指标

```yaml
# 登录失败率
login_failure_rate > 10% → Warning
login_failure_rate > 30% → Critical

# 账户锁定数
locked_accounts > 100 → Warning
locked_accounts > 1000 → Critical

# 异常登录
abnormal_login_count > 50 → Warning
abnormal_login_count > 200 → Critical
```

### 告警渠道
- **邮件**: 安全团队
- **短信**: 管理员
- **Slack**: 实时通知

## 合规性

### 安全标准
- **ISO 27001**: 信息安全
- **OWASP Top 10**: Web安全
- **GDPR**: 数据保护
- **SOC 2**: 安全控制

### 审计要求
- **操作可追溯**: 90天日志保留
- **权限最小化**: 最小权限原则
- **数据加密**: 传输和存储加密

## 常见问题

### Q: 如何调整密码策略？

A: 修改 `openidaas.security.password` 配置项。

### Q: 如何禁用MFA？

A: 设置 `openidaas.security.mfa.enabled=false`。

### Q: 如何自定义安全头？

A: 修改 `SecurityConfig` 中的头部配置。

## 许可证

Apache License 2.0
