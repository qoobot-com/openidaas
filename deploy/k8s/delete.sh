#!/bin/bash

# OpenIDaaS K8s 删除脚本

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

NAMESPACE="openidaas"

echo -e "${RED}警告: 此操作将删除 OpenIDaaS 所有 K8s 资源！${NC}"
read -p "确认删除? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "取消删除"
    exit 0
fi

echo -e "${GREEN}删除 Ingress...${NC}"
kubectl delete -f k8s/ingress.yaml --ignore-not-found=true

echo -e "${GREEN}删除应用服务...${NC}"
kubectl delete -f k8s/deployments/ --ignore-not-found=true

echo -e "${GREEN}删除 ServiceAccount...${NC}"
kubectl delete -f k8s/serviceaccount.yaml --ignore-not-found=true

echo -e "${GREEN}删除 ConfigMap...${NC}"
kubectl delete -f k8s/configmap.yaml --ignore-not-found=true

echo -e "${GREEN}删除 Secrets...${NC}"
kubectl delete -f k8s/secret.yaml --ignore-not-found=true

echo -e "${GREEN}删除命名空间...${NC}"
kubectl delete namespace ${NAMESPACE} --ignore-not-found=true

echo -e "${GREEN}删除完成！${NC}"
