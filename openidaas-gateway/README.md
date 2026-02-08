# OpenIDaaS Gateway Module

## 概述

OpenIDaaS API 网关模块，基于 Spring Cloud Gateway 实现。

## 功能特性

- 路由转发
- 负载均衡
- 统一认证
- 限流控制
- 熔断降级

## 路由配置

- `/api/auth/**` -> openidaas-auth
- `/api/users/**` -> openidaas-user
- `/api/tenants/**` -> openidaas-tenant

## 配置

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: service-name
          uri: lb://service-name
          predicates:
            - Path=/api/service/**
          filters:
            - StripPrefix=1
```

## 许可证

Apache License 2.0
