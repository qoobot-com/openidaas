# OpenIDaaS Security Module

## 概述

OpenIDaaS 安全模块，提供安全相关的工具和功能。

## 功能特性

- 密码加密
- JWT 令牌处理
- 安全工具类
- 加密解密工具

## 使用示例

### 密码加密

```java
String encodedPassword = PasswordUtils.encode("rawPassword");
boolean matches = PasswordUtils.matches("rawPassword", encodedPassword);
```

### 获取当前用户

```java
Optional<String> username = SecurityUtils.getCurrentUsername();
boolean authenticated = SecurityUtils.isAuthenticated();
```

## 配置

```yaml
openidaas:
  security:
    password:
      min-length: 8
      max-length: 128
```

## 许可证

Apache License 2.0
