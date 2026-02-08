# OpenIDaaS Deployment Guide

## 📋 目录

- [Docker 部署](#docker-部署)
- [Docker Compose 部署](#docker-compose-部署)
- [Kubernetes 部署](#kubernetes-部署)
- [安全加固](#安全加固)
- [监控和日志](#监控和日志)
- [备份和恢复](#备份和恢复)

---

## 🐳 Docker 部署

### 基础 Dockerfile

Dockerfile 使用多阶段构建，优化镜像大小和安全性：

```dockerfile
# 多阶段构建
FROM eclipse-temurin:25-alpine AS builder
# 构建阶段...

FROM eclipse-temurin:25-jre-alpine
# 运行阶段...
```

### 特性

- ✅ 非root用户运行 (UID 1000)
- ✅ 使用 tinit 作为 init 系统
- ✅ 健康检查
- ✅ JVM 优化参数
- ✅ 层级优化

### 构建镜像

```bash
# 构建基础镜像
docker build -f deployment/Dockerfile -t openidaas/app:latest ..

# 构建 Gateway 镜像
docker build -f deployment/Dockerfile.gateway -t openidaas/gateway:latest ..
```

### 运行容器

```bash
# 基本运行
docker run -d \
  --name openidaas \
  -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://postgres:5432/openidaas \
  -e REDIS_URL=redis://redis:6379 \
  -e JWT_SECRET=your-secret \
  openidaas/app:latest
```

---

## 🚀 Docker Compose 部署

### 开发环境

```bash
# 启动开发环境
docker-compose -f deployment/docker-compose.yml up -d

# 查看日志
docker-compose -f deployment/docker-compose.yml logs -f

# 停止服务
docker-compose -f deployment/docker-compose.yml down

# 停止并删除数据卷
docker-compose -f deployment/docker-compose.yml down -v
```

### 生产环境

```bash
# 复制并编辑环境变量
cp deployment/.env.example deployment/.env
vi deployment/.env

# 启动生产环境
docker-compose -f deployment/docker-compose.prod.yml up -d
```

### 服务列表

| 服务 | 端口 | 说明 |
|------|------|------|
| openidaas-gateway | 8080 | API 网关 |
| openidaas-auth | 8081 | 认证服务 |
| openidaas-user | 8082 | 用户服务 |
| openidaas-tenant | 8083 | 租户服务 |
| postgres | 5432 | PostgreSQL 数据库 |
| redis | 6379 | Redis 缓存 |
| prometheus | 9090 | 监控指标 |
| grafana | 3000 | 监控可视化 |
| adminer | 8084 | 数据库管理 |

---

## ☸️ Kubernetes 部署

### 前置要求

- Kubernetes 1.24+
- kubectl 配置完成
- 持久化存储支持

### 部署步骤

#### 1. 配置命名空间

```bash
kubectl apply -f deployment/k8s/namespace.yaml
```

#### 2. 创建 ConfigMap

```bash
kubectl apply -f deployment/k8s/configmap.yaml
```

#### 3. 创建 Secrets

```bash
# 编辑 secrets.yaml，使用实际值
vi deployment/k8s/secret.yaml

kubectl apply -f deployment/k8s/secret.yaml
```

#### 4. 部署服务

```bash
# 使用部署脚本
chmod +x deployment/k8s-deploy.sh
./deployment/k8s-deploy.sh

# 或手动部署
kubectl apply -f deployment/k8s/serviceaccount.yaml
kubectl apply -f deployment/k8s/pvc.yaml
kubectl apply -f deployment/k8s/deployment.yaml
kubectl apply -f deployment/k8s/service.yaml
kubectl apply -f deployment/k8s/ingress.yaml
```

### 验证部署

```bash
# 查看 Pod 状态
kubectl get pods -n openidaas

# 查看服务
kubectl get svc -n openidaas

# 查看 Ingress
kubectl get ingress -n openidaas

# 查看日志
kubectl logs -n openidaas -f deployment/openidaas-auth-xxx

# 端口转发
kubectl port-forward -n openidaas svc/openidaas-gateway-service 8080:8080
```

### 扩缩容

```bash
# 扩展到 3 个副本
kubectl scale deployment/openidaas-auth -n openidaas --replicas=3

# 自动扩缩容（需配置 HPA）
kubectl autoscale deployment/openidaas-auth \
  -n openidaas \
  --min=2 --max=10 \
  --cpu-percent=70
```

---

## 🔒 安全加固

### Docker 镜像安全

```bash
# 安全扫描
docker scan openidaas/app:latest

# 或使用 Trivy
trivy image openidaas/app:latest
```

### 安全最佳实践

1. **使用非 root 用户**
   ```dockerfile
   RUN addgroup -g 1000 openidaas && \
       adduser -D -u 1000 -G openidaas openidaas
   USER openidaas
   ```

2. **只读根文件系统**
   ```yaml
   # Kubernetes Deployment
   securityContext:
     runAsNonRoot: true
     readOnlyRootFilesystem: true
   ```

3. **资源限制**
   ```yaml
   resources:
     requests:
       cpu: 250m
       memory: 512Mi
     limits:
       cpu: 1000m
       memory: 1Gi
   ```

4. **网络策略**
   ```yaml
   apiVersion: networking.k8s.io/v1
   kind: NetworkPolicy
   metadata:
     name: openidaas-network-policy
   spec:
     podSelector:
       matchLabels:
         app: openidaas
     policyTypes:
       - Ingress
       - Egress
   ```

---

## 📊 监控和日志

### Prometheus 监控

访问 Prometheus: `http://localhost:9090`

#### 查询示例

```promql
# API 请求率
rate(http_server_requests_seconds_count{application="openidaas"}[5m])

# JVM 内存使用
jvm_memory_used_bytes{application="openidaas"}

# 数据库连接池
hikaricp_connections_active{application="openidaas"}
```

### Grafana 可视化

访问 Grafana: `http://localhost:3000`

- 默认用户: `admin`
- 默认密码: `admin123`

#### 导入仪表板

1. 登录 Grafana
2. 进入 Dashboards -> Import
3. 导入仪表板 ID: `4701` (JVM), `6417` (Spring Boot)

### 日志收集

```bash
# 查看所有日志
kubectl logs -n openidaas --all-containers=true -l app=openidaas

# 查看特定服务日志
kubectl logs -n openidaas -f deployment/openidaas-auth-xxx

# 查看最近 100 行
kubectl logs -n openidaas --tail=100 deployment/openidaas-auth-xxx
```

---

## 💾 备份和恢复

### 数据库备份

```bash
# 备份 PostgreSQL
docker exec openidaas-postgres-prod \
  pg_dump -U openidaas -d openidaas > backup_$(date +%Y%m%d).sql

# 定期备份（cron）
0 2 * * * docker exec openidaas-postgres-prod \
  pg_dump -U openidaas -d openidaas > /backups/backup_$(date +\%Y\%m\%d).sql
```

### 数据库恢复

```bash
# 恢复 PostgreSQL
cat backup_20260208.sql | \
  docker exec -i openidaas-postgres-prod \
  psql -U openidaas -d openidaas
```

### Redis 备份

```bash
# 备份 Redis RDB 文件
docker cp openidaas-redis-prod:/data/dump.rdb ./backup/dump_$(date +%Y%m%d).rdb
```

---

## 🔧 故障排除

### 常见问题

#### 1. 容器启动失败

```bash
# 查看容器日志
docker logs openidaas-auth

# 查看 Kubernetes Pod 日志
kubectl logs -n openidaas deployment/openidaas-auth-xxx
```

#### 2. 数据库连接失败

检查网络和配置：
```bash
# 检查网络
docker network ls
docker network inspect openidaas-network

# 测试连接
docker exec openidaas-auth ping postgres
```

#### 3. 内存不足

调整 JVM 参数：
```yaml
environment:
  JAVA_OPTS: "-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
```

#### 4. 健康检查失败

调整健康检查参数：
```yaml
livenessProbe:
  initialDelaySeconds: 90  # 增加启动延迟
  periodSeconds: 30
  timeoutSeconds: 10
```

---

## 📚 相关文档

- [数据库设计文档](../openidaas-core/DATABASE_DESIGN.md)
- [Starter 使用文档](../openidaas-starter/README.md)
- [用户管理模块](../openidaas-user/USER_MANAGEMENT_README.md)

---

**版本**: 1.0.0
**更新时间**: 2026-02-08
**维护者**: OpenIDaaS Team
