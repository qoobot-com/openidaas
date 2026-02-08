# OpenIDaaS 监控与告警系统验收标准

## 验收概览

| 类别 | 验收项 | 状态 |
|------|--------|------|
| 监控指标收集 | 应用性能指标 | ⬜ 待验收 |
| 监控指标收集 | 业务指标 | ⬜ 待验收 |
| 监控指标收集 | 安全指标 | ⬜ 待验收 |
| 告警规则配置 | 系统告警规则 | ⬜ 待验收 |
| 告警规则配置 | 业务告警规则 | ⬜ 待验收 |
| 告警规则配置 | 安全告警规则 | ⬜ 待验收 |
| Dashboard 面板 | 认证统计面板 | ⬜ 待验收 |
| Dashboard 面板 | 系统性能监控面板 | ⬜ 待验收 |
| Dashboard 面板 | 安全事件监控面板 | ⬜ 待验收 |
| Dashboard 面板 | 租户使用情况面板 | ⬜ 待验收 |
| 告警通知 | 告警通知及时性 | ⬜ 待验收 |
| 告警通知 | 告警通知准确性 | ⬜ 待验收 |
| 系统集成 | Prometheus 集成 | ⬜ 待验收 |
| 系统集成 | Grafana 集成 | ⬜ 待验收 |
| 系统集成 | Alertmanager 集成 | ⬜ 待验收 |

---

## 1. 监控指标收集验收

### 1.1 应用性能指标

#### 验收标准
- [ ] JVM 堆内存使用率指标正常收集
- [ ] CPU 使用率指标正常收集
- [ ] 响应时间 P95/P99 指标正常收集
- [ ] GC 时间统计指标正常收集

#### 验收步骤
```bash
# 1. 启动监控系统
cd deployment/monitoring
docker-compose up -d

# 2. 等待服务启动
sleep 30

# 3. 检查 JVM 内存指标
curl -s http://localhost:9090/api/v1/query?query=jvm_memory_used_bytes | jq

# 4. 检查 CPU 使用率指标
curl -s http://localhost:9090/api/v1/query?query=process_cpu_usage | jq

# 5. 检查响应时间指标
curl -s http://localhost:9090/api/v1/query?query=http_server_requests_seconds | jq

# 6. 检查 GC 指标
curl -s http://localhost:9090/api/v1/query?query=jvm_gc_pause_seconds_sum | jq
```

#### 验收结果
- JVM 堆内存使用率：通过 / 失败
- CPU 使用率：通过 / 失败
- 响应时间 P95/P99：通过 / 失败
- GC 时间统计：通过 / 失败

---

### 1.2 业务指标

#### 验收标准
- [ ] 认证成功率指标正常收集
- [ ] Token 刷新频率指标正常收集
- [ ] 用户登录统计指标正常收集
- [ ] 租户活跃度指标正常收集

#### 验收步骤
```bash
# 1. 检查认证指标
curl -s http://localhost:9090/api/v1/query?query=auth_total | jq

# 2. 检查 Token 刷新指标
curl -s http://localhost:9090/api/v1/query?query=token_refresh_total | jq

# 3. 检查活跃会话指标
curl -s http://localhost:9090/api/v1/query?query=auth_active_sessions | jq

# 4. 检查租户活跃用户指标
curl -s http://localhost:9090/api/v1/query?query=tenant_active_users | jq
```

#### 验收结果
- 认证成功率：通过 / 失败
- Token 刷新频率：通过 / 失败
- 用户登录统计：通过 / 失败
- 租户活跃度：通过 / 失败

---

### 1.3 安全指标

#### 验收标准
- [ ] 登录失败率指标正常收集
- [ ] 异常访问统计指标正常收集
- [ ] Token 泄露检测指标正常收集
- [ ] 权限违规统计指标正常收集

#### 验收步骤
```bash
# 1. 检查认证失败指标
curl -s http://localhost:9090/api/v1/query?query=auth_failure_total | jq

# 2. 检查异常访问指标
curl -s http://localhost:9090/api/v1/query?query=anomalous_access_total | jq

# 3. 检查 Token 泄露指标
curl -s http://localhost:9090/api/v1/query?query=token_leak_detected_total | jq

# 4. 检查权限拒绝指标
curl -s http://localhost:9090/api/v1/query?query=authorization_denied_total | jq
```

#### 验收结果
- 登录失败率：通过 / 失败
- 异常访问统计：通过 / 失败
- Token 泄露检测：通过 / 失败
- 权限违规统计：通过 / 失败

---

## 2. 告警规则配置验收

### 2.1 系统告警规则

#### 验收标准
- [ ] CPU 使用率 > 80% 告警触发
- [ ] 内存使用率 > 80% 告警触发
- [ ] 响应时间 P95 > 2s 告警触发
- [ ] GC 频率 > 1 次/秒 告警触发

#### 验收步骤
```bash
# 1. 查看告警规则
curl -s http://localhost:9090/api/v1/rules | jq

# 2. 查看当前告警
curl -s http://localhost:9090/api/v1/alerts | jq

# 3. 访问 Prometheus UI 验证
# http://localhost:9090/alerts
```

#### 验收结果
- CPU 使用率告警：通过 / 失败
- 内存使用率告警：通过 / 失败
- 响应时间告警：通过 / 失败
- GC 频率告警：通过 / 失败

---

### 2.2 业务告警规则

#### 验收标准
- [ ] 认证失败率 > 5% 告警触发
- [ ] Token 刷新失败 > 10 次/分钟 告警触发
- [ ] 数据库连接池使用率 > 80% 告警触发

#### 验收步骤
```bash
# 1. 检查认证失败率告警规则
curl -s http://localhost:9090/api/v1/query?query=ALERTS{alertname="HighAuthFailureRate"} | jq

# 2. 检查 Token 刷新告警规则
curl -s http://localhost:9090/api/v1/query?query=ALERTS{alertname="HighTokenRefreshFailureRate"} | jq

# 3. 检查数据库连接池告警规则
curl -s http://localhost:9090/api/v1/query?query=ALERTS{alertname="HighDBConnectionUsage"} | jq
```

#### 验收结果
- 认证失败率告警：通过 / 失败
- Token 刷新告警：通过 / 失败
- 数据库连接池告警：通过 / 失败

---

### 2.3 安全告警规则

#### 验收标准
- [ ] 登录失败 > 10 次/分钟 告警触发
- [ ] 异常访问检测告警触发
- [ ] 权限违规 > 10 次/分钟 告警触发
- [ ] Token 泄露检测告警触发

#### 验收步骤
```bash
# 1. 检查登录失败告警规则
curl -s http://localhost:9090/api/v1/query?query=ALERTS{alertname="HighLoginFailureRate"} | jq

# 2. 检查异常访问告警规则
curl -s http://localhost:9090/api/v1/query?query=ALERTS{alertname="AnomalousAccessDetected"} | jq

# 3. 检查权限违规告警规则
curl -s http://localhost:9090/api/v1/query?query=ALERTS{alertname="AuthorizationViolation"} | jq

# 4. 检查 Token 泄露告警规则
curl -s http://localhost:9090/api/v1/query?query=ALERTS{alertname="TokenLeakDetected"} | jq
```

#### 验收结果
- 登录失败告警：通过 / 失败
- 异常访问告警：通过 / 失败
- 权限违规告警：通过 / 失败
- Token 泄露告警：通过 / 失败

---

## 3. Dashboard 面板验收

### 3.1 认证统计面板

#### 验收标准
- [ ] 认证成功率仪表盘显示正常
- [ ] 认证 QPS 统计显示正常
- [ ] 认证失败率仪表盘显示正常
- [ ] 认证请求趋势图显示正常
- [ ] Token 刷新统计显示正常

#### 验收步骤
1. 访问 http://localhost:3000
2. 登录（admin/admin123）
3. 打开 "OpenIDaaS 监控面板"
4. 验证认证统计面板数据

#### 验收结果
- 认证成功率仪表盘：通过 / 失败
- 认证 QPS 统计：通过 / 失败
- 认证失败率仪表盘：通过 / 失败
- 认证请求趋势图：通过 / 失败
- Token 刷新统计：通过 / 失败

---

### 3.2 系统性能监控面板

#### 验收标准
- [ ] JVM 堆内存使用率显示正常
- [ ] CPU 使用率显示正常
- [ ] 响应时间 P95/P99 显示正常
- [ ] JVM 堆内存趋势图显示正常
- [ ] GC 时间统计显示正常

#### 验收步骤
1. 访问 http://localhost:3000
2. 登录（admin/admin123）
3. 打开 "OpenIDaaS 监控面板"
4. 验证系统性能监控面板数据

#### 验收结果
- JVM 堆内存使用率：通过 / 失败
- CPU 使用率：通过 / 失败
- 响应时间 P95/P99：通过 / 失败
- JVM 堆内存趋势图：通过 / 失败
- GC 时间统计：通过 / 失败

---

### 3.3 安全事件监控面板

#### 验收标准
- [ ] 登录失败统计（按原因）显示正常
- [ ] 异常访问统计显示正常
- [ ] 权限违规统计显示正常
- [ ] Token 泄露检测显示正常

#### 验收步骤
1. 访问 http://localhost:3000
2. 登录（admin/admin123）
3. 打开 "OpenIDaaS 监控面板"
4. 验证安全事件监控面板数据

#### 验收结果
- 登录失败统计：通过 / 失败
- 异常访问统计：通过 / 失败
- 权限违规统计：通过 / 失败
- Token 泄露检测：通过 / 失败

---

### 3.4 租户使用情况面板

#### 验收标准
- [ ] 活跃租户数显示正常
- [ ] 租户活跃用户数 Top 10 显示正常

#### 验收步骤
1. 访问 http://localhost:3000
2. 登录（admin/admin123）
3. 打开 "OpenIDaaS 监控面板"
4. 验证租户使用情况面板数据

#### 验收结果
- 活跃租户数：通过 / 失败
- 租户活跃用户数 Top 10：通过 / 失败

---

## 4. 告警通知验收

### 4.1 告警通知及时性

#### 验收标准
- [ ] 严重告警在 1 分钟内发送
- [ ] 警告告警在 5 分钟内发送
- [ ] 安全告警立即发送

#### 验收步骤
1. 配置告警通知（邮件/Slack）
2. 触发告警（例如停止某个服务）
3. 记录告警发送时间
4. 验证通知及时性

#### 验收结果
- 严重告警及时性：通过 / 失败
- 警告告警及时性：通过 / 失败
- 安全告警及时性：通过 / 失败

---

### 4.2 告警通知准确性

#### 验收标准
- [ ] 告警信息准确描述问题
- [ ] 告警标签包含足够上下文
- [ ] 告警通知包含必要的修复建议

#### 验收步骤
1. 触发各种告警
2. 检查告警通知内容
3. 验证告警信息准确性

#### 验收结果
- 告警信息准确性：通过 / 失败
- 告警标签完整性：通过 / 失败
- 告警修复建议：通过 / 失败

---

## 5. 系统集成验收

### 5.1 Prometheus 集成

#### 验收标准
- [ ] Prometheus 成功抓取所有目标
- [ ] Prometheus 正确存储指标数据
- [ ] Prometheus 告警规则正确执行

#### 验收步骤
```bash
# 1. 检查抓取目标状态
curl -s http://localhost:9090/api/v1/targets | jq

# 2. 检查指标数据存储
curl -s http://localhost:9090/api/v1/query?query=up | jq

# 3. 检查告警规则
curl -s http://localhost:9090/api/v1/rules | jq
```

#### 验收结果
- 抓取目标状态：通过 / 失败
- 指标数据存储：通过 / 失败
- 告警规则执行：通过 / 失败

---

### 5.2 Grafana 集成

#### 验收标准
- [ ] Grafana 成功连接 Prometheus
- [ ] Dashboard 自动加载
- [ ] Dashboard 数据显示正确

#### 验收步骤
1. 访问 http://localhost:3000
2. 验证数据源配置
3. 验证 Dashboard 加载
4. 验证数据显示

#### 验收结果
- Prometheus 连接：通过 / 失败
- Dashboard 自动加载：通过 / 失败
- Dashboard 数据显示：通过 / 失败

---

### 5.3 Alertmanager 集成

#### 验收标准
- [ ] Alertmanager 成功接收告警
- [ ] Alertmanager 正确路由告警
- [ ] Alertmanager 成功发送通知

#### 验收步骤
```bash
# 1. 检查 Alertmanager 状态
curl -s http://localhost:9093/api/v2/status | jq

# 2. 检查告警路由
curl -s http://localhost:9093/api/v2/alerts | jq

# 3. 验证通知发送
# 查看邮件/Slack 等通知渠道
```

#### 验收结果
- 告警接收：通过 / 失败
- 告警路由：通过 / 失败
- 通知发送：通过 / 失败

---

## 6. 性能测试

### 6.1 Prometheus 性能

#### 验收标准
- [ ] 指标抓取延迟 < 5 秒
- [ ] 查询响应时间 < 1 秒
- [ ] 存储空间增长合理

#### 验收步骤
```bash
# 1. 检查抓取延迟
curl -s http://localhost:9090/api/v1/targets | jq '.data.activeTargets[].scrapeDurationSeconds'

# 2. 执行查询测试
time curl -s http://localhost:9090/api/v1/query?query=auth_total | jq

# 3. 检查存储空间
docker exec openidaas-prometheus du -sh /prometheus
```

#### 验收结果
- 抓取延迟：通过 / 失败
- 查询响应时间：通过 / 失败
- 存储空间增长：通过 / 失败

---

### 6.2 Grafana 性能

#### 验收标准
- [ ] Dashboard 加载时间 < 3 秒
- [ ] 查询响应时间 < 2 秒
- [ ] 刷新流畅无卡顿

#### 验收步骤
1. 访问 http://localhost:3000
2. 打开 OpenIDaaS 监控面板
3. 测量加载时间
4. 测试查询性能
5. 测试自动刷新

#### 验收结果
- Dashboard 加载时间：通过 / 失败
- 查询响应时间：通过 / 失败
- 自动刷新流畅性：通过 / 失败

---

## 7. 安全测试

### 7.1 访问控制

#### 验收标准
- [ ] Grafana 默认密码已修改
- [ ] Prometheus 和 Alertmanager 不暴露公网
- [ ] 敏感信息不包含在配置文件中

#### 验收步骤
```bash
# 1. 检查 Grafana 密码
docker logs openidaas-grafana | grep admin

# 2. 检查服务端口
netstat -tuln | grep -E '9090|9093|3000'

# 3. 检查配置文件敏感信息
grep -r "password" deployment/monitoring/*.yml
```

#### 验收结果
- Grafana 密码安全：通过 / 失败
- 服务端口安全：通过 / 失败
- 敏感信息保护：通过 / 失败

---

## 8. 文档完整性

#### 验收标准
- [ ] README 文档完整
- [ ] 配置说明清晰
- [ ] 故障排查文档完善
- [ ] 验收标准文档完整

#### 验收结果
- README 文档：通过 / 失败
- 配置说明：通过 / 失败
- 故障排查文档：通过 / 失败
- 验收标准文档：通过 / 失败

---

## 验收总结

### 通过项统计
- 总验收项：30
- 通过项：0
- 失败项：0
- 待验收项：30

### 关键指标
- 监控指标覆盖率：0/10
- 告警规则覆盖率：0/10
- Dashboard 完整性：0/10
- 告警通知及时性：0/3
- 系统集成完整性：0/3

### 待解决问题
- 无

### 下一步行动
1. 完成所有验收项测试
2. 修复发现的问题
3. 补充文档
4. 准备上线

---

## 验收签名

| 角色 | 姓名 | 签名 | 日期 |
|------|------|------|------|
| 开发负责人 |  |  |  |
| 测试负责人 |  |  |  |
| 运维负责人 |  |  |  |
| 产品负责人 |  |  |  |

---

## 备注

请按照验收标准逐项进行验收，并在验收结果中标记通过或失败。
