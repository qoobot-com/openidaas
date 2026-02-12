# Sentinel 配置说明

## 访问地址
- 控制台: http://localhost:8858
- 默认用户名/密码: sentinel/sentinel

## 主要功能
- 流量控制
- 熔断降级  
- 系统负载保护
- 实时监控

## 集成方式
在微服务应用中添加以下依赖：

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

并在 application.yml 中配置：

```yaml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: localhost:8858
        port: 8719
```