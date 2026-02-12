# 多环境配置指南

## 概述

OpenIDaaS 项目已为所有微服务配置了完整的多环境支持，包括：
- **dev** (开发环境)
- **test** (测试环境)
- **prod** (生产环境)

## 服务配置清单

| 服务 | 端口 (dev/test/prod) | 配置文件 |
|------|---------------------|---------|
| gateway | 8080 | ✅ dev/test/prod |
| user-service | 8081 | ✅ dev/test/prod |
| auth-service | 8082 | ✅ dev/test/prod |
| role-service | 8083 | ✅ dev/test/prod |
| organization-service | 8084 | ✅ dev/test/prod |
| audit-service | 8085 | ✅ dev/test/prod |
| application-service | 8086 | ✅ dev/test/prod |
| authorization-service | 8087 | ✅ dev/test/prod |

## 环境配置对比

### 1. 数据源配置

| 配置项 | dev | test | prod |
|--------|-----|------|------|
| **数据库** | open_idaas | open_idaas_test | open_idaas |
| **默认用户名** | root | test_user | ${DB_USERNAME} |
| **默认密码** | root | test_password | ${DB_PASSWORD} |
| **SSL** | false | false | **true** |
| **连接池大小** | 5-20 | 10-30 | 20-100 |
| **连接测试查询** | - | - | SELECT 1 |

### 2. Redis 配置

| 配置项 | dev | test | prod |
|--------|-----|------|------|
| **数据库索引** | 0 | 1 | 0 |
| **连接池大小** | 8 | 10 | 20 |
| **超时时间** | 3000ms | 3000ms | 5000ms |
| **SSL** | false | false | false (可选) |

### 3. 日志配置

| 配置项 | dev | test | prod |
|--------|-----|------|------|
| **Root 级别** | INFO | INFO | WARN |
| **应用级别** | DEBUG | DEBUG | INFO |
| **MyBatis-Plus** | DEBUG | INFO | WARN |
| **文件日志** | - | logs/ | /var/log/openidaas/ |
| **文件滚动** | - | - | 100MB, 30天, 1GB |

### 4. Actuator 配置

| 配置项 | dev | test | prod |
|--------|-----|------|------|
| **端点暴露** | health,info,metrics,prometheus,env,configprops | health,info,metrics,prometheus | health,info,metrics,prometheus |
| **健康详情** | always | always | never |
| **环境标签** | dev | test | prod, region |

### 5. Gateway 限流配置

| 配置项 | dev | test | prod |
|--------|-----|------|------|
| **补充速率** | 100 | 50 | 200 |
| **突发容量** | 200 | 100 | 400 |

### 6. Kafka 配置 (audit-service)

| 配置项 | dev | test | prod |
|--------|-----|------|------|
| **重试次数** | 3 | 3 | 5 |
| **确认模式** | acks=1 | acks=1 | acks=all |
| **生产者幂等** | false | false | **true** |
| **批量大小** | 默认 | 默认 | 16384 |
| **延迟发送** | 默认 | 默认 | 5ms |

## 环境切换方式

### 1. 通过 Spring Profile 参数

```bash
# 开发环境
java -jar app.jar --spring.profiles.active=dev

# 测试环境
java -jar app.jar --spring.profiles.active=test

# 生产环境
java -jar app.jar --spring.profiles.active=prod
```

### 2. 通过环境变量

```bash
# 设置环境变量
export SPRING_PROFILES_ACTIVE=prod
java -jar app.jar

# 或者在 Docker 中
docker run -e SPRING_PROFILES_ACTIVE=prod app-image
```

### 3. 通过 application.yml 默认配置

每个服务的 `application.yml` 已设置默认的 profile：
```yaml
spring:
  profiles:
    active: dev  # 默认开发环境，部署时请修改
```

## 环境变量配置清单

### 开发环境 (dev)

| 环境变量 | 默认值 | 说明 |
|---------|--------|------|
| DB_HOST | localhost | 数据库主机 |
| DB_PORT | 3306 | 数据库端口 |
| DB_NAME | open_idaas | 数据库名称 |
| DB_USERNAME | root | 数据库用户名 |
| DB_PASSWORD | root | 数据库密码 |
| REDIS_HOST | localhost | Redis 主机 |
| REDIS_PORT | 6379 | Redis 端口 |
| REDIS_PASSWORD | (空) | Redis 密码 |
| REDIS_DB | 0 | Redis 数据库索引 |
| NACOS_SERVER_ADDR | localhost:8848 | Nacos 地址 |
| JWT_SECRET | dev-secret-key-... | JWT 密钥 |

### 测试环境 (test)

| 环境变量 | 默认值 | 说明 |
|---------|--------|------|
| DB_HOST | localhost | 数据库主机 |
| DB_PORT | 3306 | 数据库端口 |
| DB_NAME | open_idaas_test | 数据库名称 |
| DB_USERNAME | test_user | 数据库用户名 |
| DB_PASSWORD | test_password | 数据库密码 |
| REDIS_DB | 1 | Redis 数据库索引 (使用独立数据库) |

### 生产环境 (prod) - 必须配置

| 环境变量 | 是否必填 | 说明 |
|---------|---------|------|
| DB_HOST | ✅ 必填 | 数据库主机 |
| DB_PASSWORD | ✅ 必填 | 数据库密码 |
| REDIS_HOST | ✅ 必填 | Redis 主机 |
| REDIS_PASSWORD | ✅ 必填 | Redis 密码 |
| JWT_SECRET | ✅ 必填 | JWT 密钥 (至少 32 字符) |
| KAFKA_SERVERS | ✅ 必填 | Kafka 地址 (audit-service) |
| CORS_ALLOWED_ORIGINS | ⚠️ 建议 | 允许的跨域来源 |
| REGION | ⚠️ 建议 | 区域标签 |

## 特殊配置说明

### 1. 密码策略 (user-service)

| 环境 | 特殊字符要求 | 历史密码数量 | 登录失败限制 |
|-----|------------|------------|------------|
| dev | false (降低) | 5 | 10次, 5分钟 |
| test | true | 12 | 5次, 30分钟 |
| prod | true | 12 | 5次, 30分钟 |

### 2. Token 有效期 (auth-service)

| 环境 | Access Token | Refresh Token |
|-----|-------------|---------------|
| dev | 2小时 (7200s) | 7天 |
| test | 1小时 (3600s) | 30天 |
| prod | 1小时 (3600s) | 30天 |

### 3. MFA 配置 (auth-service)

| 环境 | TOTP Issuer | 短信过期 | 邮箱过期 |
|-----|------------|---------|---------|
| dev | IDaaS-Dev | 5分钟 | 15分钟 |
| test | IDaaS-Test | 5分钟 | 15分钟 |
| prod | IDaaS | 5分钟 | 15分钟 |

### 4. 审计配置 (audit-service)

| 环境 | 线程池大小 | 批量大小 | 批量间隔 |
|-----|-----------|---------|---------|
| dev | 5 | 100 | 5秒 |
| test | 5 | 100 | 5秒 |
| prod | 10 | 500 | 3秒 |

## Docker 部署示例

### 开发环境 Docker Compose

```yaml
version: '3.8'
services:
  user-service:
    image: openidaas/user-service:latest
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - DB_HOST=mysql
      - REDIS_HOST=redis
    depends_on:
      - mysql
      - redis
```

### 生产环境 Docker Compose

```yaml
version: '3.8'
services:
  user-service:
    image: openidaas/user-service:latest
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=${DB_HOST}
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - REDIS_HOST=${REDIS_HOST}
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
```

### Kubernetes 部署

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: openidaas-config
data:
  SPRING_PROFILES_ACTIVE: "prod"
  DB_HOST: "mysql-service"
  DB_NAME: "open_idaas"
  REDIS_HOST: "redis-service"
---
apiVersion: v1
kind: Secret
metadata:
  name: openidaas-secrets
type: Opaque
data:
  DB_PASSWORD: <base64-encoded>
  REDIS_PASSWORD: <base64-encoded>
  JWT_SECRET: <base64-encoded>
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: user-service
        image: openidaas/user-service:latest
        envFrom:
        - configMapRef:
            name: openidaas-config
        - secretRef:
            name: openidaas-secrets
```

## 安全建议

### 1. 生产环境检查清单

- [ ] 所有敏感信息使用环境变量或密钥管理
- [ ] JWT_SECRET 使用强密码（至少 32 字符）
- [ ] 数据库启用 SSL 连接
- [ ] CORS 配置限制允许的来源
- [ ] Actuator 端点不暴露详细信息
- [ ] 日志级别设置为 WARN/INFO
- [ ] Kafka 生产者启用幂等性
- [ ] 限流配置根据实际流量调整
- [ ] 日志文件独立存储

### 2. 密钥管理

推荐使用以下方案之一：
- **Kubernetes Secrets** (K8s 环境)
- **HashiCorp Vault**
- **AWS Secrets Manager**
- **Azure Key Vault**
- **环境变量文件** (.env) - 仅限开发/测试

### 3. 配置验证

部署前请验证：
```bash
# 检查配置文件语法
cat application-prod.yml

# 验证环境变量
echo $DB_HOST
echo $JWT_SECRET

# 启动前测试
java -jar app.jar --spring.profiles.active=prod --spring-boot.run.arguments=--help
```

## 故障排查

### 1. 数据库连接失败

```bash
# 检查数据库是否可达
ping $DB_HOST
telnet $DB_HOST 3306

# 检查配置
grep datasource application-*.yml
```

### 2. Redis 连接失败

```bash
# 检查 Redis
redis-cli -h $REDIS_HOST -p $REDIS_PORT ping

# 检查配置
grep redis application-*.yml
```

### 3. 环境变量未生效

```bash
# 检查进程环境变量
ps aux | grep java
cat /proc/<pid>/environ | tr '\0' '\n'

# 验证 profile
curl http://localhost:8081/actuator/env
```

## 最佳实践

1. **开发环境**
   - 使用本地数据库和 Redis
   - 开启详细日志
   - 延长 Token 有效期
   - 启用热重载

2. **测试环境**
   - 使用独立数据库
   - 模拟生产配置
   - 进行性能测试
   - 验证所有端点

3. **生产环境**
   - 使用外部数据库集群
   - 启用 SSL 和监控
   - 配置自动扩缩容
   - 定期备份日志

## 相关文档

- [部署指南](../deploy/k8s/README.md)
- [数据库设计](../design/data.md)
- [测试指南](TEST_GUIDE.md)
- [监控配置](../openidaas-audit-service/MONITORING.md)
