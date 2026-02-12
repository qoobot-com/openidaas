# OpenIDaaS 监控告警配置指南

## 目录

- [概述](#概述)
- [监控架构](#监控架构)
- [Actuator 配置](#actuator-配置)
- [Prometheus 配置](#prometheus-配置)
- [Grafana 配置](#grafana-配置)
- [告警规则](#告警规则)
- [监控指标](#监控指标)
- [Docker Compose 部署](#docker-compose-部署)
- [Kubernetes 部署](#kubernetes-部署)
- [常见问题](#常见问题)

## 概述

OpenIDaaS 采用 **Prometheus + Grafana** 监控方案，为所有微服务提供完整的监控能力。

### 监控能力

- ✅ 服务健康检查
- ✅ JVM 监控
- ✅ HTTP 请求监控
- ✅ 数据库连接池监控
- ✅ Redis 缓存监控
- ✅ 自定义业务指标
- ✅ 分布式追踪
- ✅ 告警通知

### 监控组件

| 组件 | 版本 | 端口 | 说明 |
|-----|------|------|------|
| Prometheus | 2.45+ | 9090 | 指标采集和存储 |
| Grafana | 10.0+ | 3000 | 可视化仪表板 |
| Alertmanager | 0.25+ | 9093 | 告警管理 |
| Node Exporter | 1.6+ | 9100 | 服务器指标 |
| cAdvisor | 0.47+ | 8080 | 容器指标 |

## 监控架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         OpenIDaaS 集群                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │ Auth Service │  │ User Service │  │ Gateway      │         │
│  │ :8082        │  │ :8081        │  │ :8080        │         │
│  │ /actuator    │  │ /actuator    │  │ /actuator    │         │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘         │
│         │                 │                 │                  │
│         └─────────────────┼─────────────────┘                  │
│                           │                                    │
│                    /metrics (Prometheus 格式)                  │
│                           │                                    │
└───────────────────────────┼────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                      Prometheus 服务器                            │
│                         :9090                                     │
│  - 每 15s 采集一次指标                                           │
│  - 存储时间序列数据                                               │
│  - 计算告警规则                                                   │
│  - 发送告警到 Alertmanager                                        │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                      Alertmanager                                │
│                         :9093                                     │
│  - 接收告警                                                       │
│  - 去重和分组                                                     │
│  - 路由到接收器                                                   │
│  - 发送通知（邮件、钉钉、企业微信）                                │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                      Grafana                                    │
│                         :3000                                     │
│  - 查询 Prometheus 数据                                           │
│  - 展示仪表板                                                     │
│  - 配置告警规则                                                   │
│  - 导入 Dashboard 模板                                            │
└─────────────────────────────────────────────────────────────────┘
```

## Actuator 配置

### 统一 Actuator 配置

所有服务共享以下 Actuator 配置：

```yaml
management:
  # 端点配置
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,loggers,httptrace
      base-path: /actuator
    enabled-by-default: false
  
  # 端点启用状态
  endpoint:
    health:
      enabled: true
      show-details: always
      show-components: always
    info:
      enabled: true
    metrics:
      enabled: true
    prometheus:
      enabled: true
    loggers:
      enabled: true
    httptrace:
      enabled: true
  
  # 健康检查配置
  health:
    db:
      enabled: true
    redis:
      enabled: true
    defaults:
      enabled: true
    probes:
      enabled: true
      liveness-state:
        enabled: true
      readiness-state:
        enabled: true
  
  # 指标配置
  metrics:
    enabled: true
    export:
      prometheus:
        enabled: true
        step: 1m
        descriptions: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5,0.95,0.99
      slo:
        http.server.requests: 100ms,200ms,500ms,1s,2s
    tags:
      application: ${spring.application.name}
      environment: ${ENVIRONMENT:dev}
  
  # Tracing配置
  tracing:
    sampling:
      probability: 0.1  # 10%采样率
```

### 端点说明

| 端点 | 路径 | 说明 |
|-----|------|------|
| Health | `/actuator/health` | 健康检查 |
| Info | `/actuator/info` | 应用信息 |
| Metrics | `/actuator/metrics` | JVM 指标 |
| Prometheus | `/actuator/prometheus` | Prometheus 指标 |
| Loggers | `/actuator/loggers` | 日志配置 |
| Httptrace | `/actuator/httptrace` | HTTP 追踪 |

### Health 端点示例

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 250000000000,
        "threshold": 10485760,
        "path": "/",
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0"
      }
    }
  }
}
```

### Prometheus 指标示例

```prometheus
# HTTP 请求数
http_server_requests_seconds_count{exception="none",method="GET",outcome="SUCCESS",status="200",uri="/api/users"}

# JVM 内存
jvm_memory_used_bytes{area="heap",id="G1 Eden Space"}

# 数据库连接池
hikaricp_connections_active{pool="UserHikariCP"}

# Redis 连接
lettuce_connections_active
```

## Prometheus 配置

### Prometheus 配置文件

`prometheus/prometheus.yml`:

```yaml
global:
  # 采集间隔
  scrape_interval: 15s
  # 规则评估间隔
  evaluation_interval: 15s
  # 外部标签
  external_labels:
    cluster: 'openidaas-cluster'
    environment: '${ENVIRONMENT:production}'

# 告警规则文件
rule_files:
  - 'alert_rules/*.yml'

# 监控目标配置
scrape_configs:
  # Prometheus 自监控
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  # 网关服务
  - job_name: 'openidaas-gateway'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['openidaas-gateway:8080']
        labels:
          service: 'gateway'
          component: 'gateway'
    scrape_interval: 10s
    scrape_timeout: 5s

  # 认证服务
  - job_name: 'openidaas-auth-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['openidaas-auth-service:8082']
        labels:
          service: 'auth-service'
          component: 'auth'
    scrape_interval: 10s

  # 用户服务
  - job_name: 'openidaas-user-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['openidaas-user-service:8081']
        labels:
          service: 'user-service'
          component: 'user'
    scrape_interval: 10s

  # 组织服务
  - job_name: 'openidaas-organization-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['openidaas-organization-service:8083']
        labels:
          service: 'organization-service'
          component: 'organization'
    scrape_interval: 10s

  # 角色服务
  - job_name: 'openidaas-role-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['openidaas-role-service:8084']
        labels:
          service: 'role-service'
          component: 'role'
    scrape_interval: 10s

  # 应用服务
  - job_name: 'openidaas-application-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['openidaas-application-service:8086']
        labels:
          service: 'application-service'
          component: 'application'
    scrape_interval: 10s

  # 审计服务
  - job_name: 'openidaas-audit-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['openidaas-audit-service:8085']
        labels:
          service: 'audit-service'
          component: 'audit'
    scrape_interval: 10s

  # Kubernetes 服务发现
  - job_name: 'kubernetes-pods'
    metrics_path: '/actuator/prometheus'
    kubernetes_sd_configs:
      - role: pod
        namespaces:
          names:
            - openidaas
    relabel_configs:
      - source_labels: [__meta_kubernetes_pod_name]
        target_label: pod_name
      - source_labels: [__meta_kubernetes_pod_label_app]
        target_label: app
      - source_labels: [__meta_kubernetes_pod_label_app]
        regex: .+
        action: keep

  # Node Exporter（服务器指标）
  - job_name: 'node-exporter'
    static_configs:
      - targets: ['node-exporter:9100']
        labels:
          service: 'node-exporter'

  # Alertmanager
  - job_name: 'alertmanager'
    static_configs:
      - targets: ['alertmanager:9093']
```

### Alertmanager 配置

`alertmanager/alertmanager.yml`:

```yaml
global:
  # 全局解析超时
  resolve_timeout: 5m
  # SMTP 配置
  smtp_smarthost: 'smtp.example.com:587'
  smtp_from: 'alerts@openidaas.com'
  smtp_auth_username: 'alerts@openidaas.com'
  smtp_auth_password: '${SMTP_PASSWORD}'
  smtp_require_tls: true

# 路由配置
route:
  # 分组等待时间
  group_wait: 10s
  # 分组间隔
  group_interval: 10s
  # 重复等待时间
  repeat_interval: 1h
  # 默认接收器
  receiver: 'default'
  
  # 子路由
  routes:
    # 关键告警
    - match:
        severity: critical
      receiver: 'critical-alerts'
      group_wait: 30s
    
    # 警告告警
    - match:
        severity: warning
      receiver: 'warning-alerts'
      group_wait: 5m

# 接收器配置
receivers:
  # 默认接收器
  - name: 'default'
    webhook_configs:
      - url: 'http://alert-webhook:8080/webhook'
    
  # 关键告警接收器
  - name: 'critical-alerts'
    email_configs:
      - to: 'ops@openidaas.com'
        headers:
          Subject: '[CRITICAL] OpenIDaaS Alert'
    webhook_configs:
      - url: 'http://alert-webhook:8080/webhook/critical'
        send_resolved: true
    wechat_configs:
      - corp_id: '${WECHAT_CORP_ID}'
        api_secret: '${WECHAT_API_SECRET}'
        to_party: '1'
        agent_id: '${WECHAT_AGENT_ID}'
        send_resolved: true
    
  # 警告告警接收器
  - name: 'warning-alerts'
    email_configs:
      - to: 'dev@openidaas.com'
        headers:
          Subject: '[WARNING] OpenIDaaS Alert'
    webhook_configs:
      - url: 'http://alert-webhook:8080/webhook/warning'

# 抑制规则
inhibit_rules:
  # 如果服务宕机，抑制其他告警
  - source_match:
      severity: 'critical'
      alertname: 'ServiceDown'
    target_match_re:
      alertname: '.*'
    equal: ['service', 'instance']
```

## Grafana 配置

### Grafana 数据源配置

```json
{
  "name": "OpenIDaaS-Prometheus",
  "type": "prometheus",
  "url": "http://prometheus:9090",
  "access": "proxy",
  "isDefault": true,
  "editable": true,
  "jsonData": {
    "timeInterval": "15s"
  }
}
```

### 推荐仪表板

| 仪表板 | ID | 说明 |
|-------|----|------|
| JVM Micrometer | 4701 | JVM 监控 |
| Spring Boot Statistics | 12900 | Spring Boot 统计 |
| Prometheus Stats | 3662 | Prometheus 状态 |
| Node Exporter Full | 1860 | 服务器指标 |

### 自定义仪表板示例

```json
{
  "dashboard": {
    "title": "OpenIDaaS 服务监控",
    "panels": [
      {
        "title": "服务 QPS",
        "targets": [
          {
            "expr": "sum(rate(http_server_requests_seconds_count{service=~\".*\"}[5m])) by (service)"
          }
        ],
        "type": "graph"
      },
      {
        "title": "响应时间 P95",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{service=~\".*\"}[5m])) by (service, le))"
          }
        ],
        "type": "graph"
      },
      {
        "title": "错误率",
        "targets": [
          {
            "expr": "sum(rate(http_server_requests_seconds_count{status=~\"5..\"}[5m])) by (service) / sum(rate(http_server_requests_seconds_count[5m])) by (service)"
          }
        ],
        "type": "graph"
      }
    ]
  }
}
```

## 告警规则

### 告警规则配置

`alert_rules/services.yml`:

```yaml
groups:
  - name: service_availability
    interval: 30s
    rules:
      # 服务宕机告警
      - alert: ServiceDown
        expr: up{job=~"openidaas.*"} == 0
        for: 1m
        labels:
          severity: critical
          component: service
        annotations:
          summary: "服务宕机"
          description: "服务 {{ $labels.job }} 已宕机超过1分钟"

      # 服务不可用告警
      - alert: ServiceUnavailable
        expr: up{job=~"openidaas.*"} < 1
        for: 5m
        labels:
          severity: warning
          component: service
        annotations:
          summary: "服务不可用"
          description: "服务 {{ $labels.job }} 部分实例不可用"

  - name: application_performance
    interval: 30s
    rules:
      # 高错误率告警
      - alert: HighErrorRate
        expr: |
          sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) by (service)
          /
          sum(rate(http_server_requests_seconds_count[5m])) by (service)
          > 0.05
        for: 5m
        labels:
          severity: warning
          component: application
        annotations:
          summary: "服务错误率过高"
          description: "服务 {{ $labels.service }} 错误率超过5%，当前错误率: {{ $value | humanizePercentage }}"

      # 响应时间告警
      - alert: HighResponseTime
        expr: |
          histogram_quantile(0.95, 
            sum(rate(http_server_requests_seconds_bucket{service=~".*"}[5m])) by (service, le)
          ) > 1
        for: 5m
        labels:
          severity: warning
          component: application
        annotations:
          summary: "响应时间过高"
          description: "服务 {{ $labels.service }} 95%请求响应时间超过1秒，当前P95: {{ $value }}s"

      # 慢请求告警
      - alert: SlowRequests
        expr: |
          rate(http_server_requests_seconds_count{le="+Inf"}[5m])
          -
          rate(http_server_requests_seconds_count{le="1"}[5m])
          > 10
        for: 10m
        labels:
          severity: warning
          component: application
        annotations:
          summary: "慢请求过多"
          description: "服务 {{ $labels.service }} 慢请求数超过10个/秒"

  - name: jvm_metrics
    interval: 1m
    rules:
      # JVM 内存告警
      - alert: HighHeapMemoryUsage
        expr: |
          jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.8
        for: 5m
        labels:
          severity: warning
          component: jvm
        annotations:
          summary: "JVM 堆内存使用率过高"
          description: "服务 {{ $labels.service }} JVM 堆内存使用率超过80%，当前使用率: {{ $value | humanizePercentage }}"

      # JVM 非堆内存告警
      - alert: HighNonHeapMemoryUsage
        expr: |
          jvm_memory_used_bytes{area="nonheap"} / jvm_memory_max_bytes{area="nonheap"} > 0.8
        for: 5m
        labels:
          severity: warning
          component: jvm
        annotations:
          summary: "JVM 非堆内存使用率过高"
          description: "服务 {{ $labels.service }} JVM 非堆内存使用率超过80%"

      # GC 频率告警
      - alert: HighGCRate
        expr: |
          rate(jvm_gc_pause_seconds_count[5m]) > 1
        for: 10m
        labels:
          severity: warning
          component: jvm
        annotations:
          summary: "GC 频率过高"
          description: "服务 {{ $labels.service }} GC 频率超过1次/秒"

  - name: database_metrics
    interval: 1m
    rules:
      # 数据库连接池告警
      - alert: HighDatabaseConnectionUsage
        expr: |
          hikaricp_connections_active / hikaricp_connections_max > 0.8
        for: 5m
        labels:
          severity: warning
          component: database
        annotations:
          summary: "数据库连接池使用率过高"
          description: "服务 {{ $labels.service }} 数据库连接池使用率超过80%，当前使用率: {{ $value | humanizePercentage }}"

      # 数据库连接等待告警
      - alert: DatabaseConnectionWaiting
        expr: |
          hikaricp_connections_pending > 10
        for: 2m
        labels:
          severity: warning
          component: database
        annotations:
          summary: "数据库连接等待过多"
          description: "服务 {{ $labels.service }} 数据库连接等待数超过10个"

  - name: redis_metrics
    interval: 1m
    rules:
      # Redis 连接池告警
      - alert: HighRedisConnectionUsage
        expr: |
          lettuce_connections_active / lettuce_connections_max > 0.8
        for: 5m
        labels:
          severity: warning
          component: redis
        annotations:
          summary: "Redis 连接池使用率过高"
          description: "服务 {{ $labels.service }} Redis 连接池使用率超过80%"

  - name: system_metrics
    interval: 1m
    rules:
      # CPU 使用率告警
      - alert: HighCPUUsage
        expr: |
          100 - (avg by (instance) (irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100) > 80
        for: 5m
        labels:
          severity: warning
          component: system
        annotations:
          summary: "CPU 使用率过高"
          description: "实例 {{ $labels.instance }} CPU 使用率超过80%，当前使用率: {{ $value }}%"

      # 内存使用率告警
      - alert: HighMemoryUsage
        expr: |
          (1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100 > 80
        for: 5m
        labels:
          severity: warning
          component: system
        annotations:
          summary: "内存使用率过高"
          description: "实例 {{ $labels.instance }} 内存使用率超过80%，当前使用率: {{ $value }}%"

      # 磁盘空间告警
      - alert: LowDiskSpace
        expr: |
          (node_filesystem_avail_bytes{mountpoint="/"} / node_filesystem_size_bytes{mountpoint="/"}) * 100 < 10
        for: 5m
        labels:
          severity: warning
          component: system
        annotations:
          summary: "磁盘空间不足"
          description: "实例 {{ $labels.instance }} 磁盘可用空间低于10%，当前可用率: {{ $value }}%"
```

## 监控指标

### JVM 指标

| 指标 | 说明 |
|-----|------|
| `jvm_memory_used_bytes` | JVM 内存使用量 |
| `jvm_memory_max_bytes` | JVM 内存最大值 |
| `jvm_gc_pause_seconds_count` | GC 次数 |
| `jvm_gc_pause_seconds_sum` | GC 总耗时 |
| `jvm_threads_live_threads` | 活跃线程数 |
| `jvm_classes_loaded_classes` | 加载的类数 |

### HTTP 请求指标

| 指标 | 说明 |
|-----|------|
| `http_server_requests_seconds_count` | HTTP 请求数 |
| `http_server_requests_seconds_sum` | HTTP 请求总耗时 |
| `http_server_requests_seconds_max` | HTTP 请求最大耗时 |

### 数据库指标

| 指标 | 说明 |
|-----|------|
| `hikaricp_connections_active` | 活跃连接数 |
| `hikaricp_connections_idle` | 空闲连接数 |
| `hikaricp_connections_pending` | 等待连接数 |
| `hikaricp_connections_max` | 最大连接数 |

### Redis 指标

| 指标 | 说明 |
|-----|------|
| `lettuce_connections_active` | 活跃连接数 |
| `lettuce_connections_idle` | 空闲连接数 |
| `lettuce_connections_max` | 最大连接数 |

## Docker Compose 部署

### Docker Compose 配置

`docker-compose.monitoring.yml`:

```yaml
version: '3.8'

services:
  # Prometheus
  prometheus:
    image: prom/prometheus:v2.45.0
    container_name: openidaas-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - ./prometheus/alert_rules:/etc/prometheus/alert_rules
      - prometheus-data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/usr/share/prometheus/console_libraries'
      - '--web.console.templates=/usr/share/prometheus/consoles'
      - '--storage.tsdb.retention.time=30d'
    networks:
      - monitoring

  # Grafana
  grafana:
    image: grafana/grafana:10.0.3
    container_name: openidaas-grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_USER=${GRAFANA_ADMIN_USER:-admin}
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_ADMIN_PASSWORD:-admin}
      - GF_INSTALL_PLUGINS=grafana-piechart-panel
    volumes:
      - grafana-data:/var/lib/grafana
      - ./grafana/provisioning:/etc/grafana/provisioning
      - ./grafana/dashboards:/var/lib/grafana/dashboards
    networks:
      - monitoring
    depends_on:
      - prometheus

  # Alertmanager
  alertmanager:
    image: prom/alertmanager:v0.25.0
    container_name: openidaas-alertmanager
    ports:
      - "9093:9093"
    volumes:
      - ./alertmanager/alertmanager.yml:/etc/alertmanager/alertmanager.yml
      - alertmanager-data:/alertmanager
    command:
      - '--config.file=/etc/alertmanager/alertmanager.yml'
      - '--storage.path=/alertmanager'
    networks:
      - monitoring

  # Node Exporter
  node-exporter:
    image: prom/node-exporter:v1.6.1
    container_name: openidaas-node-exporter
    ports:
      - "9100:9100"
    command:
      - '--path.rootfs=/host'
    volumes:
      - /:/host:ro,rslave
    networks:
      - monitoring

  # cAdvisor
  cadvisor:
    image: gcr.io/cadvisor/cadvisor:v0.47.0
    container_name: openidaas-cadvisor
    ports:
      - "8081:8080"
    volumes:
      - /:/rootfs:ro
      - /var/run:/var/run:ro
      - /sys:/sys:ro
      - /var/lib/docker/:/var/lib/docker:ro
      - /dev/disk/:/dev/disk:ro
    networks:
      - monitoring

volumes:
  prometheus-data:
  grafana-data:
  alertmanager-data:

networks:
  monitoring:
    driver: bridge
```

### 启动监控服务

```bash
# 启动监控服务
docker-compose -f docker-compose.monitoring.yml up -d

# 查看服务状态
docker-compose -f docker-compose.monitoring.yml ps

# 查看日志
docker-compose -f docker-compose.monitoring.yml logs -f prometheus

# 停止监控服务
docker-compose -f docker-compose.monitoring.yml down
```

## Kubernetes 部署

### Prometheus 部署

`k8s/monitoring/prometheus-deployment.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
  namespace: openidaas
data:
  prometheus.yml: |
    global:
      scrape_interval: 15s
      evaluation_interval: 15s

    scrape_configs:
      - job_name: 'kubernetes-pods'
        metrics_path: '/actuator/prometheus'
        kubernetes_sd_configs:
          - role: pod
            namespaces:
              names:
                - openidaas
        relabel_configs:
          - source_labels: [__meta_kubernetes_pod_name]
            target_label: pod_name
          - source_labels: [__meta_kubernetes_pod_label_app]
            target_label: app
          - source_labels: [__meta_kubernetes_pod_label_app]
            regex: .+
            action: keep
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: prometheus
  namespace: openidaas
spec:
  replicas: 1
  selector:
    matchLabels:
      app: prometheus
  template:
    metadata:
      labels:
        app: prometheus
    spec:
      containers:
        - name: prometheus
          image: prom/prometheus:v2.45.0
          ports:
            - containerPort: 9090
          volumeMounts:
            - name: config
              mountPath: /etc/prometheus
            - name: data
              mountPath: /prometheus
          args:
            - '--config.file=/etc/prometheus/prometheus.yml'
            - '--storage.tsdb.path=/prometheus'
      volumes:
        - name: config
          configMap:
            name: prometheus-config
        - name: data
          emptyDir: {}
---
apiVersion: v1
kind: Service
metadata:
  name: prometheus
  namespace: openidaas
spec:
  selector:
    app: prometheus
  ports:
    - port: 9090
      targetPort: 9090
  type: ClusterIP
```

### Grafana 部署

`k8s/monitoring/grafana-deployment.yaml`:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: grafana-secret
  namespace: openidaas
type: Opaque
data:
  GF_SECURITY_ADMIN_PASSWORD: YWRtaW4=
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: grafana
  namespace: openidaas
spec:
  replicas: 1
  selector:
    matchLabels:
      app: grafana
  template:
    metadata:
      labels:
        app: grafana
    spec:
      containers:
        - name: grafana
          image: grafana/grafana:10.0.3
          ports:
            - containerPort: 3000
          env:
            - name: GF_SECURITY_ADMIN_USER
              value: admin
            - name: GF_SECURITY_ADMIN_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: grafana-secret
                  key: GF_SECURITY_ADMIN_PASSWORD
          volumeMounts:
            - name: data
              mountPath: /var/lib/grafana
      volumes:
        - name: data
          emptyDir: {}
---
apiVersion: v1
kind: Service
metadata:
  name: grafana
  namespace: openidaas
spec:
  selector:
    app: grafana
  ports:
    - port: 3000
      targetPort: 3000
  type: LoadBalancer
```

### 部署监控组件

```bash
# 部署 Prometheus
kubectl apply -f k8s/monitoring/prometheus-deployment.yaml

# 部署 Grafana
kubectl apply -f k8s/monitoring/grafana-deployment.yaml

# 查看部署状态
kubectl get pods -n openidaas -l app=prometheus
kubectl get pods -n openidaas -l app=grafana

# 端口转发
kubectl port-forward svc/prometheus 9090:9090 -n openidaas
kubectl port-forward svc/grafana 3000:3000 -n openidaas
```

## 常见问题

### Prometheus 无法采集指标

**问题**: Prometheus 无法采集服务指标

**解决方法**:
1. 检查服务健康状态
2. 确认 `/actuator/prometheus` 端点可访问
3. 检查防火墙规则
4. 查看 Prometheus 日志

### 告警未触发

**问题**: 告警规则未触发

**解决方法**:
1. 检查告警规则配置
2. 确认 Prometheus 能够采集指标
3. 查看 Prometheus 告警界面
4. 检查 Alertmanager 配置

### Grafana 无法连接 Prometheus

**问题**: Grafana 无法查询 Prometheus 数据

**解决方法**:
1. 检查 Prometheus 数据源配置
2. 确认 Prometheus 可访问
3. 查看 Grafana 日志

---

## 附录

### 参考资源

- [Prometheus 官方文档](https://prometheus.io/docs/)
- [Grafana 官方文档](https://grafana.com/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer 文档](https://micrometer.io/docs)

### 仪表板模板

- [Grafana 官方仪表板](https://grafana.com/grafana/dashboards/)
- [Spring Boot 2.1 Statistics](https://grafana.com/grafana/dashboards/12900)
- [JVM (Micrometer)](https://grafana.com/grafana/dashboards/4701)

---

**文档版本**: 1.0  
**最后更新**: 2024-01-15
