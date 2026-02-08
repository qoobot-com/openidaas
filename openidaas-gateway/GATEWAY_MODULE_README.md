# OpenIDaaS API网关模块文档

## 概述

openidaas-gateway 模块提供企业级 API 网关功能，统一管理所有API请求，提供路由管理、认证集成、流量控制、监控统计等核心能力。

## 核心能力

### 1. 路由管理

#### 动态路由配置
- 基于YAML配置路由规则
- 支持Path、Method、Header等谓词
- 动态路由热更新

#### 路由负载均衡
- 集成Spring Cloud LoadBalancer
- 支持轮询、随机等负载均衡策略
- 服务健康检查

#### 路由权限控制
- JWT Token验证
- OAuth2.1 Token验证
- API Key验证
- 基于角色的路由访问控制

#### 路由限流熔断
- 基于令牌桶算法的限流
- Resilience4j熔断机制
- 自动故障转移
- 自定义降级策略

### 2. 认证集成

#### JWT Token验证
- 无状态Token验证
- Token过期检查
- Token黑名单检查
- 用户信息提取

#### OAuth2.1 Token验证
- OAuth2资源服务器
- Token introspection
- Scope验证

#### API Key验证
- API Key管理
- Key有效性检查
- 权限映射

#### 客户端认证
- TLS客户端认证
- 证书验证

### 3. 流量控制

#### 限流策略
- QPS限流
- TPS限流
- 并发限流
- IP级别限流

#### 熔断机制
- 失败率熔断
- 慢调用熔断
- 半开状态探测
- 自动恢复

#### 故障转移
- 重试机制
- 降级服务
- 故障隔离

#### 流量染色
- 请求标记
- 灰度发布
- A/B测试

### 4. 监控统计

#### API调用统计
- 请求数统计
- 响应时间统计
- 错误率统计
- QPS统计

#### 响应时间监控
- P50/P95/P99延迟
- 平均响应时间
- 慢请求追踪

#### 错误率监控
- 4xx错误统计
- 5xx错误统计
- 错误分布
- 错误趋势

#### 实时告警
- QPS异常告警
- 错误率告警
- 延迟告警
- 熔断告警

## 技术实现

### 网关框架
- **Spring Cloud Gateway 4.1.0**: 响应式网关
- **WebFlux**: 异步非阻塞

### 服务发现
- **Nacos集成**: 服务注册与发现
- **Spring Cloud LoadBalancer**: 负载均衡

### 限流熔断
- **Resilience4j**: 熔断器
- **Bucket4j**: 令牌桶算法

### 认证验证
- **JWT**: Token验证
- **Spring Security Reactive**: 安全框架

### 监控统计
- **Micrometer**: 指标收集
- **Prometheus**: 指标存储
- **Grafana**: 指标可视化

## 配置说明

### 路由配置

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://openidaas-user
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=1
            - AuthenticationGatewayFilter
            - RateLimitGatewayFilter
```

### 限流配置

```yaml
openidaas:
  gateway:
    rate-limit:
      enabled: true
      default-capacity: 100
      default-refill-rate: 50
```

### 熔断配置

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        sliding-window-size: 60
```

## 网关管理API

### 获取网关指标

```bash
GET /api/gateway/metrics
```

响应示例：
```json
{
  "totalRequests": 1000000,
  "successRequests": 995000,
  "failedRequests": 5000,
  "errorRate": 0.5,
  "avgResponseTime": 45.2,
  "currentQps": 1500
}
```

### 获取API统计

```bash
GET /api/gateway/stats?uri=/api/users&minutes=60
```

### 获取QPS统计

```bash
GET /api/gateway/qps?uri=/api/users
```

### 获取错误率

```bash
GET /api/gateway/error-rate?uri=/api/users
```

### 重置限流

```bash
POST /api/gateway/rate-limit/reset?key=192.168.1.1&scope=api
```

### 健康检查

```bash
GET /api/gateway/health
```

## 性能优化

### 路由优化
- 使用高效的谓词
- 减少过滤器数量
- 合理设置超时

### 缓存优化
- JWT验证结果缓存
- 路由信息缓存
- 限流令牌桶缓存

### 连接池优化
- 合理设置连接池大小
- 启用连接复用
- 设置合理的超时

## 性能测试

### 网关转发性能

目标：10万 QPS

```bash
# 使用JMeter或wrk进行压力测试
wrk -t4 -c1000 -d30s --latency http://localhost:8080/api/users
```

预期结果：
- QPS > 100,000
- P99延迟 < 50ms
- 错误率 < 0.1%

### 路由配置热更新

```bash
# 修改配置后触发刷新
curl -X POST http://localhost:8080/actuator/refresh
```

### 限流熔断机制验证

```bash
# 触发限流
for i in {1..200}; do
  curl http://localhost:8080/api/users
done

# 预期：前100次成功，后100次返回429
```

### 认证验证性能

```bash
# 使用有效Token请求1000次
for i in {1..1000}; do
  curl -H "Authorization: Bearer TOKEN" http://localhost:8080/api/users
done

# 预期：平均响应时间 < 10ms
```

## 监控告警

### Prometheus指标

```yaml
# 网关QPS
gateway_requests_total

# 网关响应时间
gateway_response_time_seconds

# 网关错误数
gateway_errors_total

# 熔断器状态
circuitbreaker_state{service="user-service"}
```

### Grafana面板

推荐监控指标：
- QPS趋势
- 响应时间分布（P50/P95/P99）
- 错误率趋势
- 熔断器状态
- 活跃连接数

### 告警规则

```yaml
# QPS异常告警
- alert: GatewayHighErrorRate
  expr: gateway_error_rate > 5
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "Gateway error rate is high"

# 熔断器告警
- alert: GatewayCircuitBreakerOpen
  expr: circuitbreaker_state == "OPEN"
  for: 1m
  labels:
    severity: critical
  annotations:
    summary: "Circuit breaker is open"
```

## 故障排查

### 常见问题

#### 路由不生效
- 检查路由配置
- 确认服务注册
- 查看网关日志

#### 限流不生效
- 检查Redis连接
- 确认限流配置
- 查看限流统计

#### 熔断器频繁触发
- 调整失败率阈值
- 增加窗口大小
- 检查后端服务健康

### 日志查看

```bash
# 查看网关日志
tail -f logs/openidaas-gateway.log

# 查看特定路由日志
grep "user-service" logs/openidaas-gateway.log
```

## 最佳实践

### 路由设计
- 按服务划分路由
- 使用统一前缀
- 合理设置超时

### 安全配置
- 启用HTTPS
- 配置CORS白名单
- 实施限流保护

### 监控告警
- 设置合理阈值
- 配置多级告警
- 定期检查告警

## 常见问题

### Q: 如何动态更新路由配置？

A: 使用Spring Cloud Config或Nacos配置中心，支持热更新。

### Q: 如何禁用某个路由？

A: 在配置中删除该路由，或设置enabled: false。

### Q: 如何自定义熔断降级策略？

A: 实现自定义FallbackProvider接口。

## 许可证

Apache License 2.0
