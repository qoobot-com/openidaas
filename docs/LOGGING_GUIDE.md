# 日志配置指南

## 目录
1. [日志配置概述](#日志配置概述)
2. [日志文件结构](#日志文件结构)
3. [日志级别](#日志级别)
4. [日志格式](#日志格式)
5. [环境配置](#环境配置)
6. [日志输出](#日志输出)
7. [日志查询](#日志查询)
8. [日志分析](#日志分析)
9. [性能优化](#性能优化)
10. [故障排查](#故障排查)

---

## 日志配置概述

OpenIDaaS 项目使用 **Logback** 作为日志框架，提供统一的日志配置。所有微服务共享相同的日志配置模板，确保日志格式和行为的一致性。

### 日志配置位置

- **统一配置**: `openidaas-common/src/main/resources/logback-spring.xml`
- **服务特定配置**: 各服务的 `application.yml` 可覆盖日志级别

### 日志特性

- ✅ 控制台彩色输出
- ✅ 文件滚动存储
- ✅ 错误日志单独记录
- ✅ 异步日志提升性能
- ✅ 追踪链路支持
- ✅ JSON 格式日志
- ✅ 多环境配置
- ✅ 日志压缩和归档

---

## 日志文件结构

### 日志目录

```
logs/
├── openidaas-gateway.log              # 网关服务日志
├── openidaas-gateway-error.log        # 网关错误日志
├── openidaas-gateway-slow.log         # 网关慢日志
├── openidaas-auth-service.log        # 认证服务日志
├── openidaas-auth-service-error.log   # 认证错误日志
├── openidaas-user-service.log        # 用户服务日志
├── openidaas-user-service-error.log   # 用户错误日志
├── openidaas-audit-service.log       # 审计服务日志
├── openidaas-audit-service-audit.log  # 审计日志（JSON格式）
└── ...
```

### 日志文件命名规则

| 文件类型 | 命名规则 | 说明 |
|---------|---------|------|
| 主日志 | `{APP_NAME}.log` | 所有级别日志 |
| 错误日志 | `{APP_NAME}-error.log` | 仅 ERROR 级别 |
| 慢日志 | `{APP_NAME}-slow.log` | 慢请求日志 |
| 审计日志 | `{APP_NAME}-audit.log` | 审计日志（JSON） |
| 滚动日志 | `{APP_NAME}-{date}.{index}.log` | 按日期滚动 |

### 滚动策略

```xml
<rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
    <fileNamePattern>${LOG_PATH}/${APP_NAME}-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
    <maxFileSize>100MB</maxFileSize>
    <maxHistory>30</maxHistory>
    <totalSizeCap>10GB</totalSizeCap>
</rollingPolicy>
```

**配置说明**:
- `maxFileSize`: 单个日志文件最大 100MB
- `maxHistory`: 保留 30 天日志
- `totalSizeCap`: 总日志大小不超过 10GB
- `%i`: 文件索引（当一天日志超过 100MB 时）

---

## 日志级别

### 日志级别定义

| 级别 | 说明 | 使用场景 |
|-----|------|---------|
| **TRACE** | 最详细的调试信息 | 开发调试，生产环境不使用 |
| **DEBUG** | 调试信息 | 开发和测试环境 |
| **INFO** | 一般信息 | 关键业务操作 |
| **WARN** | 警告信息 | 潜在问题 |
| **ERROR** | 错误信息 | 系统错误、异常 |

### 默认日志级别

#### 开发环境 (dev)

```yaml
logging:
  level:
    root: DEBUG
    com.qoobot.openidaas: DEBUG
    com.qoobot.openidaas.mapper: DEBUG
    org.springframework.web: DEBUG
    org.springframework.cloud.gateway: DEBUG
```

#### 测试环境 (test)

```yaml
logging:
  level:
    root: INFO
    com.qoobot.openidaas: DEBUG
    com.qoobot.openidaas.mapper: DEBUG
```

#### 生产环境 (prod)

```yaml
logging:
  level:
    root: WARN
    com.qoobot.openidaas: INFO
    com.qoobot.openidaas.mapper: WARN
```

### 第三方组件日志级别

| 组件 | 默认级别 | 说明 |
|-----|---------|------|
| Spring | INFO | 框架核心日志 |
| Spring Cloud | INFO | 微服务组件日志 |
| Nacos | INFO | 服务注册发现 |
| Sentinel | INFO | 流量控制 |
| Kafka | WARN | 消息队列 |
| Redis | WARN | 缓存 |
| MyBatis | INFO | SQL 日志 |
| Feign | DEBUG | 服务调用 |

---

## 日志格式

### 控制台格式

```
2024-01-15 10:30:45.123 DEBUG 12345 --- [nio-8081-exec-1] c.q.o.u.controller.UserController  : User created: testuser
```

**字段说明**:
- `2024-01-15 10:30:45.123`: 时间戳
- `DEBUG`: 日志级别（彩色）
- `12345`: 进程 ID
- `[nio-8081-exec-1]`: 线程名
- `c.q.o.u.controller.UserController`: Logger 名称
- `User created: testuser`: 日志消息

### 文件格式

```
2024-01-15 10:30:45.123 DEBUG 12345 --- [http-nio-8081-exec-1] com.qoobot.openidaas.user.controller.UserController : User created: testuser
```

**额外信息**:
- 完整类名
- 异常堆栈（最多 5 行）

### JSON 格式

```json
{
  "timestamp": "2024-01-15 10:30:45.123",
  "level": "DEBUG",
  "pid": "12345",
  "thread": "http-nio-8081-exec-1",
  "logger": "com.qoobot.openidaas.user.controller.UserController",
  "message": "User created: testuser",
  "traceId": "a1b2c3d4e5f6",
  "spanId": "b2c3d4e5f6a1"
}
```

**用途**: ELK 日志分析、Prometheus 指标提取

---

## 环境配置

### 开发环境

#### 配置特点
- ✅ 详细的 DEBUG 日志
- ✅ 控制台彩色输出
- ✅ 实时日志查看
- ✅ SQL 日志输出

#### 配置示例

```yaml
# application-dev.yml
logging:
  level:
    root: DEBUG
    com.qoobot.openidaas: DEBUG
    com.qoobot.openidaas.mapper: DEBUG
    org.springframework.web: DEBUG
    org.springframework.cloud.gateway: DEBUG
  pattern:
    console: "%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n"
```

### 测试环境

#### 配置特点
- ✅ DEBUG 级别用于测试
- ✅ 完整日志文件
- ✅ 错误日志单独记录
- ✅ SQL 日志输出

#### 配置示例

```yaml
# application-test.yml
logging:
  level:
    root: INFO
    com.qoobot.openidaas: DEBUG
    com.qoobot.openidaas.mapper: DEBUG
  file:
    name: /var/log/openidaas/${spring.application.name}.log
```

### 生产环境

#### 配置特点
- ✅ WARN/ERROR 级别
- ✅ 仅文件输出
- ✅ 错误日志单独记录
- ✅ 日志压缩和归档
- ✅ 异步日志提升性能

#### 配置示例

```yaml
# application-prod.yml
logging:
  level:
    root: WARN
    com.qoobot.openidaas: INFO
    com.qoobot.openidaas.mapper: WARN
  file:
    name: /var/log/openidaas/${spring.application.name}.log
  logback:
    rollingpolicy:
      max-file-size: 100MB
      max-history: 30
      total-size-cap: 10GB
```

---

## 日志输出

### 使用 Slf4j

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {
    
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    public void createUser(UserDTO dto) {
        log.debug("Creating user: {}", dto.getUsername());
        log.info("User created successfully: {}", dto.getUsername());
        log.warn("User already exists: {}", dto.getUsername());
        log.error("Failed to create user", e);
    }
}
```

### 使用 Lombok

```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {
    
    public void createUser(UserDTO dto) {
        log.debug("Creating user: {}", dto.getUsername());
        log.info("User created successfully: {}", dto.getUsername());
        log.warn("User already exists: {}", dto.getUsername());
        log.error("Failed to create user", e);
    }
}
```

### 结构化日志

```java
@Data
public class AuditLog {
    private String userId;
    private String action;
    private String resource;
    private String result;
    private Long timestamp;
}

@Slf4j
@Service
public class AuditService {
    
    public void logUserAction(String userId, String action, String resource) {
        AuditLog auditLog = AuditLog.builder()
            .userId(userId)
            .action(action)
            .resource(resource)
            .result("SUCCESS")
            .timestamp(System.currentTimeMillis())
            .build();
        
        log.info("User action: {}", auditLog);
    }
}
```

---

## 日志查询

### 本地查询

```bash
# 查看实时日志
tail -f logs/openidaas-user-service.log

# 查看错误日志
tail -f logs/openidaas-user-service-error.log

# 搜索关键字
grep "ERROR" logs/openidaas-user-service.log

# 查看最近 100 行
tail -n 100 logs/openidaas-user-service.log

# 查看特定时间范围
awk '/2024-01-15 10:00:00/,/2024-01-15 11:00:00/' logs/openidaas-user-service.log
```

### Kubernetes 查询

```bash
# 查看所有 Pod 日志
kubectl logs -f -l app=user-service -n openidaas

# 查看特定 Pod 日志
kubectl logs -f user-service-7d8f9b6c5d-x4k2p -n openidaas

# 查看前一个版本日志
kubectl logs user-service-7d8f9b6c5d-x4k2p --previous -n openidaas

# 查看最近 100 行
kubectl logs user-service-7d8f9b6c5d-x4k2p --tail=100 -n openidaas

# 搜索日志
kubectl logs user-service-7d8f9b6c5d-x4k2p | grep "ERROR" -n openidaas
```

### 按追踪 ID 查询

```bash
# 通过 traceId 查询整个链路日志
grep "traceId=a1b2c3d4e5f6" logs/*.log

# Kubernetes 查询
kubectl logs -f -l app=user-service | grep "a1b2c3d4e5f6"
```

---

## 日志分析

### ELK Stack

#### Filebeat 配置

```yaml
# filebeat.yml
filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /var/log/openidaas/*.log
  fields:
    app: openidaas
  multiline.pattern: '^\d{4}-\d{2}-\d{2}'
  multiline.negate: true
  multiline.match: after

output.elasticsearch:
  hosts: ["elasticsearch:9200"]
  index: "openidaas-%{+yyyy.MM.dd}"
```

### Prometheus 日志指标

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'openidaas-logs'
    static_configs:
      - targets: ['localhost:8081']
    metrics_path: '/actuator/prometheus'
    metric_relabel_configs:
      - source_labels: [__name__]
        regex: 'logback_.*'
        action: keep
```

### Grafana 日志查询

```sql
-- 查询错误日志
SELECT timestamp, level, message
FROM "openidaas"
WHERE level = 'ERROR'
  AND time > now() - 1h
ORDER BY timestamp DESC
LIMIT 100

-- 查询慢请求
SELECT *
FROM "openidaas"
WHERE logger = 'com.qoobot.openidaas.slow'
  AND time > now() - 24h
```

---

## 性能优化

### 异步日志

**配置**:
```xml
<appender name="ASYNC_FILE" class="ch.qos.logback.classic.AsyncAppender">
    <appender-ref ref="FILE"/>
    <queueSize>1024</queueSize>
    <discardingThreshold>0</discardingThreshold>
</appender>
```

**优化建议**:
- `queueSize`: 队列大小，默认 256
- `discardingThreshold`: 丢弃阈值，0 表示不丢弃
- 生产环境建议 `queueSize=1024`, `discardingThreshold=0`

### 日志级别控制

**开发环境**:
```yaml
logging:
  level:
    root: DEBUG
```

**生产环境**:
```yaml
logging:
  level:
    root: WARN
    com.qoobot.openidaas: INFO
    com.qoobot.openidaas.mapper: WARN
```

### 日志滚动优化

**配置**:
```xml
<rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
    <fileNamePattern>${LOG_PATH}/${APP_NAME}-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
    <maxFileSize>100MB</maxFileSize>
    <maxHistory>30</maxHistory>
    <totalSizeCap>10GB</totalSizeCap>
</rollingPolicy>
```

**优化建议**:
- `maxFileSize`: 50-200MB
- `maxHistory`: 7-30 天
- `totalSizeCap`: 5-20GB

---

## 故障排查

### 问题1: 日志文件不生成

**可能原因**:
- 目录权限不足
- 磁盘空间不足
- 配置路径错误

**排查方法**:
```bash
# 检查目录权限
ls -la /var/log/openidaas/

# 检查磁盘空间
df -h

# 检查配置
grep "LOG_PATH" application.yml
```

### 问题2: 日志不输出到文件

**可能原因**:
- 日志级别配置错误
- Appender 配置错误

**排查方法**:
```bash
# 检查日志级别
curl http://localhost:8081/actuator/loggers/com.qoobot.openidaas

# 查看 Logback 配置
cat src/main/resources/logback-spring.xml
```

### 问题3: 日志丢失

**可能原因**:
- 异步队列满
- 日志文件被删除

**排查方法**:
```bash
# 检查异步队列状态
grep "queueSize" logback-spring.xml

# 检查日志文件
ls -la /var/log/openidaas/
```

---

## 附录

### 日志配置清单

- [ ] 开发环境使用 DEBUG 级别
- [ ] 生产环境使用 INFO/WARN 级别
- [ ] 错误日志单独记录
- [ ] 日志文件滚动配置
- [ ] 异步日志启用
- [ ] 追踪 ID 支持
- [ ] 日志格式统一
- [ ] 日志目录权限正确
- [ ] 磁盘空间充足

### 日志目录权限

```bash
# 创建日志目录
sudo mkdir -p /var/log/openidaas

# 设置权限
sudo chown -R $USER:$USER /var/log/openidaas
sudo chmod -R 755 /var/log/openidaas
```

### 日志清理脚本

```bash
#!/bin/bash
# clean-logs.sh

LOG_DIR="/var/log/openidaas"
DAYS_TO_KEEP=30

# 清理旧日志
find $LOG_DIR -name "*.log" -mtime +$DAYS_TO_KEEP -delete

# 压缩日志
find $LOG_DIR -name "*.log" -mtime +7 -exec gzip {} \;

echo "Logs cleaned and compressed"
```

### Docker 日志配置

```yaml
# docker-compose.yml
services:
  user-service:
    logging:
      driver: "json-file"
      options:
        max-size: "100m"
        max-file: "3"
    volumes:
      - ./logs:/var/log/openidaas
```

### Kubernetes 日志配置

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: user-service
spec:
  containers:
  - name: user-service
    volumeMounts:
    - name: log-volume
      mountPath: /var/log/openidaas
  volumes:
  - name: log-volume
    hostPath:
      path: /var/log/openidaas
```
