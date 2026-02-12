#!/bin/bash

# OpenIDaaS K8s 部署脚本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置
NAMESPACE="openidaas"
REGISTRY=${DOCKER_REGISTRY:-your-registry.com}
IMAGE_TAG=${IMAGE_TAG:-1.0.0}

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}OpenIDaaS K8s 部署脚本${NC}"
echo -e "${GREEN}========================================${NC}"

# 检查 kubectl
if ! command -v kubectl &> /dev/null; then
    echo -e "${RED}错误: 未找到 kubectl 命令${NC}"
    exit 1
fi

# 检查当前上下文
CURRENT_CONTEXT=$(kubectl config current-context)
echo -e "${YELLOW}当前 K8s 上下文: ${CURRENT_CONTEXT}${NC}"

# 创建命名空间
echo -e "\n${GREEN}创建命名空间...${NC}"
kubectl apply -f k8s/namespace.yaml

# 创建 ConfigMap
echo -e "\n${GREEN}创建 ConfigMap...${NC}"
kubectl apply -f k8s/configmap.yaml

# 创建 Secrets（从环境变量读取）
echo -e "\n${GREEN}创建 Secrets...${NC}"
envsubst < k8s/secret.yaml | kubectl apply -f -

# 创建 ServiceAccount
echo -e "\n${GREEN}创建 ServiceAccount...${NC}"
kubectl apply -f k8s/serviceaccount.yaml

# 部署基础设施服务
echo -e "\n${GREEN}部署基础设施服务...${NC}"
# 注意：MySQL、Redis、Nacos 等基础设施需要单独部署

# 部署应用服务
echo -e "\n${GREEN}部署应用服务...${NC}"
for service in gateway auth-service user-service organization-service role-service application-service audit-service admin-ui; do
    echo -e "${YELLOW}部署 $service...${NC}"
    envsubst < k8s/deployments/${service}.yaml | kubectl apply -f -
done

# 等待服务就绪
echo -e "\n${GREEN}等待服务就绪...${NC}"
kubectl wait --for=condition=available --timeout=300s -n ${NAMESPACE} deployment/gateway || true
kubectl wait --for=condition=available --timeout=300s -n ${NAMESPACE} deployment/auth-service || true
kubectl wait --for=condition=available --timeout=300s -n ${NAMESPACE} deployment/user-service || true

# 创建 Ingress
echo -e "\n${GREEN}创建 Ingress...${NC}"
kubectl apply -f k8s/ingress.yaml

# 显示部署状态
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}部署完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "${YELLOW}Pod 状态:${NC}"
kubectl get pods -n ${NAMESPACE}

echo -e "\n${YELLOW}Service 状态:${NC}"
kubectl get svc -n ${NAMESPACE}

echo -e "\n${YELLOW}查看日志:${NC}"
echo -e "kubectl logs -n ${NAMESPACE} deployment/gateway -f"
echo -e "kubectl logs -n ${NAMESPACE} deployment/auth-service -f"

echo -e "\n${YELLOW}访问应用:${NC}"
echo -e "前端: http://admin.openidaas.com"
echo -e "API: http://api.openidaas.com"
