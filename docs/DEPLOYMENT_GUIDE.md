# OpenIDaaS 部署指南

## 目录
1. [部署架构](#部署架构)
2. [环境准备](#环境准备)
3. [本地开发部署](#本地开发部署)
4. [Docker 部署](#docker-部署)
5. [Kubernetes 部署](#kubernetes-部署)
6. [生产环境部署](#生产环境部署)
7. [监控与运维](#监控与运维)
8. [备份与恢复](#备份与恢复)
9. [故障排查](#故障排查)

---

## 部署架构

### 生产环境架构

```
                    ┌─────────────────┐
                    │   Load Balancer  │
                    │   (Nginx/SLB)    │
                    └────────┬─────────┘
                             │
                ┌────────────┼────────────┐
                │            │            │
          ┌─────▼────┐  ┌────▼────┐  ┌───▼──────┐
          │ Gateway  │  │ Gateway │  │  Gateway │
          │   Pod1   │  │  Pod2   │  │   Pod3   │
          └─────┬────┘  └────┬────┘  └────┬─────┘
                │            │            │
         ┌──────┴────┬───────┴────┬──────┴──────┐
         │           │            │              │
    ┌────▼────┐ ┌───▼────┐  ┌───▼─────┐  ┌────▼─────┐
    │ Auth    │ │  User  │  │  Org    │  │   Role   │
    │ Service │ │ Service│  │ Service │  │  Service │
    └────┬────┘ └───┬────┘  └───┬─────┘  └────┬─────┘
         │           │            │              │
         └───────────┼────────────┴──────────────┘
                     │
         ┌───────────┼───────────┐
         │           │           │
    ┌────▼────┐ ┌───▼────┐  ┌───▼────┐
    │  MySQL  │ │  Redis │  │ Nacos  │
    │ (Master)│ │ Cluster│  │ Cluster│
    └─────────┘ └────────┘  └────────┘
```

---

## 环境准备

### 系统要求

#### 最低配置
- **CPU**: 4 核
- **内存**: 8 GB
- **磁盘**: 50 GB SSD

#### 推荐配置（生产环境）
- **CPU**: 8+ 核
- **内存**: 16+ GB
- **磁盘**: 200+ GB SSD
- **网络**: 1 Gbps

### 软件依赖

| 软件 | 版本 | 用途 |
|-----|------|------|
| JDK | 17+ | Java 运行环境 |
| Maven | 3.8+ | 构建工具 |
| Docker | 20.10+ | 容器化部署 |
| Kubernetes | 1.24+ | 编排调度 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 7.0+ | 缓存数据库 |
| Nginx | 1.20+ | 负载均衡 |

### 端口规划

| 服务 | 端口 | 说明 |
|-----|------|------|
| Nginx | 80/443 | HTTP/HTTPS |
| Gateway | 8080 | API 网关 |
| Auth Service | 8082 | 认证服务 |
| User Service | 8081 | 用户服务 |
| Org Service | 8084 | 组织服务 |
| Role Service | 8083 | 角色服务 |
| App Service | 8086 | 应用服务 |
| Audit Service | 8085 | 审计服务 |
| Nacos | 8848 | 配置中心 |
| Sentinel | 8858 | 流量控制 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |

---

## 本地开发部署

### 1. 安装依赖

#### 安装 JDK 17

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# CentOS/RHEL
sudo yum install java-17-openjdk-devel

# macOS
brew install openjdk@17

# 验证安装
java -version
```

#### 安装 Maven

```bash
# Ubuntu/Debian
sudo apt install maven

# CentOS/RHEL
sudo yum install maven

# macOS
brew install maven

# 验证安装
mvn -version
```

#### 安装 MySQL

```bash
# Ubuntu/Debian
sudo apt install mysql-server

# macOS
brew install mysql

# 启动服务
sudo systemctl start mysql
brew services start mysql

# 安全配置
sudo mysql_secure_installation
```

#### 安装 Redis

```bash
# Ubuntu/Debian
sudo apt install redis-server

# macOS
brew install redis

# 启动服务
sudo systemctl start redis
brew services start redis
```

#### 安装 Nacos

```bash
# 下载
wget https://github.com/alibaba/nacos/releases/download/2.2.3/nacos-server-2.2.3.zip
unzip nacos-server-2.2.3.zip -d /opt/

# 启动（单机模式）
cd /opt/nacos/bin
./startup.sh -m standalone
```

#### 安装 Sentinel Dashboard

```bash
# 下载
wget https://github.com/alibaba/Sentinel/releases/download/1.8.6/sentinel-dashboard-1.8.6.jar

# 启动
java -Dserver.port=8858 -Dcsp.sentinel.dashboard.server=localhost:8858 \
     -Dproject.name=sentinel-dashboard \
     -jar sentinel-dashboard-1.8.6.jar
```

### 2. 数据库初始化

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE open_idaas CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 执行初始化脚本
mysql -u root -p open_idaas < db/schema.sql
mysql -u root -p open_idaas < db/init_data.sql
```

### 3. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑配置
vim .env

# 加载环境变量
source .env
```

### 4. 编译项目

```bash
# 清理并编译
mvn clean install -DskipTests

# 或跳过测试和检查
mvn clean install -DskipTests -Dmaven.javadoc.skip=true
```

### 5. 启动服务

#### 启动基础设施

```bash
# 启动 Nacos
cd /opt/nacos/bin
./startup.sh -m standalone

# 启动 Sentinel
java -jar sentinel-dashboard-1.8.6.jar &

# 启动 MySQL
sudo systemctl start mysql

# 启动 Redis
sudo systemctl start redis
```

#### 启动微服务

```bash
# 方式1: 使用 Maven 启动
cd openidaas-gateway
mvn spring-boot:run

cd ../openidaas-auth-service
mvn spring-boot:run

cd ../openidaas-user-service
mvn spring-boot:run

# ... 其他服务
```

```bash
# 方式2: 使用 JAR 包启动
java -jar openidaas-gateway/target/openidaas-gateway-1.0.0.jar \
    --spring.profiles.active=dev

java -jar openidaas-auth-service/target/openidaas-auth-service-1.0.0.jar \
    --spring.profiles.active=dev

# ... 其他服务
```

### 6. 验证部署

```bash
# 检查服务健康
curl http://localhost:8080/actuator/health
curl http://localhost:8082/actuator/health

# 检查 Nacos 服务列表
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=user-service

# 访问 Nacos 控制台
open http://localhost:8848/nacos
```

---

## Docker 部署

### 1. 安装 Docker

```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# CentOS/RHEL
sudo yum install docker-ce docker-ce-cli containerd.io

# 启动 Docker
sudo systemctl start docker
sudo systemctl enable docker

# 验证安装
docker --version
```

### 2. 构建镜像

```bash
# 构建所有服务镜像
./build-images.sh

# 或单独构建
cd openidaas-gateway
docker build -t openidaas/gateway:latest .

cd ../openidaas-auth-service
docker build -t openidaas/auth-service:latest .
```

### 3. 配置环境变量

```bash
# 复制环境变量模板
cp docker-compose.env.example docker-compose.env

# 编辑配置
vim docker-compose.env
```

### 4. 启动服务

#### 方式1: 使用 Docker Compose

```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down

# 停止并删除数据
docker-compose down -v
```

#### 方式2: 分别启动基础设施和应用

```bash
# 启动基础设施
docker-compose -f docker-compose-init.yml up -d

# 等待服务就绪
sleep 30

# 启动应用服务
docker-compose up -d
```

### 5. 验证部署

```bash
# 查看容器状态
docker-compose ps

# 查看日志
docker-compose logs gateway
docker-compose logs auth-service

# 进入容器
docker-compose exec gateway bash

# 检查服务健康
curl http://localhost:8080/actuator/health
```

### 6. 常用操作

```bash
# 重启服务
docker-compose restart gateway

# 扩展服务
docker-compose up -d --scale gateway=3 --scale auth-service=2

# 查看资源使用
docker stats

# 清理未使用的资源
docker system prune -a
```

---

## Kubernetes 部署

### 1. 安装 Kubernetes

#### 使用 Minikube（本地）

```bash
# 安装 Minikube
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube

# 启动集群
minikube start --cpus=4 --memory=8192

# 安装 kubectl
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install kubectl /usr/local/bin/kubectl

# 验证安装
kubectl cluster-info
kubectl get nodes
```

#### 使用 k3s（轻量级）

```bash
# 安装 k3s
curl -sfL https://get.k3s.io | sh -

# 获取 kubeconfig
sudo cat /etc/rancher/k3s/k3s.yaml

# 验证安装
kubectl get nodes
```

### 2. 准备镜像

#### 推送镜像到仓库

```bash
# 登录镜像仓库
docker login

# 标记镜像
docker tag openidaas/gateway:latest your-registry/openidaas/gateway:1.0.0

# 推送镜像
docker push your-registry/openidaas/gateway:1.0.0
```

#### 或使用本地镜像（Minikube）

```bash
# 加载镜像到 Minikube
minikube image load openidaas/gateway:latest
minikube image load openidaas/auth-service:latest
```

### 3. 创建命名空间

```bash
# 创建命名空间
kubectl create namespace openidaas

# 设置默认命名空间
kubectl config set-context --current --namespace=openidaas
```

### 4. 创建配置

#### 4.1 创建 Secret

```bash
# 方式1: 从环境文件创建
kubectl create secret generic openidaas-secrets \
  --from-env-file=.env \
  --namespace=openidaas \
  --dry-run=client -o yaml > k8s/secret.yaml

kubectl apply -f k8s/secret.yaml
```

```yaml
# 方式2: 手动创建
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

#### 4.2 创建 ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: openidaas-config
  namespace: openidaas
data:
  SPRING_PROFILES_ACTIVE: "prod"
  NACOS_SERVER_ADDR: "nacos-service:8848"
  REDIS_HOST: "redis-service"
  DB_HOST: "mysql-service"
```

```bash
kubectl apply -f k8s/configmap.yaml
```

### 5. 部署应用

```bash
# 部署所有服务
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/serviceaccount.yaml
kubectl apply -f k8s/pvc.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml

# 或一次性部署
kubectl apply -f k8s/
```

### 6. 验证部署

```bash
# 查看 Pod 状态
kubectl get pods -n openidaas

# 查看 Service
kubectl get svc -n openidaas

# 查看 Ingress
kubectl get ingress -n openidaas

# 查看日志
kubectl logs -f deployment/gateway -n openidaas
kubectl logs -f deployment/auth-service -n openidaas

# 进入 Pod
kubectl exec -it <pod-name> -n openidaas -- bash

# 端口转发
kubectl port-forward svc/gateway 8080:8080 -n openidaas
```

### 7. 扩缩容

```bash
# 手动扩缩容
kubectl scale deployment gateway --replicas=3 -n openidaas
kubectl scale deployment auth-service --replicas=2 -n openidaas

# 配置 HPA（自动扩缩容）
kubectl autoscale deployment gateway \
  --cpu-percent=70 \
  --min=2 \
  --max=10 \
  -n openidaas

# 查看 HPA
kubectl get hpa -n openidaas
```

### 8. 更新部署

```bash
# 滚动更新
kubectl set image deployment/gateway \
  gateway=openidaas/gateway:1.1.0 \
  -n openidaas

# 查看更新状态
kubectl rollout status deployment/gateway -n openidaas

# 回滚到上一个版本
kubectl rollout undo deployment/gateway -n openidaas

# 回滚到指定版本
kubectl rollout undo deployment/gateway --to-revision=2 -n openidaas
```

---

## 生产环境部署

### 1. 高可用架构

#### 数据库主从复制

```sql
-- 主库配置
server-id = 1
log-bin = mysql-bin
binlog-format = ROW

-- 从库配置
server-id = 2
relay-log = mysql-relay-bin
read-only = 1
```

#### Redis 集群

```bash
# Redis Sentinel 配置
sentinel monitor mymaster 127.0.0.1 6379 2
sentinel down-after-milliseconds mymaster 5000
sentinel failover-timeout mymaster 10000
```

#### Nacos 集群

```yaml
# 集群配置
nacos:
  cluster:
    enabled: true
    nodes:
      - nacos1:8848
      - nacos2:8848
      - nacos3:8848
```

### 2. 负载均衡

#### Nginx 配置

```nginx
upstream gateway {
    least_conn;
    server gateway-1:8080 weight=3;
    server gateway-2:8080 weight=3;
    server gateway-3:8080 weight=2;
    keepalive 32;
}

server {
    listen 80;
    server_name api.openidaas.com;

    location / {
        proxy_pass http://gateway;
        proxy_http_version 1.1;
        proxy_set_header Connection "";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_connect_timeout 30s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;
    }
}
```

### 3. SSL/TLS 配置

```bash
# 生成 SSL 证书
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout /etc/nginx/ssl/openidaas.key \
  -out /etc/nginx/ssl/openidaas.crt

# 创建 Kubernetes TLS Secret
kubectl create secret tls openidaas-tls \
  --cert=openidaas.crt \
  --key=openidaas.key \
  -n openidaas
```

```yaml
# Ingress 配置
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: openidaas-ingress
  namespace: openidaas
  annotations:
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
spec:
  tls:
  - hosts:
    - api.openidaas.com
    secretName: openidaas-tls
  rules:
  - host: api.openidaas.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: gateway
            port:
              number: 80
```

### 4. 日志收集

#### ELK Stack 配置

```yaml
# Filebeat 配置
filebeat.inputs:
- type: container
  paths:
    - /var/log/containers/*.log
  processors:
  - add_kubernetes_metadata:
      host: ${NODE_NAME}
      matchers:
      - logs_path:
          logs_path: "/var/log/containers/"

output.elasticsearch:
  hosts: ["elasticsearch:9200"]
  index: "openidaas-%{+yyyy.MM.dd}"
```

---

## 监控与运维

### 1. 健康检查

```bash
# 服务健康检查
curl http://api.openidaas.com/actuator/health

# Prometheus 指标
curl http://api.openidaas.com/actuator/metrics

# 查看服务依赖
curl http://api.openidaas.com/actuator/beans
```

### 2. 日志管理

```bash
# 查看 Kubernetes 日志
kubectl logs -f deployment/gateway -n openidaas --tail=100

# 查看多个 Pod 日志
kubectl logs -f -l app=gateway -n openidaas

# 查看前一个版本的日志
kubectl logs deployment/gateway --previous -n openidaas
```

### 3. 监控指标

```yaml
# Prometheus 告警规则
groups:
- name: openidaas
  rules:
  - alert: HighErrorRate
    expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
    for: 5m
    labels:
      severity: critical
    annotations:
      summary: "高错误率: {{ $labels.instance }}"

  - alert: HighResponseTime
    expr: rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m]) > 1
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "高响应时间: {{ $labels.instance }}"
```

---

## 备份与恢复

### 1. 数据库备份

```bash
# 全量备份
mysqldump -u root -p open_idaas > backup/open_idaas_$(date +%Y%m%d).sql

# 压缩备份
mysqldump -u root -p open_idaas | gzip > backup/open_idaas_$(date +%Y%m%d).sql.gz

# 定时备份
0 2 * * * /usr/bin/mysqldump -u root -p open_idaas | gzip > /backup/open_idaas_$(date +\%Y\%m\%d).sql.gz
```

### 2. 数据库恢复

```bash
# 恢复备份
mysql -u root -p open_idaas < backup/open_idaas_20240101.sql

# 恢复压缩备份
gunzip < backup/open_idaas_20240101.sql.gz | mysql -u root -p open_idaas
```

### 3. Redis 备份

```bash
# RDB 备份
cp /var/lib/redis/dump.rdb backup/redis_dump_$(date +%Y%m%d).rdb

# AOF 备份
cp /var/lib/redis/appendonly.aof backup/redis_appendonly_$(date +%Y%m%d).aof
```

---

## 故障排查

### 1. 服务无法启动

**检查清单**:
- 端口是否被占用
- 环境变量是否正确
- 依赖服务是否正常
- 日志中的错误信息

**排查命令**:
```bash
# 检查端口
netstat -tunlp | grep 8080

# 查看日志
tail -f /var/log/openidaas/application.log

# 检查依赖
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=nacos
```

### 2. 服务间调用失败

**检查清单**:
- 服务注册状态
- 网络连接
- 负载均衡配置
- 防火墙规则

**排查命令**:
```bash
# 检查服务注册
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=user-service

# 测试网络连接
telnet user-service 8081

# 检查负载均衡
kubectl get endpoints -n openidaas
```

### 3. 数据库连接失败

**检查清单**:
- 数据库服务状态
- 连接配置
- 连接池设置
- 密码是否正确

**排查命令**:
```bash
# 检查 MySQL 状态
systemctl status mysql

# 测试连接
mysql -h localhost -u root -p

# 查看连接数
mysql -e "SHOW PROCESSLIST;"
```

---

## 附录

### 部署检查清单

- [ ] 系统资源满足要求
- [ ] 软件依赖已安装
- [ ] 数据库已初始化
- [ ] 环境变量已配置
- [ ] 镜像已构建/推送
- [ ] 配置文件已更新
- [ ] 服务已正常启动
- [ ] 健康检查通过
- [ ] 负载均衡已配置
- [ ] SSL/TLS 证书已配置
- [ ] 监控告警已配置
- [ ] 日志收集已配置
- [ ] 备份策略已配置

### 端口占用检查

```bash
# 检查所有服务端口
for port in 8080 8081 8082 8083 8084 8085 8086 8848 8858 8091 3306 6379; do
    netstat -tunlp | grep $port && echo "Port $port is in use"
done
```

### 服务启动顺序

1. MySQL
2. Redis
3. Nacos
4. Sentinel Dashboard
5. Gateway
6. Auth Service
7. User Service
8. Organization Service
9. Role Service
10. Application Service
11. Audit Service
12. Authorization Service
