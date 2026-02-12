# Sentinel 流量控制与降级指南

## 概述

OpenIDaaS 使用 Alibaba Sentinel 实现流量控制、熔断降级和系统负载保护。

## Sentinel 架构

```
┌─────────────────────────────────────────────────────────────┐
│                     OpenIDaaS System                       │
├─────────────────────────────────────────────────────────────┤
│  Gateway                                                  │
│  ├─ Sentinel Gateway Adapter (网关限流)                   │
│  └─ 流控规则: 200 QPS                                     │
├─────────────────────────────────────────────────────────────┤
│  Microservices                                             │
│  ├─ auth-service    (100 QPS)                              │
│  ├─ user-service    (150 QPS)                              │
│  ├─ role-service    (100 QPS)                              │
│  ├─ org-service     (100 QPS)                              │
│  ├─ app-service     (80 QPS)                               │
│  └─ audit-service   (200 QPS)                              │
├─────────────────────────────────────────────────────────────┤
│  Sentinel Dashboard                                        │
│  ├─ 监控面板 (http://localhost:8858)                      │
│  ├─ 规则管理                                               │
│  └─ 实时监控                                               │
└─────────────────────────────────────────────────────────────┘
```

## 1. 部署 Sentinel Dashboard

### Docker Compose 部署

```bash
# 使用 docker-compose 启动
docker-compose up -d sentinel

# 或单独启动
docker run -d \
  --name openidaas-sentinel \
  -p 8858:8858 \
  bladex/sentinel-dashboard:1.8.6
```

### 访问 Dashboard

- URL: http://localhost:8858
- 默认用户名: sentinel
- 默认密码: sentinel

## 2. 服务配置

### 2.1 Gateway 配置

```yaml
spring:
  cloud:
    sentinel:
      # 启用 Sentinel
      enabled: true
      # Dashboard 地址
      transport:
        dashboard: localhost:8858
        port: 8719
      # Nacos 数据源
      datasource:
        nacos:
          enabled: true
          server-addr: localhost:8848
          group-id: SENTINEL_GROUP
          rule-type: flow
          data-id: gateway-flow-rules
      # 网关配置
      scg:
        enabled: true
        order: 100

# 网关降级响应
spring.cloud.sentinel.scg.fallback:
  response-mode: response
  response-status: 429
  response-body: '{"code": 429, "message": "系统繁忙，请稍后重试"}'
```

### 2.2 微服务配置

```yaml
spring:
  cloud:
    sentinel:
      enabled: true
      transport:
        dashboard: ${SENTINEL_DASHBOARD:localhost:8858}
        port: 8719
      datasource:
        file:
          enabled: true
          file: classpath:sentinel/rules.json
          rule-type: flow

# 自定义配置
sentinel:
  enabled: true
  flow:
    qps: 100
    control-behavior: 0
    warm-up-period: 10
  degrade:
    ratio: 0.5
    time-window: 10
```

## 3. 流控规则

### 3.1 规则类型

| 类型 | 说明 | 适用场景 |
|-----|------|---------|
| **QPS 限流** | 按请求数限流 | API 接口 |
| **并发线程数限流** | 按线程数限流 | 耗时操作 |
| **Warm Up** | 预热限流 | 突发流量 |
| **匀速排队** | 令牌桶算法 | 平滑流量 |
| **冷启动** | 逐渐增加 QPS | 防止雪崩 |

### 3.2 规则配置示例

#### QPS 限流

```json
{
  "resource": "/api/users/**",
  "limitApp": "default",
  "grade": 1,
  "count": 100,
  "strategy": 0,
  "controlBehavior": 0,
  "clusterMode": false
}
```

参数说明:
- `grade`: 限流阈值类型，1=QPS, 0=并发线程数
- `count`: 限流阈值
- `strategy`: 0=直接, 1=关联, 2=链路
- `controlBehavior`: 0=直接拒绝, 1=Warm Up, 2=匀速排队

#### Warm Up 配置

```json
{
  "resource": "/api/users/list",
  "grade": 1,
  "count": 200,
  "controlBehavior": 1,
  "warmUpPeriodSec": 10
}
```

#### 匀速排队配置

```json
{
  "resource": "/api/users/create",
  "grade": 1,
  "count": 50,
  "controlBehavior": 2,
  "maxQueueingTimeMs": 500
}
```

### 3.3 预定义流控规则

| 资源 | QPS 限制 | 行为 |
|-----|---------|------|
| `/api/auth/**` | 100 | 直接拒绝 |
| `/api/users/**` | 150 | 直接拒绝 |
| `/api/organizations/**` | 100 | 直接拒绝 |
| `/api/roles/**` | 100 | 直接拒绝 |
| `/api/applications/**` | 80 | 直接拒绝 |
| `/api/audit/**` | 200 | 直接拒绝 |

## 4. 降级规则

### 4.1 降级策略

| 策略 | 说明 | 配置参数 |
|-----|------|---------|
| **慢调用比例** | 响应时间超过阈值且比例达到阈值 | slowRatioThreshold, minRequestAmount |
| **异常比例** | 异常比例达到阈值 | ratio, minRequestAmount |
| **异常数** | 异常数达到阈值 | count, timeWindow |

### 4.2 降级配置示例

#### 慢调用比例降级

```json
{
  "resource": "openidaas-user-service",
  "grade": 0,
  "count": 500,
  "timeWindow": 10,
  "minRequestAmount": 5,
  "statIntervalMs": 1000
}
```

参数说明:
- `grade`: 0=慢调用比例, 1=异常比例, 2=异常数
- `count`: 慢调用 RT 阈值 (ms) 或异常比例
- `timeWindow`: 降级时间窗口 (s)
- `minRequestAmount`: 最小请求数

#### 异常比例降级

```json
{
  "resource": "openidaas-auth-service",
  "grade": 1,
  "count": 0.5,
  "timeWindow": 10,
  "minRequestAmount": 5,
  "statIntervalMs": 1000
}
```

### 4.3 预定义降级规则

| 资源 | 降级策略 | 阈值 | 时间窗口 |
|-----|---------|------|---------|
| user-service | 慢调用 | 500ms | 10s |
| auth-service | 异常比例 | 50% | 10s |
| organization-service | 慢调用 | 1000ms | 10s |

## 5. 使用注解

### 5.1 @SentinelResource 注解

```java
import com.qoobot.openidaas.common.sentinel.SentinelResource;

@Service
public class UserService {

    @SentinelResource(
        value = "user-service:getUser",
        blockHandler = "handleGetUserBlock",
        fallback = "handleGetUserFallback"
    )
    public User getUser(Long id) {
        // 业务逻辑
    }

    // 流控处理
    public User handleGetUserBlock(Long id, BlockException ex) {
        return User.guest();
    }

    // 降级处理
    public User handleGetUserFallback(Long id, Throwable ex) {
        return User.guest();
    }
}
```

### 5.2 注解参数

| 参数 | 说明 | 默认值 |
|-----|------|--------|
| `value` | 资源名称 | 方法名 |
| `resourceType` | 资源类型 | COMMON |
| `blockHandler` | 流控处理方法 | - |
| `fallback` | 降级处理方法 | - |

## 6. 测试验证

### 6.1 启动服务

```bash
# 启动 Sentinel Dashboard
docker-compose up -d sentinel

# 启动所有服务
docker-compose up -d

# 或本地启动
mvn spring-boot:run
```

### 6.2 测试流控

```bash
# 测试高 QPS 限流
for i in {1..200}; do
  curl http://localhost:8080/api/users/1 &
done
```

预期结果:
- 前 100 个请求成功
- 后 100 个请求返回 429

### 6.3 测试降级

```bash
# 访问降级测试接口
curl http://localhost:8080/sentinel/test-degrade
```

预期结果:
- 前几次请求成功
- 连续慢调用后触发降级

### 6.4 查看 Dashboard

1. 访问 http://localhost:8858
2. 登录 (sentinel/sentinel)
3. 选择服务
4. 查看实时监控
5. 查看规则列表
6. 查看簇点链路

## 7. Nacos 规则持久化

### 7.1 上传规则到 Nacos

```bash
# 登录 Nacos
http://localhost:8848/nacos

# 创建配置
Data ID: gateway-flow-rules
Group: SENTINEL_GROUP
配置格式: JSON
配置内容: (见 sentinel-gateway-flow-rules.json)
```

### 7.2 自动同步规则

服务启动时会自动从 Nacos 拉取规则：
```yaml
spring.cloud.sentinel.datasource.nacos:
  enabled: true
  server-addr: localhost:8848
  namespace: public
  group-id: SENTINEL_GROUP
  rule-type: flow
  data-id: gateway-flow-rules
```

## 8. 监控与告警

### 8.1 查看监控指标

```bash
# 通过 Actuator 查看
curl http://localhost:8080/actuator/sentinel
```

### 8.2 自定义指标收集

```java
@Component
public class SentinelMetricsExporter {

    @Scheduled(fixedRate = 5000)
    public void exportMetrics() {
        List<ResourceMetric> metrics = ResourceMetric.getMetrics();
        // 上传到 Prometheus
    }
}
```

### 8.3 告警配置

在 Dashboard 中配置告警规则：
- 限流触发次数 > 100
- 降级触发次数 > 50
- 异常比例 > 10%

## 9. 最佳实践

### 9.1 流控策略

1. **网关层**: 限流保护整体系统
2. **服务层**: 限流保护单个服务
3. **方法层**: 限流保护核心接口

### 9.2 降级策略

1. **核心服务**: 使用异常比例降级
2. **非核心服务**: 使用慢调用比例降级
3. **降级时间**: 10-30 秒

### 9.3 规则配置

1. **QPS 阈值**: 根据实际负载测试确定
2. **预热时间**: 10-30 秒
3. **时间窗口**: 10-30 秒

### 9.4 监控告警

1. **实时监控**: Dashboard
2. **趋势分析**: Prometheus + Grafana
3. **告警通知**: 邮件、短信、钉钉

## 10. 故障排查

### 10.1 规则未生效

```bash
# 检查 Sentinel 是否启用
curl http://localhost:8080/actuator/sentinel

# 检查 Dashboard 连接
# 查看日志: Sentinel transport connecting to localhost:8858
```

### 10.2 降级未触发

```bash
# 检查规则配置
# 查看最小请求数是否满足

# 检查降级条件
# 慢调用: RT > threshold
# 异常比例: 异常数/总数 > ratio
```

### 10.3 性能问题

```bash
# 检查统计时间窗口
# statIntervalMs 不宜过小，建议 1000ms

# 检查集群模式
# clusterMode: false (本地限流性能更好)
```

## 相关文档

- [Sentinel 官方文档](https://sentinelguard.io/zh-cn/)
- [Spring Cloud Alibaba Sentinel](https://github.com/alibaba/spring-cloud-alibaba/wiki/Sentinel)
- [Nacos 配置中心](https://nacos.io/zh-cn/docs/quick-start.html)
