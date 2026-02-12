# 数据库安全配置指南

## 目录
1. [环境变量配置](#环境变量配置)
2. [HikariCP 连接池优化](#hikaricp-连接池优化)
3. [部署环境配置](#部署环境配置)
4. [安全最佳实践](#安全最佳实践)
5. [密钥管理](#密钥管理)

---

## 环境变量配置

### 为什么使用环境变量

- **避免敏感信息泄露**: 防止数据库密码等敏感信息被提交到代码仓库
- **环境隔离**: 不同环境（开发、测试、生产）使用不同配置
- **配置灵活性**: 无需修改代码即可更改配置
- **安全性**: 符合 OWASP 安全要求

### 环境变量列表

#### 数据库配置

| 环境变量 | 说明 | 默认值 | 生产环境要求 |
|---------|------|--------|------------|
| `DB_URL` | 数据库连接 URL | `jdbc:mysql://localhost:3306/open_idaas` | **必填** |
| `DB_USERNAME` | 数据库用户名 | `root` | **必填** |
| `DB_PASSWORD` | 数据库密码 | `root123` | **必填**，建议强密码 |

#### HikariCP 连接池配置

| 环境变量 | 说明 | 开发环境 | 生产环境 |
|---------|------|---------|---------|
| `HIKARI_MAX_POOL_SIZE` | 最大连接数 | 20 | CPU核心数×2+磁盘数 |
| `HIKARI_MIN_IDLE` | 最小空闲连接 | 5 | 10-20 |
| `HIKARI_CONNECTION_TIMEOUT` | 连接超时(ms) | 30000 | 15000 |
| `HIKARI_IDLE_TIMEOUT` | 空闲超时(ms) | 600000 | 600000 |
| `HIKARI_MAX_LIFETIME` | 最大生命周期(ms) | 1800000 | 1800000 |
| `HIKARI_VALIDATION_TIMEOUT` | 验证超时(ms) | 5000 | 3000 |
| `HIKARI_LEAK_DETECTION_THRESHOLD` | 泄漏检测(ms) | 60000 | 30000 |
| `HIKARI_POOL_NAME` | 连接池名称 | `OpenIDaSHikariCP` | 自定义 |
| `HIKARI_TRANSACTION_ISOLATION` | 事务隔离级别 | `READ_COMMITTED` | `READ_COMMITTED` |

#### Redis 配置

| 环境变量 | 说明 | 默认值 | 生产环境要求 |
|---------|------|--------|------------|
| `REDIS_HOST` | Redis 主机 | `localhost` | **必填** |
| `REDIS_PORT` | Redis 端口 | `6379` | `6379` |
| `REDIS_PASSWORD` | Redis 密码 | `(空)` | **必填** |
| `REDIS_DB` | Redis 数据库索引 | `0` | `0` |
| `REDIS_POOL_MAX_ACTIVE` | 最大连接数 | 20 | 50 |
| `REDIS_POOL_MAX_IDLE` | 最大空闲连接 | 10 | 20 |

#### JWT 配置

| 环境变量 | 说明 | 默认值 | 生产环境要求 |
|---------|------|--------|------------|
| `JWT_SECRET` | JWT 密钥 | `mySecretKey...` | **必填**，至少 32 字符 |
| `JWT_EXPIRATION` | Token 有效期(秒) | `3600` | `3600` |
| `JWT_REFRESH_EXPIRATION` | 刷新 Token 有效期(秒) | `86400` | `86400` |
| `JWT_ISSUER` | 签发者 | `OpenIDaaS` | 应用域名 |

---

## HikariCP 连接池优化

### HikariCP 简介

HikariCP 是目前性能最好的 JDBC 连接池，具有以下特点：
- 极快的启动和连接获取速度
- 最小的代码体积
- 智能的连接管理
- 完善的监控和诊断功能

### 优化配置详解

#### 1. 最大连接池大小 (maximum-pool-size)

**计算公式**:
```
最大连接数 = ((核心数 * 2) + 有效磁盘数)
```

**推荐值**:
- **4核CPU, 1块磁盘**: `(4 * 2) + 1 = 9` → `10`
- **8核CPU, 2块磁盘**: `(8 * 2) + 2 = 18` → `20`
- **16核CPU, 4块磁盘**: `(16 * 2) + 4 = 36` → `40`

**注意**: 过多的连接不会提升性能，反而增加数据库压力。

#### 2. 最小空闲连接 (minimum-idle)

- **开发环境**: `5`
- **生产环境**: `10-20`
- **高并发场景**: `20-30`

#### 3. 连接超时 (connection-timeout)

- **开发环境**: `30000ms` (30秒)
- **生产环境**: `15000ms` (15秒)

#### 4. 空闲超时 (idle-timeout)

- 默认值: `600000ms` (10分钟)
- 建议保持默认，过短会导致频繁创建连接

#### 5. 最大生命周期 (max-lifetime)

- 默认值: `1800000ms` (30分钟)
- 建议保持默认，确保连接不会过期

#### 6. 泄漏检测 (leak-detection-threshold)

- **开发环境**: `60000ms` (1分钟)
- **生产环境**: `30000ms` (30秒)

**作用**: 检测应用程序未正确关闭的连接，记录警告日志。

#### 7. MySQL 性能优化参数

```yaml
data-source-properties:
  # 预编译语句缓存
  cachePrepStmts: true
  prepStmtCacheSize: 250
  prepStmtCacheSqlLimit: 2048

  # 使用服务端预编译
  useServerPrepStmts: true

  # 本地会话状态
  useLocalSessionState: true

  # 批量语句重写
  rewriteBatchedStatements: true

  # 结果集元数据缓存
  cacheResultSetMetadata: true

  # 服务器配置缓存
  cacheServerConfiguration: true

  # 禁用自动提交
  elideSetAutoCommits: true

  # 关闭时间统计（提升性能）
  maintainTimeStats: false
```

---

## 部署环境配置

### 1. 本地开发

使用 `.env` 文件:

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件，填写实际配置
vim .env

# 使用 direnv 或 source 加载
source .env
```

### 2. Docker Compose

使用 `docker-compose.env` 文件:

```bash
# 复制环境变量模板
cp docker-compose.env.example docker-compose.env

# 编辑配置
vim docker-compose.env

# 启动服务
docker-compose --env-file docker-compose.env up -d
```

### 3. Kubernetes

#### 3.1 创建 Secret

**方式1: 从环境文件创建**

```bash
# 创建 .env 文件
cat > openidaas-env <<EOF
DB_PASSWORD=your_secure_password
JWT_SECRET=your_jwt_secret_key
REDIS_PASSWORD=your_redis_password
EOF

# 从文件创建 Secret
kubectl create secret generic openidaas-secrets \
  --from-env-file=openidaas-env \
  --namespace=openidaas \
  --dry-run=client -o yaml > k8s/secret.yaml

kubectl apply -f k8s/secret.yaml
```

**方式2: 手动创建**

编辑 `k8s/secret.yaml`:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: openidaas-secrets
  namespace: openidaas
type: Opaque
stringData:
  DB_PASSWORD: "your_secure_password"
  JWT_SECRET: "your_jwt_secret_key"
  REDIS_PASSWORD: "your_redis_password"
```

#### 3.2 在 Deployment 中使用 Secret

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
spec:
  template:
    spec:
      containers:
      - name: user-service
        env:
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: openidaas-secrets
              key: DB_PASSWORD
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: openidaas-secrets
              key: JWT_SECRET
```

### 4. 云平台 (云原生构建)

使用云平台的环境变量管理功能：

- **腾讯云**: 环境变量配置项
- **阿里云**: 配置项管理
- **AWS**: Systems Manager Parameter Store
- **Azure**: Key Vault

---

## 安全最佳实践

### 1. 密码安全

#### 数据库密码

✅ **推荐**:
- 至少 16 字符
- 包含大小写字母、数字、特殊字符
- 使用密码管理器生成
- 定期更换（每3-6个月）

❌ **禁止**:
- 使用默认密码 (`root123`, `password`)
- 使用常见密码 (`123456`, `qwerty`)
- 在代码中硬编码
- 提交到版本控制

示例生成密码:
```bash
# 使用 openssl
openssl rand -base64 32

# 使用 pwgen
pwgen -s 32 1

# 使用 Python
python3 -c "import secrets; print(secrets.token_urlsafe(32))"
```

#### JWT 密钥

✅ **推荐**:
- 至少 32 字符（建议 64+）
- 使用高熵随机字符串
- 与数据库密码不同

生成示例:
```bash
# 生成 64 字符随机密钥
python3 -c "import secrets; print(secrets.token_urlsafe(64))"
```

### 2. 网络安全

#### SSL/TLS 加密

生产环境必须启用 SSL:

```yaml
# 启用 SSL 连接
DB_URL: jdbc:mysql://db-host:3306/open_idaas?useSSL=true&requireSSL=true
```

#### 防火墙规则

- 仅允许应用服务器访问数据库
- 使用白名单 IP 地址
- 限制数据库端口暴露

### 3. 访问控制

#### 最小权限原则

- 创建专用数据库用户，不使用 root
- 仅授予必要的权限

```sql
-- 创建应用专用用户
CREATE USER 'openidaas_app'@'%' IDENTIFIED BY 'secure_password';

-- 仅授予必要权限
GRANT SELECT, INSERT, UPDATE, DELETE ON open_idaas.* TO 'openidaas_app'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON open_idaas_audit.* TO 'openidaas_app'@'%';
FLUSH PRIVILEGES;
```

#### 数据库分离

- 生产环境使用独立数据库服务器
- 读写分离
- 分库分表（大规模场景）

### 4. 审计日志

- 启用数据库审计日志
- 记录敏感操作
- 定期审查访问日志

---

## 密钥管理

### 1. 本地开发

使用 `.env` 文件 + `.gitignore`:

```gitignore
# .gitignore
.env
.env.local
.env.*.local
docker-compose.env
```

### 2. 密钥管理工具

#### HashiCorp Vault

```bash
# 安装 Vault
vault server -dev

# 存储密钥
vault kv put secret/openidaas db_password="your_password" jwt_secret="your_secret"

# 读取密钥
vault kv get secret/openidaas
```

#### AWS Secrets Manager

```bash
# 存储密钥
aws secretsmanager create-secret \
  --name openidaas/db-password \
  --secret-string "your_password"

# 获取密钥
aws secretsmanager get-secret-value \
  --secret-id openidaas/db-password
```

#### Azure Key Vault

```bash
# 存储密钥
az keyvault secret set \
  --vault-name openidaas-vault \
  --name db-password \
  --value "your_password"

# 获取密钥
az keyvault secret show \
  --vault-name openidaas-vault \
  --name db-password
```

### 3. 密钥轮换

- **数据库密码**: 每3-6个月
- **JWT 密钥**: 每6-12个月（需考虑 Token 有效期）
- **API 密钥**: 每年

### 4. 密钥备份

- 安全存储备份密钥
- 使用加密存储
- 多地点备份
- 限制访问权限

---

## 配置示例

### 开发环境配置

```yaml
# application-dev.yml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 3
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000

logging:
  level:
    com.zaxxer.hikari: DEBUG
```

### 生产环境配置

```yaml
# application-prod.yml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 50
      minimum-idle: 20
      connection-timeout: 15000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 30000
      connection-test-query: SELECT 1

logging:
  level:
    com.zaxxer.hikari: WARN
```

---

## 监控和告警

### HikariCP 监控端点

```bash
# Actuator 端点
curl http://localhost:8081/actuator/metrics/hikaricp.connections.active
curl http://localhost:8081/actuator/metrics/hikaricp.connections.idle
curl http://localhost:8081/actuator/metrics/hikaricp.connections.pending
```

### Prometheus 指标

- `hikaricp_connections_active`: 活跃连接数
- `hikaricp_connections_idle`: 空闲连接数
- `hikaricp_connections_pending`: 等待连接数
- `hikaricp_connections_max`: 最大连接数
- `hikaricp_connections_min`: 最小连接数

### 告警规则示例

```yaml
# Prometheus 告警规则
groups:
- name: hikaricp
  rules:
  - alert: HikariCPConnectionsNearExhaustion
    expr: (hikaricp_connections_active / hikaricp_connections_max) > 0.9
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "连接池即将耗尽"

  - alert: HikariCPConnectionLeakDetected
    expr: increase(hikaricp_connections_pending[5m]) > 10
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "检测到连接泄漏"
```

---

## 故障排查

### 问题1: 连接池耗尽

**症状**: 应用无法获取数据库连接

**排查**:
```bash
# 检查活跃连接数
curl http://localhost:8081/actuator/metrics/hikaricp.connections.active

# 检查等待连接数
curl http://localhost:8081/actuator/metrics/hikaricp.connections.pending
```

**解决方案**:
- 增加 `maximum-pool-size`
- 减少 `connection-timeout`
- 检查是否有连接泄漏

### 问题2: 连接泄漏

**症状**: 活跃连接数持续增长

**排查**:
```bash
# 检查 HikariCP 日志
tail -f /var/log/openidaas/application.log | grep HikariPool

# 查找未关闭的连接
grep "Connection leak detection" /var/log/openidaas/application.log
```

**解决方案**:
- 启用 `leak-detection-threshold`
- 修复代码中未关闭的连接
- 使用 try-with-resources

---

## 总结

### 关键要点

1. ✅ **使用环境变量**存储敏感信息
2. ✅ **合理配置 HikariCP**连接池大小
3. ✅ **启用连接泄漏检测**
4. ✅ **生产环境必须使用强密码**
5. ✅ **定期轮换密钥**
6. ✅ **启用 SSL/TLS 加密**
7. ✅ **监控连接池状态**

### 检查清单

部署前检查:
- [ ] 所有敏感信息使用环境变量
- [ ] `.env` 文件已添加到 `.gitignore`
- [ ] 生产环境使用强密码
- [ ] HikariCP 连接池大小已优化
- [ ] 启用连接泄漏检测
- [ ] MySQL 连接参数已优化
- [ ] Redis 密码已配置
- [ ] JWT 密钥已更新
- [ ] SSL/TLS 已启用（生产环境）
- [ ] 监控和告警已配置
