# OpenFeign 服务间通信使用指南

## 概述

OpenIDaaS 使用 OpenFeign 实现微服务之间的通信。所有 Feign 客户端接口定义在 `openidaas-common` 模块中。

## 目录

- [依赖配置](#依赖配置)
- [启用 Feign](#启用-feign)
- [Feign 客户端](#feign-客户端)
- [使用示例](#使用示例)
- [高级用法](#高级用法)
- [配置说明](#配置说明)

## 依赖配置

### 在 pom.xml 中添加依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

## 启用 Feign

在启动类上添加 `@EnableFeignClients` 注解：

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.qoobot.openidaas.common.feign")
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

## Feign 客户端

所有 Feign 客户端接口位于 `com.qoobot.openidaas.common.feign` 包下：

| 客户端 | 服务 | 用途 |
|--------|------|------|
| `UserClient` | user-service | 用户管理 |
| `OrganizationClient` | organization-service | 组织架构管理 |
| `RoleClient` | role-service | 角色权限管理 |
| `ApplicationClient` | application-service | 应用管理 |
| `AuditClient` | audit-service | 审计日志 |

## 使用示例

### 基础用法

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl {

    private final UserClient userClient;

    /**
     * 在用户服务中调用组织服务获取部门信息
     */
    public UserVO getUserWithDepartment(Long userId) {
        // 1. 获取用户信息
        UserVO user = userClient.getUserById(userId).getData();

        // 2. 通过组织服务获取部门信息
        if (user.getDeptId() != null) {
            DepartmentVO dept = organizationClient.getDepartmentById(user.getDeptId()).getData();
            user.setDeptName(dept.getDeptName());
        }

        return user;
    }
}
```

### 使用 FeignHelper（推荐）

`FeignHelper` 提供了更安全的调用方式，自动处理异常和响应：

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl {

    private final OrganizationClient organizationClient;

    public UserVO getUserWithDepartment(Long userId) {
        // 使用 FeignHelper 安全调用
        DepartmentVO dept = FeignHelper.call(
            () -> organizationClient.getDepartmentById(deptId)
        );

        user.setDeptName(dept.getDeptName());
        return user;
    }

    /**
     * 带重试的调用
     */
    public DepartmentVO getDepartmentWithRetry(Long deptId) {
        return FeignHelper.callWithRetry(
            () -> organizationClient.getDepartmentById(deptId),
            3 // 重试 3 次
        );
    }

    /**
     * 异步调用
     */
    public CompletableFuture<UserVO> getUserAsync(Long userId) {
        return FeignHelper.callAsync(
            () -> userClient.getUserById(userId)
        );
    }
}
```

### 批量操作示例

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl {

    private final RoleClient roleClient;
    private final AuditClient auditClient;

    /**
     * 为用户分配角色并记录审计日志
     */
    @Transactional
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        // 1. 分配角色
        for (Long roleId : roleIds) {
            FeignHelper.call(() -> roleClient.assignRoleToUser(userId, roleId, null, null));
        }

        // 2. 批量记录审计日志
        List<AuditLogCreateDTO> auditLogs = roleIds.stream()
            .map(roleId -> AuditLogCreateDTO.builder()
                .userId(userId)
                .operationType(OperationTypeEnum.ASSIGN)
                .module("role")
                .targetId(roleId.toString())
                .description("分配角色 " + roleId)
                .build())
            .collect(Collectors.toList());

        FeignHelper.call(() -> auditClient.createAuditLogBatch(auditLogs));
    }
}
```

### 服务间传递用户上下文

Feign 配置会自动传递认证头和链路追踪 ID：

```java
// 请求中的认证信息会自动传递给下游服务
@GetMapping("/users/{id}/roles")
public ResultVO<List<RoleVO>> getUserRoles(@PathVariable Long id) {
    // 调用角色服务时，会自动携带当前请求的 Authorization 头
    List<RoleVO> roles = FeignHelper.call(
        () -> roleClient.getUserRoles(id)
    );
    return ResultVO.success(roles);
}
```

## 高级用法

### 自定义超时配置

在 application.yml 中配置：

```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 5000
      # 针对特定服务配置
      user-service:
        connectTimeout: 3000
        readTimeout: 3000
```

### 降级处理

使用 Hystrix 或 Resilience4j 实现熔断降级：

```java
@FeignClient(
    name = "user-service",
    fallbackFactory = UserClientFallbackFactory.class
)
public interface UserClient {
    // ...
}

@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {
    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {
            @Override
            public ResultVO<Object> getUserById(Long id) {
                log.error("用户服务降级", cause);
                return ResultVO.fail("服务暂时不可用");
            }
            // 实现其他方法
        };
    }
}
```

### 压缩配置

```yaml
feign:
  compression:
    request:
      enabled: true
      mime-types: text/xml,application/xml,application/json
      min-request-size: 2048
    response:
      enabled: true
```

## 配置说明

### FeignConfig 配置

`FeignConfig` 提供了以下配置：

1. **日志级别**: FULL（记录请求和响应的完整信息）
2. **重试策略**: 默认不重试（避免雪崩效应）
3. **请求拦截器**: 自动传递认证头和链路追踪 ID
4. **错误解码器**: 统一异常处理

### 自定义配置

如需自定义配置，可以在服务中覆盖：

```java
@Configuration
public class CustomFeignConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC; // 生产环境使用 BASIC
    }

    @Bean
    RequestInterceptor customInterceptor() {
        return template -> {
            // 自定义请求头
            template.header("X-Custom-Header", "value");
        };
    }
}
```

## 最佳实践

1. **使用 FeignHelper**: 简化调用和异常处理
2. **避免循环调用**: 注意服务间调用链，避免死循环
3. **设置合理超时**: 根据业务场景配置超时时间
4. **异步调用**: 对于非关键路径，使用异步调用提高性能
5. **监控调用**: 监控 Feign 调用的成功率和耗时
6. **降级处理**: 关键服务需要配置降级策略
7. **批量操作**: 尽量使用批量接口减少网络调用

## 故障排查

### 调用超时

```yaml
# 增加超时时间
feign:
  client:
    config:
      default:
        readTimeout: 10000
```

### 服务不可用

检查 Nacos 服务注册：

```bash
# 查看 Nacos 服务列表
curl http://nacos:8848/nacos/v1/ns/instance/list?serviceName=user-service
```

### 查看调用日志

配置日志级别：

```yaml
logging:
  level:
    com.qoobot.openidaas.common.feign: DEBUG
    feign: DEBUG
```

## 相关文档

- [Spring Cloud OpenFeign 文档](https://spring.io/projects/spring-cloud-openfeign)
- [OpenFeign 官方文档](https://github.com/OpenFeign/feign)
