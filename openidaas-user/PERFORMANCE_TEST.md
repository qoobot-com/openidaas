# 用户管理模块性能测试方案

## 测试目标

验证 openidaas-user 模块在以下场景下的性能表现：

- **100万用户规模**: 单页查询 < 50ms
- **全文搜索**: 关键词搜索 < 100ms
- **批量导入**: 10万用户 < 5分钟
- **并发访问**: 1000并发 QPS > 1000

## 测试环境

### 硬件配置

- CPU: 8核
- 内存: 16GB
- 磁盘: SSD
- 网络: 1Gbps

### 软件环境

- JDK: 25
- PostgreSQL: 14+
- Redis: 6+
- JMeter: 5.6+

## 测试数据准备

### 生成测试数据

```sql
-- 创建100万测试用户
INSERT INTO users (username, password, email, phone, fullname, status, created_at)
SELECT
    'user_' || i,
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',
    'user' || i || '@example.com',
    '138' || LPAD(i::text, 8, '0'),
    'User ' || i,
    'ACTIVE',
    CURRENT_TIMESTAMP
FROM generate_series(1, 1000000) AS i;
```

### 创建索引

```sql
-- 用户名索引
CREATE INDEX idx_username ON users(username);

-- 邮箱索引
CREATE INDEX idx_email ON users(email);

-- 状态索引
CREATE INDEX idx_status ON users(status);

-- 全文搜索索引
CREATE INDEX idx_fulltext ON users USING gin(to_tsvector('english', fullname || ' ' || email));
```

## 测试场景

### 1. 用户查询性能测试

#### 测试场景

- 分页查询用户列表（每页20条）
- 按ID查询用户详情
- 按用户名/邮箱查询

#### JMeter测试计划

```xml
<ThreadGroup>
  <stringProp name="ThreadGroup.num_threads">100</stringProp>
  <stringProp name="ThreadGroup.ramp_time">10</stringProp>
  <stringProp name="ThreadGroup.duration">300</stringProp>
</ThreadGroup>
```

#### 测试命令

```bash
# 分页查询测试
jmeter -n -t user_query_test.jmx -l query_results.jtl -e -o query_report

# 详情查询测试
jmeter -n -t user_detail_test.jmx -l detail_results.jtl -e -o detail_report
```

#### 预期结果

- **P50**: < 30ms
- **P95**: < 50ms
- **P99**: < 100ms
- **QPS**: > 1000

### 2. 用户搜索性能测试

#### 测试场景

- 全文搜索用户（姓名、邮箱、手机号）
- 多条件组合搜索

#### 测试用例

```bash
# 关键词搜索
curl -X POST http://localhost:8082/api/users/search \
  -H "Content-Type: application/json" \
  -d '{"keyword": "张三", "page": 0, "size": 20}'

# 组合搜索
curl -X POST http://localhost:8082/api/users/search \
  -H "Content-Type: application/json" \
  -d '{"keyword": "user", "status": "ACTIVE", "tenantId": 1}'
```

#### 预期结果

- **P50**: < 50ms
- **P95**: < 100ms
- **P99**: < 200ms
- **QPS**: > 500

### 3. 批量导入性能测试

#### 测试场景

- 批量导入10万用户
- 导入过程性能监控

#### Python测试脚本

```python
import requests
import time
import csv

def bulk_import_users(file_path):
    url = "http://localhost:8082/api/users/import"
    
    with open(file_path, 'rb') as f:
        files = {'file': f}
        start_time = time.time()
        response = requests.post(url, files=files)
        duration = time.time() - start_time
        
        print(f"Import duration: {duration:.2f}s")
        print(f"Response: {response.json()}")

if __name__ == "__main__":
    bulk_import_users("users_100k.xlsx")
```

#### 预期结果

- **总耗时**: < 300秒
- **平均速度**: > 330用户/秒
- **成功率**: 100%

### 4. 并发访问性能测试

#### 测试场景

- 1000并发用户同时访问
- 混合读写操作

#### JMeter配置

```xml
<ThreadGroup>
  <stringProp name="ThreadGroup.num_threads">1000</stringProp>
  <stringProp name="ThreadGroup.ramp_time">60</stringProp>
  <stringProp name="ThreadGroup.duration">600</stringProp>
</ThreadGroup>

<!-- 比例：读70%，写20%，更新10% -->
<hashTree>
  <HTTPSamplerProxy guiclass="HttpTestSampleGui">
    <stringProp name="HTTPSampler.path">/api/users</stringProp>
  </HTTPSamplerProxy>
  <WeightedController>
    <elementProp name="ReadWeight" elementType="WeightedController.weight">
      <stringProp name="WeightedController.weight">70</stringProp>
    </elementProp>
  </WeightedController>
</hashTree>
```

#### 预期结果

- **QPS**: > 1000
- **P50**: < 50ms
- **P95**: < 200ms
- **错误率**: < 0.1%

## 性能优化建议

### 数据库优化

1. **索引优化**
   ```sql
   -- 复合索引
   CREATE INDEX idx_user_search ON users(status, tenant_id, department_id);

   -- 部分索引
   CREATE INDEX idx_active_users ON users(username) WHERE status = 'ACTIVE';
   ```

2. **查询优化**
   - 使用 JOIN 替代子查询
   - 避免 SELECT *
   - 合理使用 LIMIT

3. **连接池配置**
   ```yaml
   spring:
     datasource:
       hikari:
         maximum-pool-size: 50
         minimum-idle: 10
   ```

### 缓存优化

1. **Redis缓存策略**
   - 用户信息缓存：TTL 1小时
   - 权限缓存：TTL 1小时
   - 热点数据预热

2. **缓存更新策略**
   - 写穿透
   - 缓存更新
   - 定时刷新

### 代码优化

1. **批量操作**
   ```java
   @Transactional
   public void batchImport(List<CreateUserRequest> requests) {
       List<User> users = new ArrayList<>();
       for (CreateUserRequest request : requests) {
           users.add(toEntity(request));
       }
       userRepository.saveAll(users);
   }
   ```

2. **异步处理**
   ```java
   @Async("taskExecutor")
   public CompletableFuture<Void> sendWelcomeEmail(User user) {
       // 发送邮件逻辑
   }
   ```

## 性能监控

### Prometheus指标

```yaml
# JVM指标
jvm_memory_used_bytes
jvm_gc_pause_seconds

# 数据库指标
hikaricp_connections_active
hikaricp_connections_idle

# Redis指标
redis_command_duration_seconds
redis_cache_hit_ratio

# 自定义指标
user_query_duration_seconds
user_import_duration_seconds
```

### Grafana仪表盘

推荐监控指标：

- QPS
- 响应时间（P50/P95/P99）
- 错误率
- CPU/内存使用率
- 数据库连接数
- Redis命中率

## 测试报告

### 测试模板

```markdown
# 性能测试报告

## 测试环境
- 测试时间：2024-01-01
- 测试环境：生产环境
- 数据规模：100万用户

## 测试结果

### 用户查询
- P50: XX ms
- P95: XX ms
- P99: XX ms
- QPS: XXXX

### 用户搜索
- P50: XX ms
- P95: XX ms
- P99: XX ms
- QPS: XXXX

### 批量导入
- 总耗时: XX s
- 导入速度: XXX 用户/秒
- 成功率: XX%

### 并发访问
- QPS: XXXX
- P50: XX ms
- P95: XX ms
- 错误率: XX%

## 结论
✅ 通过 / ❌ 不通过

## 建议
（优化建议）
```

## 故障排查

### 常见性能问题

1. **查询慢**
   - 检查索引是否生效
   - 使用 EXPLAIN ANALYZE 分析查询计划
   - 优化SQL语句

2. **连接池耗尽**
   - 增加连接池大小
   - 检查慢查询
   - 优化事务

3. **缓存命中率低**
   - 检查缓存配置
   - 分析缓存访问模式
   - 调整缓存策略

## 参考资料

- [Spring Boot Performance Best Practices](https://spring.io/guides/top-spring-boot-performance-tips/)
- [PostgreSQL Performance Tuning](https://www.postgresql.org/docs/current/performance-tips.html)
- [Redis Performance Best Practices](https://redis.io/docs/manual/patterns/)
