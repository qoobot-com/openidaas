# OpenIDaaS 监控与告警系统

## 概述

OpenIDaaS 监控系统基于 Prometheus + Grafana + Alertmanager 构建，提供企业级的监控和告警能力。

## 架构

```
┌─────────────┐
│   应用服务   │
└──────┬──────┘
       │ Micrometer 指标
       │ /actuator/prometheus
       ▼
┌─────────────────┐
│   Prometheus    │  ← 指标收集和存储
└──────┬──────────┘
       │ 告警规则
       ▼
┌─────────────────┐
│  Alertmanager   │  ← 告警路由和通知
└──────┬──────────┘
       │ 查询
       ▼
┌─────────────────┐
│    Grafana      │  ← 可视化面板
└─────────────────┘
```

## 功能特性

### 1. 指标收集

#### 应用性能指标
- JVM 内存使用率
- CPU 使用率
- 响应时间 P95/P99
- GC 时间统计

#### 业务指标
- 认证成功率
- Token 刷新频率
- 用户注册/登录统计
- 租户活跃度统计

#### 安全指标
- 登录失败率
- 异常访问统计
- Token 泄露检测
- 权限违规统计

### 2. 告警规则

#### 系统告警
- CPU 使用率 > 80%（警告）/ > 95%（严重）
- 内存使用率 > 80%（警告）/ > 95%（严重）
- 响应时间 P95 > 2s
- 响应时间 P99 > 3s
- GC 频率 > 1 次/秒
- 服务宕机

#### 业务告警
- 认证失败率 > 5%（警告）/ > 10%（严重）
- Token 刷新失败 > 10 次/分钟
- 认证 QPS 异常偏低/偏高
- 数据库连接池使用率 > 80%

#### 安全告警
- 登录失败 > 10 次/分钟（警告）/ > 50 次/分钟（严重）
- 异常访问检测
- 权限违规 > 10 次/分钟
- Token 泄露检测
- MFA 验证失败 > 5 次/分钟

#### 租户告警
- 租户活跃度 < 10
- 租户配额使用 > 90%（警告）/ > 98%（严重）
- 租户请求量 > 1000/秒
- 租户错误率 > 5%

#### 数据库告警
- 数据库连接池耗尽
- 查询响应时间 > 1 秒
- 数据库死锁检测
- 连接超时频繁

### 3. 可视化面板

#### 认证统计面板
- 认证成功率仪表盘
- 认证 QPS 统计
- 认证失败率仪表盘
- 认证请求趋势图
- Token 刷新统计

#### 系统性能监控面板
- JVM 堆内存使用率
- CPU 使用率
- 响应时间 P95/P99
- JVM 堆内存趋势
- GC 时间统计

#### 安全事件监控面板
- 登录失败统计（按原因）
- 异常访问统计
- 权限违规统计
- Token 泄露检测

#### 租户使用情况面板
- 活跃租户数
- 租户活跃用户数 Top 10

## 快速开始

### 1. 启动监控系统

```bash
cd deployment/monitoring
docker-compose up -d
```

### 2. 访问服务

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin123)
- **Alertmanager**: http://localhost:9093
- **Node Exporter**: http://localhost:9100/metrics
- **cAdvisor**: http://localhost:8081

### 3. 导入 Grafana Dashboard

Dashboard 已通过自动配置导入，访问 http://localhost:3000 查看。

### 4. 配置告警通知

编辑 `alertmanager/alertmanager.yml`，配置邮件、Slack、Webhook 等通知方式：

```yaml
receivers:
  - name: 'critical-alerts'
    email_configs:
      - to: 'oncall@openidaas.com'
    slack_configs:
      - api_url: 'YOUR_SLACK_WEBHOOK'
```

重启 Alertmanager 使配置生效：

```bash
docker-compose restart alertmanager
```

## 自定义指标

### 1. 使用 Micrometer

```java
@Autowired
private AuthMetrics authMetrics;

public void authenticate(String username, String password) {
    long startTime = System.nanoTime();
    authMetrics.recordAuthRequest();

    try {
        // 认证逻辑
        if (authenticateUser(username, password)) {
            authMetrics.recordAuthSuccess();
        } else {
            authMetrics.recordAuthFailure("invalid_credentials");
        }
    } finally {
        authMetrics.recordAuthResponseTime(startTime);
    }
}
```

### 2. 添加自定义指标

```java
@Component
public class CustomMetrics {

    private final Counter customCounter;
    private final Gauge customGauge;

    public CustomMetrics(MeterRegistry registry) {
        this.customCounter = Counter.builder("custom_metric_total")
                .description("Custom counter")
                .register(registry);

        this.customGauge = Gauge.builder("custom_gauge", () -> getValue())
                .description("Custom gauge")
                .register(registry);
    }
}
```

## 配置说明

### Prometheus 配置

配置文件：`prometheus/prometheus.yml`

关键配置：
- `scrape_interval`: 指标抓取间隔（默认 15s）
- `evaluation_interval`: 规则评估间隔（默认 15s）
- `rule_files`: 告警规则文件
- `scrape_configs`: 抓取目标配置

### Alertmanager 配置

配置文件：`alertmanager/alertmanager.yml`

关键配置：
- `global`: 全局配置（SMTP、超时等）
- `route`: 告警路由规则
- `receivers`: 接收器配置
- `inhibit_rules`: 抑制规则

### Grafana 配置

配置目录：`grafana/provisioning/`

- `datasources/`: 数据源配置
- `dashboards/`: Dashboard 自动加载配置

## 监控指标参考

### Micrometer 指标

#### HTTP 请求指标
- `http_server_requests_seconds_count`: HTTP 请求总数
- `http_server_requests_seconds_sum`: HTTP 请求总时间
- `http_server_requests_seconds{quantile="0.95"}`: P95 响应时间

#### JVM 指标
- `jvm_memory_used_bytes`: JVM 内存使用量
- `jvm_gc_pause_seconds_count`: GC 暂停次数
- `jvm_gc_pause_seconds_sum`: GC 暂停总时间

#### 数据库指标
- `hikaricp_connections_active`: 活跃连接数
- `hikaricp_connections_max`: 最大连接数
- `hikaricp_connections_idle`: 空闲连接数

#### 自定义指标
- `auth_total`: 认证请求总数
- `auth_success_total`: 认证成功总数
- `auth_failure_total`: 认证失败总数
- `token_refresh_total`: Token 刷新总数
- `security_event_total`: 安全事件总数

## 告警管理

### 查看告警

1. Prometheus UI: http://localhost:9090/alerts
2. Alertmanager UI: http://localhost:9093

### 静默告警

```bash
# 创建静默规则
curl -X POST http://localhost:9093/api/v2/silences \
  -H 'Content-Type: application/json' \
  -d '{
    "matchers": [{"name": "alertname", "value": "HighCPUUsage"}],
    "startsAt": "2024-01-01T00:00:00Z",
    "endsAt": "2024-01-02T00:00:00Z",
    "comment": "Maintenance"
  }'
```

### 验证告警规则

```bash
# 检查规则语法
promtool check rules prometheus/alerts.yml

# 查看规则执行情况
curl http://localhost:9090/api/v1/rules
```

## 性能优化

### 1. Prometheus 存储优化

```yaml
# prometheus/prometheus.yml
storage:
  tsdb:
    path: /prometheus
    retention.time: 30d  # 保留 30 天
    retention.size: 100GB  # 最多 100GB
```

### 2. 指标采集优化

```yaml
# 只采集需要的指标
scrape_configs:
  - job_name: 'openidaas-auth'
    metrics_path: '/actuator/prometheus'
    sample_limit: 10000  # 限制每个目标采集的样本数
```

### 3. 告警规则优化

- 使用 `for` 参数避免瞬时告警
- 合理设置告警阈值
- 使用抑制规则减少告警噪音

## 故障排查

### 1. 指标未显示

检查服务是否暴露指标端点：

```bash
curl http://localhost:8080/actuator/prometheus
```

### 2. 告警未触发

检查告警规则是否生效：

```bash
curl http://localhost:9090/api/v1/rules | jq
```

### 3. Grafana 无法连接 Prometheus

检查 Grafana 数据源配置：

1. 访问 http://localhost:3000
2. 进入 Configuration > Data Sources
3. 测试 Prometheus 连接

## 扩展集成

### 1. 钉钉通知

```yaml
receivers:
  - name: 'dingtalk'
    webhook_configs:
      - url: 'https://oapi.dingtalk.com/robot/send?access_token=xxx'
```

### 2. 企业微信通知

```yaml
receivers:
  - name: 'wechat'
    webhook_configs:
      - url: 'https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx'
```

### 3. PagerDuty 集成

```yaml
receivers:
  - name: 'pagerduty'
    pagerduty_configs:
      - service_key: 'YOUR_SERVICE_KEY'
```

## 最佳实践

1. **指标命名规范**
   - 使用 `_<unit>` 后缀表示单位（如 `_bytes`, `_seconds`）
   - 使用 `total` 后缀表示累计值
   - 使用 `rate()` 处理累计指标

2. **告警规则设计**
   - 设置合理的阈值
   - 使用 `for` 参数避免瞬时告警
   - 配置抑制规则减少告警噪音

3. **Dashboard 设计**
   - 使用清晰的标题和描述
   - 合理使用颜色编码
   - 提供上下文信息

4. **监控覆盖**
   - 监控关键业务指标
   - 监控系统资源使用
   - 监控安全事件

## 文档

- [Prometheus 官方文档](https://prometheus.io/docs/)
- [Grafana 官方文档](https://grafana.com/docs/)
- [Alertmanager 官方文档](https://prometheus.io/docs/alerting/latest/alertmanager/)
- [Micrometer 文档](https://micrometer.io/docs)

## 支持

如有问题，请联系 devops@openidaas.com
