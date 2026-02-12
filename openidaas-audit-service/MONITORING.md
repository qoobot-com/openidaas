# 审计日志服务监控文档

## 概述

审计日志服务集成了完整的监控和日志分析功能，支持实时监控、指标收集、健康检查和告警通知。

## 监控功能

### 1. Actuator端点

服务暴露了以下监控端点：

| 端点 | 路径 | 描述 |
|------|------|------|
| Health | `/actuator/health` | 健康检查 |
| Info | `/actuator/info` | 应用信息 |
| Metrics | `/actuator/metrics` | 指标数据 |
| Prometheus | `/actuator/prometheus` | Prometheus格式指标 |
| Loggers | `/actuator/loggers` | 日志级别管理 |
| HttpTrace | `/actuator/httptrace` | HTTP跟踪 |

### 2. 自定义健康检查

审计服务提供了自定义的健康检查指示器：

```bash
curl http://localhost:8085/actuator/health
```

响应示例：
```json
{
  "status": "UP",
  "components": {
    "audit": {
      "status": "UP",
      "details": {
        "lastProcessedTime": 1707648000000,
        "successCount": 1234,
        "failureCount": 2,
        "failureRate": 0.0016,
        "timeSinceLastProcessMs": 5000
      }
    },
    "db": {
      "status": "UP"
    },
    "redis": {
      "status": "UP"
    }
  }
}
```

### 3. 关键指标

#### 业务指标

| 指标名称 | 类型 | 描述 |
|---------|------|------|
| `audit.logs.total` | Counter | 审计日志总数 |
| `audit.logs.success` | Counter | 成功日志数 |
| `audit.logs.failed` | Counter | 失败日志数 |
| `audit.logs.async` | Counter | 异步日志数 |
| `audit.logs.by.operation.type` | Counter | 按操作类型统计 |
| `audit.logs.by.module` | Counter | 按模块统计 |
| `audit.logs.by.tenant` | Counter | 按租户统计 |

#### 性能指标

| 指标名称 | 类型 | 描述 |
|---------|------|------|
| `audit.log.record.duration` | Timer | 日志记录耗时 |
| `audit.log.query.duration` | Timer | 日志查询耗时 |
| `audit.log.export.duration` | Timer | 日志导出耗时 |

#### JVM指标

| 指标名称 | 类型 | 描述 |
|---------|------|------|
| `jvm.memory.used` | Gauge | JVM内存使用量 |
| `jvm.gc.pause` | Timer | GC停顿时间 |
| `jvm.threads.*` | Gauge | 线程池状态 |

### 4. 日志配置

#### 日志级别

- **开发环境**: DEBUG
- **生产环境**: INFO/WARN

#### 日志文件

| 文件 | 描述 | 保留策略 |
|------|------|---------|
| `openidaas-audit-service.log` | 主日志文件 | 30天，100MB/文件 |
| `openidaas-audit-service-error.log` | 错误日志 | 60天，50MB/文件 |
| `openidaas-audit-service-audit.log` | 审计日志(JSON格式) | 90天，200MB/文件 |

#### 结构化日志

审计日志使用JSON格式输出，便于日志分析系统解析：

```json
{
  "timestamp": "2024-02-12 10:30:00.123",
  "level": "INFO",
  "pid": "12345",
  "thread": "http-nio-8085-exec-1",
  "logger": "com.qoobot.openidaas.audit.service.impl.AuditServiceImpl",
  "message": "AUDIT_OPERATION | operation=CREATE, module=USER, operator=admin, target=user_123, success=true, duration=150ms",
  "traceId": "abc123",
  "spanId": "def456"
}
```

### 5. 告警配置

#### 告警规则

参见 `alert_rules.yml` 文件，包含以下告警：

1. **服务可用性告警**
   - 服务宕机
   - 健康检查失败

2. **性能告警**
   - 高错误率（>10%）
   - 慢查询（>2秒）
   - 高响应时间（>1秒）

3. **资源告警**
   - JVM内存使用率过高（>80%）
   - 磁盘空间不足（<10%）

4. **业务告警**
   - 审计日志量突增
   - 未处理安全事件
   - 特定操作失败率过高

#### 告警通知

告警支持多种通知渠道：
- 邮件
- 企业微信/钉钉
- Slack
- PagerDuty
- SMS

### 6. 集成监控工具

#### Prometheus

配置文件：`PrometheusConfig.yaml`

```yaml
scrape_configs:
  - job_name: 'audit-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['audit-service:8085']
```

#### Grafana仪表板

推荐的Grafana面板：

1. **服务概览面板**
   - QPS
   - 错误率
   - 响应时间
   - 日志量趋势

2. **数据库面板**
   - 慢查询统计
   - 连接池状态
   - 查询延迟分布

3. **JVM面板**
   - 堆内存使用
   - GC频率
   - 线程状态

4. **业务面板**
   - 操作类型分布
   - 模块活跃度
   - 租户活动统计

### 7. 日志分析

#### ELK Stack集成

```yaml
# filebeat配置
filebeat.inputs:
  - type: log
    enabled: true
    paths:
      - /logs/openidaas-audit-service-audit.log
    json.keys_under_root: true
    json.add_error_key: true

output.elasticsearch:
  hosts: ["elasticsearch:9200"]
  index: "audit-logs-%{+yyyy.MM.dd}"
```

#### 查询示例

```bash
# 查询失败的审计日志
GET /audit-logs-*/_search
{
  "query": {
    "bool": {
      "must": [
        { "match": { "message": "AUDIT_OPERATION" } },
        { "match": { "level": "INFO" } }
      ],
      "must_not": [
        { "match": { "success": "true" } }
      ]
    }
  }
}

# 统计操作类型分布
GET /audit-logs-*/_search
{
  "size": 0,
  "aggs": {
    "by_operation_type": {
      "terms": {
        "field": "operation.keyword",
        "size": 20
      }
    }
  }
}
```

### 8. 监控最佳实践

1. **设置合理的告警阈值**
   - 避免告警风暴
   - 设置告警冷却时间
   - 区分告警严重级别

2. **定期审查告警规则**
   - 根据业务调整阈值
   - 优化误报率
   - 添加新的监控指标

3. **使用仪表板**
   - 创建Grafana仪表板
   - 设置关键指标阈值
   - 配置自动刷新

4. **日志归档**
   - 定期归档历史日志
   - 设置日志保留策略
   - 使用冷存储降低成本

5. **监控数据保留**
   - 热数据：7-30天
   - 温数据：30-90天
   - 冷数据：90天以上

### 9. 故障排查

#### 常见问题

**问题1: 服务响应缓慢**
```bash
# 检查慢查询
curl http://localhost:8085/actuator/metrics/audit.log.query.duration

# 检查JVM状态
curl http://localhost:8085/actuator/metrics/jvm.memory.used
```

**问题2: 日志记录失败**
```bash
# 检查数据库连接
curl http://localhost:8085/actuator/health/db

# 查看错误日志
tail -f logs/openidaas-audit-service-error.log
```

**问题3: 内存不足**
```bash
# 检查堆内存使用
curl http://localhost:8085/actuator/metrics/jvm.memory.used?tag=area:heap

# 检查GC统计
curl http://localhost:8085/actuator/metrics/jvm.gc.pause
```

## 相关资源

- [Spring Boot Actuator文档](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer文档](https://micrometer.io/docs)
- [Prometheus文档](https://prometheus.io/docs/)
- [Grafana文档](https://grafana.com/docs/)
