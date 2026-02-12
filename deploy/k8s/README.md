# OpenIDaaS Kubernetes 部署文档

## 前置要求

- Kubernetes 1.20+
- kubectl 已安装并配置
- Docker Registry（用于存储镜像）
- 域名配置（用于 Ingress）

## 目录结构

```
k8s/
├── namespace.yaml          # 命名空间
├── secret.yaml            # 密钥配置
├── configmap.yaml         # 配置映射
├── serviceaccount.yaml     # 服务账户
├── ingress.yaml           # 入口路由
├── deployments/           # 部署配置
│   ├── gateway.yaml
│   ├── auth-service.yaml
│   ├── user-service.yaml
│   ├── organization-service.yaml
│   ├── role-service.yaml
│   ├── application-service.yaml
│   ├── audit-service.yaml
│   └── admin-ui.yaml
├── deploy.sh             # 部署脚本
└── delete.sh            # 删除脚本
```

## 快速开始

### 1. 配置环境变量

复制 `.env.example` 为 `.env` 并修改配置：

```bash
cp .env.example .env
```

关键配置项：
- `MYSQL_PASSWORD` - MySQL 密码
- `REDIS_PASSWORD` - Redis 密码
- `NACOS_AUTH_TOKEN` - Nacos 认证令牌
- `JWT_SIGNING_KEY` - JWT 签名密钥

### 2. 构建 Docker 镜像

```bash
./build-images.sh
```

### 3. 推送镜像到 Registry

```bash
docker push localhost/openidaas-gateway:1.0.0
docker push localhost/openidaas-auth-service:1.0.0
# ... 其他服务
```

### 4. 部署到 Kubernetes

```bash
./k8s/deploy.sh
```

### 5. 验证部署

```bash
# 查看 Pod 状态
kubectl get pods -n openidaas

# 查看 Service
kubectl get svc -n openidaas

# 查看日志
kubectl logs -n openidaas deployment/gateway -f
```

## 手动部署步骤

### 创建命名空间

```bash
kubectl apply -f k8s/namespace.yaml
```

### 创建配置和密钥

```bash
# 创建 ConfigMap
kubectl apply -f k8s/configmap.yaml

# 创建 Secrets（需先替换变量）
envsubst < k8s/secret.yaml | kubectl apply -f -
```

### 部署服务

```bash
# 部署网关
kubectl apply -f k8s/deployments/gateway.yaml

# 部署认证服务
kubectl apply -f k8s/deployments/auth-service.yaml

# ... 部署其他服务
```

### 创建 Ingress

```bash
kubectl apply -f k8s/ingress.yaml
```

## 扩容

### 手动扩容

```bash
kubectl scale deployment gateway -n openidaas --replicas=3
```

### 自动扩容

已配置 HPA，支持根据 CPU/内存自动扩容：

```bash
# 查看 HPA 状态
kubectl get hpa -n openidaas

# 手动调整扩容范围
kubectl edit hpa gateway-hpa -n openidaas
```

## 监控

### 查看 Pod 日志

```bash
# 查看所有 Pod
kubectl get pods -n openidaas

# 查看特定 Pod 日志
kubectl logs -n openidaas <pod-name> -f

# 查看多个 Pod 日志
kubectl logs -n openidaas -l app=gateway -f --all-containers=true
```

### 查看资源使用

```bash
# 资源使用情况
kubectl top nodes
kubectl top pods -n openidaas

# 事件
kubectl get events -n openidaas --sort-by='.lastTimestamp'
```

### 访问应用

配置 DNS 解析或修改 `/etc/hosts`：

```
<LB-IP> api.openidaas.com
<LB-IP> admin.openidaas.com
```

访问地址：
- 前端: https://admin.openidaas.com
- API: https://api.openidaas.com

## 故障排查

### Pod 启动失败

```bash
# 查看 Pod 详情
kubectl describe pod <pod-name> -n openidaas

# 查看日志
kubectl logs <pod-name> -n openidaas
```

### 服务不可访问

```bash
# 检查 Service
kubectl get svc -n openidaas

# 检查 Endpoints
kubectl get endpoints -n openidaas

# 测试服务连通性
kubectl run -it --rm debug --image=busybox -- sh
wget -O- http://gateway:8080/actuator/health
```

### 滚动更新失败

```bash
# 回滚到上一版本
kubectl rollout undo deployment/gateway -n openidaas

# 查看更新状态
kubectl rollout status deployment/gateway -n openidaas
```

## 删除部署

```bash
./k8s/delete.sh
```

## 生产环境建议

1. **安全**
   - 使用强密码
   - 启用 RBAC
   - 使用 NetworkPolicy 限制网络访问
   - 定期更新镜像

2. **高可用**
   - 配置多副本（至少 2 个）
   - 使用反亲和性分布 Pod
   - 配置 PodDisruptionBudget

3. **监控告警**
   - 集成 Prometheus
   - 配置 Grafana 仪表盘
   - 设置告警规则

4. **备份**
   - 定期备份 MySQL 数据
   - 备份 Nacos 配置
   - 备份 Redis 数据（如果使用持久化）

5. **日志**
   - 集成 ELK/Loki
   - 集中收集日志
   - 配置日志保留策略
