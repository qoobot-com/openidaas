# OpenIDaaS API 版本管理指南

## 目录

- [概述](#概述)
- [版本策略](#版本策略)
- [API 路径规范](#api-路径规范)
- [请求头版本控制](#请求头版本控制)
- [网关路由配置](#网关路由配置)
- [服务端实现](#服务端实现)
- [版本兼容性](#版本兼容性)
- [版本废弃流程](#版本废弃流程)
- [最佳实践](#最佳实践)

## 概述

OpenIDaaS 采用 **URL 路径版本控制** 方案，为所有 API 提供版本管理能力。

### 版本控制的重要性

- ✅ 向后兼容性
- ✅ 平滑升级
- ✅ 多版本共存
- ✅ 逐步迁移

### 版本格式

```
/api/v{version}/{resource}

示例:
/api/v1/users
/api/v1/auth/login
/api/v1/organizations
/api/v2/users  # 新版本
```

## 版本策略

### 语义化版本控制

| 版本 | 说明 | 生命周期 |
|-----|------|---------|
| v1 | 稳定版本 | 长期支持 |
| v2 | 新功能版本 | 主要版本 |
| v3 | 实验性版本 | 早期采用 |

### 版本升级规则

| 变更类型 | 版本升级 | 示例 |
|---------|---------|------|
| 破坏性变更 | 主版本 | v1 → v2 |
| 新增功能 | 主版本 | v1 → v2 |
| 向后兼容更新 | 不变 | v1.x |
| Bug 修复 | 不变 | v1.x |

## API 路径规范

### 标准路径格式

```
/api/v{version}/{resource}/{id}

示例:
POST   /api/v1/auth/login
GET    /api/v1/users
GET    /api/v1/users/{id}
POST   /api/v1/users
PUT    /api/v1/users/{id}
DELETE /api/v1/users/{id}
```

### 资源路径规范

| 资源 | v1 路径 | v2 路径（示例） |
|-----|---------|---------------|
| 认证 | `/api/v1/auth` | `/api/v2/auth` |
| 用户 | `/api/v1/users` | `/api/v2/users` |
| 组织 | `/api/v1/organizations` | `/api/v2/organizations` |
| 角色 | `/api/v1/roles` | `/api/v1/roles` |
| 应用 | `/api/v1/applications` | `/api/v1/applications` |
| 审计 | `/api/v1/audit` | `/api/v1/audit` |
| 授权 | `/api/v1/authorization` | `/api/v1/authorization` |

### 路径对比

#### 旧版（无版本控制）

```yaml
/api/auth/login      # 认证
/api/users           # 用户
/api/organizations   # 组织
```

#### 新版（有版本控制）

```yaml
/api/v1/auth/login      # 认证 v1
/api/v1/users           # 用户 v1
/api/v1/organizations   # 组织 v1

/api/v2/auth/login      # 认证 v2（新增功能）
/api/v2/users           # 用户 v2（新字段）
```

## 请求头版本控制

除了 URL 路径版本控制，也支持通过请求头指定版本。

### 请求头

```http
API-Version: v1
Accept: application/vnd.openidaas.v1+json
```

### 支持的版本格式

| 请求头 | 格式 | 说明 |
|-------|------|------|
| API-Version | `v1`, `v2` | 简单版本 |
| Accept | `application/vnd.openidaas.v1+json` | 内容协商 |
| X-API-Version | `v1`, `v2` | 自定义头 |

### 优先级

1. URL 路径版本（优先级最高）
2. API-Version 请求头
3. Accept 头
4. 默认版本 v1

## 网关路由配置

### Spring Cloud Gateway 配置

#### v1 版本路由

```yaml
spring:
  cloud:
    gateway:
      routes:
        # 认证服务 v1
        - id: auth-service-v1
          uri: lb://openidaas-auth-service
          predicates:
            - Path=/api/v1/auth/**
          filters:
            - StripPrefix=2
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
                key-resolver: "#{@ipKeyResolver}"

        # 用户服务 v1
        - id: user-service-v1
          uri: lb://openidaas-user-service
          predicates:
            - Path=/api/v1/users/**
          filters:
            - StripPrefix=2
            - name: JwtAuthentication
              args:
                requireAuth: true

        # 组织服务 v1
        - id: organization-service-v1
          uri: lb://openidaas-organization-service
          predicates:
            - Path=/api/v1/organizations/**
          filters:
            - StripPrefix=2
            - name: JwtAuthentication
              args:
                requireAuth: true

        # 角色服务 v1
        - id: role-service-v1
          uri: lb://openidaas-role-service
          predicates:
            - Path=/api/v1/roles/**
          filters:
            - StripPrefix=2
            - name: JwtAuthentication
              args:
                requireAuth: true

        # 应用服务 v1
        - id: application-service-v1
          uri: lb://openidaas-application-service
          predicates:
            - Path=/api/v1/applications/**
          filters:
            - StripPrefix=2
            - name: JwtAuthentication
              args:
                requireAuth: true

        # 审计服务 v1
        - id: audit-service-v1
          uri: lb://openidaas-audit-service
          predicates:
            - Path=/api/v1/audit/**
          filters:
            - StripPrefix=2
            - name: JwtAuthentication
              args:
                requireAuth: true

        # 授权服务 v1
        - id: authorization-service-v1
          uri: lb://openidaas-authorization-service
          predicates:
            - Path=/api/v1/authorization/**
          filters:
            - StripPrefix=2
            - name: JwtAuthentication
              args:
                requireAuth: true
```

#### v2 版本路由（预留）

```yaml
        # 认证服务 v2（未来版本）
        - id: auth-service-v2
          uri: lb://openidaas-auth-service
          predicates:
            - Path=/api/v2/auth/**
          filters:
            - StripPrefix=2

        # 用户服务 v2（未来版本）
        - id: user-service-v2
          uri: lb://openidaas-user-service
          predicates:
            - Path=/api/v2/users/**
          filters:
            - StripPrefix=2
```

### 旧路径兼容（可选）

为了平滑迁移，保留旧路径并重定向到新版本：

```yaml
        # 旧路径重定向到 v1
        - id: legacy-auth-redirect
          uri: lb://openidaas-auth-service
          predicates:
            - Path=/api/auth/**
          filters:
            - RewritePath=/api/auth/(?<segment>.*), /api/v1/auth/${segment}

        - id: legacy-users-redirect
          uri: lb://openidaas-user-service
          predicates:
            - Path=/api/users/**
          filters:
            - RewritePath=/api/users/(?<segment>.*), /api/v1/users/${segment}
```

## 服务端实现

### Controller 版本化

#### 方式一：路径前缀

```java
@Slf4j
@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    @PostMapping("/login")
    public ResultVO<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        // v1 实现
    }

    @PostMapping("/logout")
    public ResultVO<Void> logout() {
        // v1 实现
    }
}

@Slf4j
@RestController
@RequestMapping("/v2/auth")
public class AuthControllerV2 {

    @PostMapping("/login")
    public ResultVO<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        // v2 实现（可能包含新功能）
    }

    @PostMapping("/login/mfa")
    public ResultVO<LoginVO> loginWithMFA(@Valid @RequestBody LoginMFA dto) {
        // v2 新功能
    }
}
```

#### 方式二：继承和多态

```java
// 基础 Controller
@Slf4j
public abstract class BaseAuthController {
    // 公共方法
}

// v1 实现
@RestController
@RequestMapping("/v1/auth")
public class AuthControllerV1 extends BaseAuthController {
    // v1 特定方法
}

// v2 实现
@RestController
@RequestMapping("/v2/auth")
public class AuthControllerV2 extends BaseAuthController {
    // v2 特定方法，可以覆盖父类方法
}
```

### Service 层版本化

```java
// v1 Service
@Service
public class UserServiceV1 {
    public UserVO createUser(UserCreateDTO dto) {
        // v1 逻辑
    }
}

// v2 Service（继承 v1）
@Service
public class UserServiceV2 extends UserServiceV1 {
    @Override
    public UserVO createUser(UserCreateDTO dto) {
        // v2 逻辑，可以调用 super.createUser(dto)
    }

    public UserVO createUserV2(UserCreateDTOV2 dto) {
        // v2 新方法
    }
}
```

### DTO 版本化

```java
// v1 DTO
@Data
public class UserVO {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
}

// v2 DTO（新增字段）
@Data
public class UserVOV2 extends UserVO {
    private String phone;           // 新增
    private String avatar;           // 新增
    private List<String> tags;       // 新增
}
```

### 版本信息响应

所有 API 响应应包含版本信息：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "testuser"
  },
  "meta": {
    "version": "v1",
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "abc123"
  }
}
```

## 版本兼容性

### 兼容性矩阵

| 旧版本 | 新版本 | 兼容性 |
|-------|-------|--------|
| v1 | v1 | ✅ 完全兼容 |
| v1 | v2 | ⚠️ 部分兼容 |
| v1 | v3 | ❌ 不兼容 |

### 向后兼容原则

#### 必须保持兼容

- ✅ 不删除 API 端点
- ✅ 不修改请求参数类型
- ✅ 不修改响应字段类型
- ✅ 新增字段使用可选

#### 可以破坏兼容

- ⚠️ 删除废弃字段（需提前通知）
- ⚠️ 修改字段语义（需提前通知）
- ⚠️ 修改认证方式（需提前通知）

### 兼容性测试

```java
@SpringBootTest
@ActiveProfiles("test")
class APICompatibilityTest {

    @Test
    void testV1AndV2Compatibility() {
        // 测试 v1 和 v2 兼容性
        UserVO v1 = userServiceV1.getUser(1L);
        UserVOV2 v2 = userServiceV2.getUser(1L);
        // 验证 v1 字段在 v2 中存在
        assertEquals(v1.getId(), v2.getId());
        assertEquals(v1.getUsername(), v2.getUsername());
    }
}
```

## 版本废弃流程

### 废弃时间线

```
v1 版本生命周期:

发布 → 稳定期 → 废弃通知 → 废弃期 → 停止支持
  ↓        ↓         ↓          ↓         ↓
 当前    12个月    6个月前    3个月前    现在
```

### 废弃通知

#### API 响应头

```http
X-API-Version: v1
X-API-Deprecated: true
X-API-Deprecation-Date: 2024-06-01
X-API-Sunset-Date: 2024-09-01
X-API-Recommended-Version: v2
Link: <https://docs.openidaas.com/api/v2>; rel="successor-version"
```

#### 响应体

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "warnings": [
    {
      "code": "DEPRECATED_API",
      "message": "This API version is deprecated and will be removed on 2024-09-01",
      "recommendation": "Please migrate to API v2",
      "documentationUrl": "https://docs.openidaas.com/api/v2"
    }
  ]
}
```

### 废弃流程

1. **发布新版本**（v2）
2. **标记旧版本废弃**（v1）
3. **提供迁移指南**
4. **发送通知**（邮件、公告）
5. **3个月废弃期**
6. **停止支持**

## 最佳实践

### 1. 版本命名

```java
// ✅ 推荐
@RequestMapping("/v1/users")
@RequestMapping("/v2/users")

// ❌ 不推荐
@RequestMapping("/v1_0/users")
@RequestMapping("/v1.0/users")
@RequestMapping("/version1/users")
```

### 2. 文档管理

- ✅ 为每个版本维护独立文档
- ✅ 明确标记废弃 API
- ✅ 提供迁移指南
- ✅ 保持文档同步

### 3. 监控和告警

```yaml
# 监控旧版本 API 调用量
alert: HighLegacyAPICallRate
expr: |
  rate(http_server_requests_seconds_count{version="v1"}[5m])
  /
  rate(http_server_requests_seconds_count[5m])
  > 0.3
for: 7d
labels:
  severity: warning
annotations:
  summary: "旧版本 API 调用率过高"
  description: "v1 API 调用率超过30%，建议客户升级到 v2"
```

### 4. 逐步迁移

```bash
# 客户端迁移步骤
1. 添加 v2 API 支持
2. 新请求使用 v2
3. 旧请求继续使用 v1
4. 逐步替换 v2
5. 移除 v1
```

### 5. 测试策略

```java
// 多版本并行测试
@SpringBootTest
@ActiveProfiles("test")
class MultiVersionAPITest {

    @Test
    void testV1AndV2ReturnSameResult() {
        // 确保同一操作在 v1 和 v2 返回相同结果
    }

    @Test
    void testV2HasAdditionalFields() {
        // 确保 v2 有额外字段
    }
}
```

### 6. API 网关配置

```yaml
# 默认版本路由（未指定版本时）
- id: default-api-version
  uri: lb://openidaas-auth-service
  predicates:
    - Path=/api/**
    - Header=API-Version,  (.*)
  filters:
    - AddRequestHeader=API-Version, v1
```

## 示例

### 完整的 API 版本化示例

#### Controller

```java
@Slf4j
@RestController
@RequestMapping("/v1/users")
@Tag(name = "用户管理 v1", description = "用户管理 API v1 版本")
public class UserController {

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public ResultVO<UserVO> getUser(@PathVariable Long id) {
        // v1 实现
    }

    @PostMapping
    @Operation(summary = "创建用户")
    public ResultVO<UserVO> createUser(@Valid @RequestBody UserCreateDTO dto) {
        // v1 实现
    }
}

@Slf4j
@RestController
@RequestMapping("/v2/users")
@Tag(name = "用户管理 v2", description = "用户管理 API v2 版本")
public class UserControllerV2 {

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public ResultVO<UserVOV2> getUser(@PathVariable Long id) {
        // v2 实现（返回更多信息）
    }

    @PostMapping
    @Operation(summary = "创建用户")
    public ResultVO<UserVOV2> createUser(@Valid @RequestBody UserCreateDTOV2 dto) {
        // v2 实现（支持更多字段）
    }
}
```

#### 调用示例

```bash
# v1 API
curl -X GET http://localhost:8080/api/v1/users/1 \
  -H "Authorization: Bearer <token>"

# v2 API
curl -X GET http://localhost:8080/api/v2/users/1 \
  -H "Authorization: Bearer <token>"
```

---

## 附录

### 版本控制对比

| 方案 | 优点 | 缺点 |
|-----|------|------|
| URL 路径 | 简单直观、易于调试 | URL 变长 |
| 请求头 | URL 简洁 | 不直观、难以调试 |
| 内容协商 | RESTful 标准 | 复杂度高 |

### 参考资源

- [Semantic Versioning](https://semver.org/)
- [API Versioning Best Practices](https://restfulapi.net/versioning/)
- [Microsoft REST API Guidelines](https://github.com/microsoft/api-guidelines/blob/vNext/Guidelines.md)

---

**文档版本**: 1.0  
**最后更新**: 2024-01-15
