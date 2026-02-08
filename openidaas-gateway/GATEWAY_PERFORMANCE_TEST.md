# 网关模块性能测试方案

## 测试目标

验证 openidaas-gateway 模块在以下场景下的性能表现：

- **网关转发性能**: 10万 QPS
- **路由配置热更新**: 毫秒级生效
- **限流熔断机制**: 准确触发
- **认证验证性能**: < 10ms

## 测试环境

### 硬件配置

- CPU: 8核+
- 内存: 16GB+
- 磁盘: SSD
- 网络: 1Gbps

### 软件环境

- JDK: 25
- Redis: 6+
- Nacos: 2.3+
- JMeter: 5.6+
- wrk: 最新版本

## 测试工具

### wrk

```bash
# 安装wrk
git clone https://github.com/wg/wrk.git
cd wrk
make

# 运行测试
wrk -t4 -c1000 -d30s --latency http://localhost:8080/api/users
```

### JMeter

```bash
# 命令行运行
jmeter -n -t gateway_test.jmx -l results.jtl -e -o report
```

### ab (Apache Bench)

```bash
# 安装ab
apt-get install apache2-utils

# 运行测试
ab -n 100000 -c 1000 http://localhost:8080/api/users
```

## 测试场景

### 1. 网关转发性能测试

#### 测试目标

- QPS > 100,000
- P99延迟 < 50ms
- 错误率 < 0.1%

#### 测试用例

```bash
# 测试1: 轻负载（100并发）
wrk -t4 -c100 -d60s --latency \
  -H "Authorization: Bearer TOKEN" \
  http://localhost:8080/api/users?page=0&size=20

# 测试2: 中等负载（1000并发）
wrk -t4 -c1000 -d60s --latency \
  -H "Authorization: Bearer TOKEN" \
  http://localhost:8080/api/users?page=0&size=20

# 测试3: 高负载（10000并发）
wrk -t8 -c10000 -d60s --latency \
  -H "Authorization: Bearer TOKEN" \
  http://localhost:8080/api/users?page=0&size=20

# 测试4: 峰值负载（50000并发）
wrk -t16 -c50000 -d30s --latency \
  -H "Authorization: Bearer TOKEN" \
  http://localhost:8080/api/users?page=0&size=20
```

#### 预期结果

| 并发数 | QPS | P50 | P95 | P99 | 错误率 |
|--------|-----|-----|-----|-----|--------|
| 100    | >10K | <10ms| <20ms| <30ms| <0.05% |
| 1000   | >50K | <15ms| <30ms| <50ms| <0.1% |
| 10000  | >100K| <25ms| <40ms| <60ms| <0.1% |
| 50000  | >100K| <30ms| <50ms| <100ms| <0.2% |

### 2. 路由配置热更新测试

#### 测试目标

- 配置更新 < 1秒生效
- 不影响正在处理的请求
- 路由切换无感知

#### 测试步骤

```bash
# 1. 初始配置测试
curl http://localhost:8080/api/users -v
# 预期：路由到 openidaas-user

# 2. 修改路由配置（修改uri到测试服务）
# 修改 application.yml

# 3. 触发配置刷新
curl -X POST http://localhost:8080/actuator/refresh

# 4. 验证新配置
curl http://localhost:8080/api/users -v
# 预期：路由到新的服务

# 5. 监控日志
tail -f logs/openidaas-gateway.log | grep "RouteLocator"
```

#### 预期结果

- 配置刷新耗时 < 1000ms
- 旧请求正常完成
- 新请求使用新路由
- 无错误日志

### 3. 限流熔断机制验证

#### 3.1 限流测试

```bash
# 测试限流功能
for i in {1..200}; do
  curl -w "\nHTTP Status: %{http_code}\n" \
    http://localhost:8080/api/users &
done
wait
```

#### 预期结果

- 前100次请求返回200
- 后100次请求返回429
- 响应头包含限流信息：
  - X-RateLimit-Limit: 50
  - X-RateLimit-Remaining: 0

#### 3.2 熔断测试

```bash
# 模拟后端服务故障
# 1. 停止后端服务
docker stop openidaas-user

# 2. 发送请求
for i in {1..20}; do
  curl -w "\nHTTP Status: %{http_code}\n" \
    http://localhost:8080/api/users &
done
wait

# 3. 检查熔断状态
curl http://localhost:8080/api/gateway/metrics
```

#### 预期结果

- 前10次请求尝试调用后端
- 第11次请求触发熔断，返回降级响应
- 熔断器状态：CLOSED → OPEN → HALF_OPEN → CLOSED
- 熔断时间：30秒

### 4. 认证验证性能测试

#### 测试目标

- Token验证 < 10ms
- 不影响网关转发性能

#### 测试脚本

```python
import requests
import time

token = "your-jwt-token"
url = "http://localhost:8080/api/users"

latencies = []
for i in range(1000):
    start = time.time()
    response = requests.get(
        url,
        headers={"Authorization": f"Bearer {token}"}
    )
    end = time.time()
    
    latencies.append((end - start) * 1000)  # 转换为毫秒

    if i % 100 == 0:
        print(f"Completed {i} requests")

# 统计结果
latencies.sort()
p50 = latencies[len(latencies) // 2]
p95 = latencies[int(len(latencies) * 0.95)]
p99 = latencies[int(len(latencies) * 0.99)]
avg = sum(latencies) / len(latencies)

print(f"P50: {p50:.2f}ms")
print(f"P95: {p95:.2f}ms")
print(f"P99: {p99:.2f}ms")
print(f"Avg: {avg:.2f}ms")
```

#### 预期结果

- P50 < 5ms
- P95 < 8ms
- P99 < 10ms
- Avg < 5ms

## 性能优化建议

### JVM优化

```bash
# 启动参数
java -Xms2g -Xmx4g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+DisableExplicitGC \
     -jar openidaas-gateway.jar
```

### Netty优化

```yaml
spring:
  cloud:
    gateway:
      httpserver:
        # 连接配置
        max-initial-line-length: 65536
        max-header-size: 65536
        max-chunk-size: 8192
```

### 连接池优化

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        # 连接池配置
        connect-timeout: 1000
        response-timeout: 5s
        pool:
          type: elastic
          max-connections: 500
          max-idle-time: 20s
          max-life-time: 60s
          acquire-timeout: 30000
```

## 性能监控

### 关键指标

```yaml
# 网关QPS
gateway_requests_total

# 网关响应时间
gateway_response_time_seconds{quantile="0.5"}
gateway_response_time_seconds{quantile="0.95"}
gateway_response_time_seconds{quantile="0.99"}

# 网关错误数
gateway_errors_total{status="5xx"}

# 熔断器状态
resilience4j_circuitbreaker_state{name="user-service"}
```

### Prometheus查询

```promql
# 当前QPS
rate(gateway_requests_total[1m])

# P95延迟
histogram_quantile(0.95, rate(gateway_response_time_seconds_bucket[5m]))

# 错误率
rate(gateway_errors_total{status="5xx"}[5m]) / rate(gateway_requests_total[5m])

# 熔断器开启
resilience4j_circuitbreaker_state{name="user-service"} == 1
```

## 性能报告模板

```markdown
# 网关性能测试报告

## 测试环境
- 测试时间：2024-01-01
- 硬件配置：8核CPU, 16GB RAM
- 网络环境：1Gbps
- 测试工具：wrk, JMeter

## 测试结果

### 转发性能
- 最大QPS：XXXXX
- P50延迟：XX ms
- P95延迟：XX ms
- P99延迟：XX ms
- 错误率：XX%

### 路由热更新
- 配置更新耗时：XX ms
- 请求影响：无
- 切换成功率：100%

### 限流熔断
- 限流准确率：100%
- 熔断响应时间：< 10ms
- 自动恢复时间：30s

### 认证性能
- Token验证时间：X ms
- 不影响转发性能

## 结论
✅ 达到预期性能目标 / ❌ 未达到预期

## 优化建议
（列出优化建议）
```

## 故障排查

### 性能问题

#### QPS达不到预期
- 检查JVM内存
- 检查连接池配置
- 检查后端服务性能
- 检查网络带宽

#### 延迟过高
- 检查GC日志
- 检查网络延迟
- 检查后端服务
- 优化过滤器链

#### 错误率过高
- 检查熔断器状态
- 检查后端服务健康
- 检查限流配置
- 检查日志错误

## 参考资料

- [Spring Cloud Gateway官方文档](https://spring.io/projects/spring-cloud-gateway)
- [Resilience4j文档](https://resilience4j.readme.io/)
- [wrk文档](https://github.com/wg/wrk/blob/master/SCRIPTING)
