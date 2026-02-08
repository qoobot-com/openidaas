# OpenIDaaS Spring Boot Starter

## 📦 模块概述

`openidaas-starter` 是 OpenIDaaS 系统的 Spring Boot Starter，提供开箱即用的自动配置功能。

---

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>com.qoobot.openidaas</groupId>
    <artifactId>openidaas-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 基本配置

```yaml
openidaas:
  enabled: true
  auth:
    jwt:
      secret: your-secret-key-change-in-production
      expiration: 3600
  security:
    password-policy:
      min-length: 8
  tenant:
    isolation-strategy: SCHEMA
```

### 显式启用（可选）

```java
@SpringBootApplication
@EnableOpenIDaaS
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

---

## 📋 功能特性

### 自动装配

- ✅ **条件装配**: 根据 classpath 和配置自动装配
- ✅ **属性绑定**: 自动绑定 `openidaas.*` 配置
- ✅ **防止重复**: `@ConditionalOnMissingBean` 防止重复 Bean
- ✅ **健康检查**: 集成 Spring Boot Actuator

### 核心模块

- ✅ **认证模块**: OAuth2.1, OIDC, JWT
- ✅ **安全模块**: 密码策略, MFA, 限流
- ✅ **租户模块**: 多租户隔离
- ✅ **用户模块**: 用户管理
- ✅ **网关模块**: API Gateway 集成

---

## ⚙️ 配置属性

### 全局配置

```yaml
openidaas:
  enabled: true                    # 是否启用 OpenIDaaS
  database:
    auto-init: false              # 是否自动初始化数据库
```

### 认证配置

```yaml
openidaas:
  auth:
    enabled: true

    # JWT 配置
    jwt:
      enabled: true
      secret: your-secret-key
      expiration: 3600              # Access Token 过期时间（秒）
      refresh-expiration: 2592000   # Refresh Token 过期时间（秒）
      issuer: openidaas
      algorithm: HS256              # HS256 或 RS256

    # OAuth2 配置
    oauth2:
      enabled: true
      authorization-code-validity: 300      # 授权码有效期（秒）
      access-token-validity: 3600          # 访问令牌有效期（秒）
      refresh-token-validity: 2592000      # 刷新令牌有效期（秒）
      require-proof-key: false
      require-authorization-consent: false

    # OIDC 配置
    oidc:
      enabled: true
      user-info-enabled: true
      client-registration-enabled: true

    # 会话配置
    session:
      timeout: 1800                  # 会话超时（秒）
      remember-me-duration: 2592000  # 记住我（秒）
      max-concurrent-sessions: 5      # 最大并发会话数
      allow-concurrent-login: true
      session-fixation-protection: migrateSession
```

### 安全配置

```yaml
openidaas:
  security:
    enabled: true

    # 密码策略
    password-policy:
      min-length: 8
      max-length: 128
      require-uppercase: true
      require-lowercase: true
      require-numbers: true
      require-special-chars: true
      password-history: 5              # 密码历史记录数量
      expiration-days: 90             # 密码过期天数

    # MFA 配置
    mfa:
      enabled: false
      required-for-admin: true
      supported-types: ["TOTP", "SMS", "EMAIL"]
      default-type: TOTP
      backup-codes-count: 10
      totp-validity: 30
      remember-device: true

    # 限流配置
    rate-limit:
      enabled: true
      algorithm: TOKEN_BUCKET          # TOKEN_BUCKET, FIXED_WINDOW, SLIDING_WINDOW
      requests-per-minute: 100
      requests-per-hour: 1000
      requests-per-day: 10000
      bucket-capacity: 100
      refill-rate: 10                 # Token 填充速率（每秒）

    # 访问控制
    access-control:
      enabled: true
      default-policy: DENY_ALL         # DENY_ALL 或 PERMIT_ALL
      ip-whitelist-enabled: false
      ip-blacklist-enabled: false
      cors-enabled: true
      cors-allowed-origins: ["*"]
      cors-allowed-methods: ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
      cors-allowed-headers: ["*"]
```

### 租户配置

```yaml
openidaas:
  tenant:
    enabled: true
    isolation-strategy: SCHEMA        # NONE, SCHEMA, DATABASE
    default-tenant-id: 00000000-0000-0000-0000-000000000001
    tenant-resolver: HEADER          # HEADER, COOKIE, PATH
    tenant-header-name: X-Tenant-ID
    tenant-cookie-name: tenant_id
    cache-enabled: true
    cache-expiration: 3600
```

### 监控配置

```yaml
openidaas:
  monitoring:
    enabled: true
    metrics-enabled: true
    metrics-export: PROMETHEUS        # PROMETHEUS, INFLUX, LOGGING
    metrics-prefix: openidaas
    audit-enabled: true
    audit-level: INFO                # INFO, WARN, ERROR, ALL
    tracing-enabled: false
    tracing-sampling-rate: 0.1
    health-check-enabled: true
```

### 缓存配置

```yaml
openidaas:
  cache:
    type: REDIS                       # REDIS, HAZELCAST, CAFFEINE, SIMPLE
    default-expiration: 1800
    user-info-expiration: 1800
    token-expiration: 3600
    permission-expiration: 600
    local-cache-enabled: true
    local-cache-max-size: 1000
```

---

## 🏥 健康检查

启用 Actuator 后，可以通过 `/actuator/health` 端点查看 OpenIDaaS 健康状态：

```bash
curl http://localhost:8080/actuator/health
```

响应示例：

```json
{
  "status": "UP",
  "components": {
    "openidaas": {
      "status": "UP",
      "details": {
        "enabled": true,
        "version": "1.0.0",
        "modules": {
          "auth": true,
          "security": true,
          "tenant": true
        },
        "database": "UP",
        "configuration": "OK"
      }
    }
  }
}
```

---

## 🔧 自定义配置

### 自定义 PasswordEncoder

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

### 自定义 JWT 配置

```java
@Bean
public JwtConfiguration jwtConfiguration(OpenIDaaSProperties properties) {
    // 自定义 JWT 配置
    return new JwtConfiguration(customProperties);
}
```

### 禁用特定模块

```yaml
openidaas:
  auth:
    enabled: false
  mfa:
    enabled: false
```

或使用注解：

```java
@EnableOpenIDaaS(
    enableAuth = false,
    enableMfa = false
)
```

---

## 📊 监控指标

OpenIDaaS 集成 Micrometer，自动收集以下指标：

- `openidaas.authentication.success` - 认证成功次数
- `openidaas.authentication.failure` - 认证失败次数
- `openidaas.token.issued` - Token 签发次数
- `openidaas.token.refreshed` - Token 刷新次数
- `openidaas.user.created` - 用户创建次数
- `openidaas.password.changed` - 密码修改次数

---

## 🧪 测试

### 单元测试

```bash
mvn test
```

### 集成测试

```bash
mvn verify
```

---

## 🔍 故障排除

### 自动配置未生效

1. 检查 `openidaas.enabled` 配置
2. 检查 classpath 中是否包含所需依赖
3. 查看启动日志中的自动配置报告

```bash
java -jar app.jar --debug
```

### 配置属性未绑定

1. 检查配置文件位置和格式
2. 确认属性名拼写正确
3. 查看 Spring Boot 配置元数据

### 健康检查失败

1. 检查数据库连接
2. 检查 JWT secret 配置
3. 检查租户隔离策略配置

---

## 📚 相关文档

- [数据库设计文档](../openidaas-core/DATABASE_DESIGN.md)
- [用户管理模块](../openidaas-user/USER_MANAGEMENT_README.md)
- [安全模块](../openidaas-security/SECURITY_MODULE_README.md)
- [网关模块](../openidaas-gateway/GATEWAY_MODULE_README.md)

---

## 📝 示例代码

### 完整配置示例

```yaml
openidaas:
  enabled: true

  auth:
    enabled: true
    jwt:
      enabled: true
      secret: ${JWT_SECRET:your-secret-key}
      expiration: 3600
      refresh-expiration: 2592000
    session:
      timeout: 1800
      max-concurrent-sessions: 5

  security:
    enabled: true
    password-policy:
      min-length: 8
      require-uppercase: true
      require-lowercase: true
      require-numbers: true
      require-special-chars: true
      password-history: 5
    mfa:
      enabled: false
    rate-limit:
      enabled: true
      requests-per-minute: 100

  tenant:
    enabled: true
    isolation-strategy: SCHEMA
    cache-enabled: true

  monitoring:
    enabled: true
    metrics-enabled: true
    audit-enabled: true
    health-check-enabled: true

  cache:
    type: REDIS
    default-expiration: 1800

management:
  endpoints:
    web:
      exposure:
        include: health, metrics, prometheus
  health:
    openidaas:
      enabled: true
```

### Java 配置示例

```java
@SpringBootApplication
@EnableOpenIDaaS(
    enableAuth = true,
    enableUser = true,
    enableTenant = true,
    enableSecurity = true,
    enableHealthCheck = true
)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

---

**版本**: 1.0.0
**更新时间**: 2026-02-08
**维护者**: OpenIDaaS Team
