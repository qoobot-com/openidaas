#!/bin/bash

# OpenIDaaS Docker 镜像构建脚本

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

REGISTRY=${DOCKER_REGISTRY:-localhost}
IMAGE_TAG=${IMAGE_TAG:-1.0.0}

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}构建 Docker 镜像${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "${YELLOW}Registry: ${REGISTRY}${NC}"
echo -e "${YELLOW}Tag: ${IMAGE_TAG}${NC}"

SERVICES=(
    "openidaas-gateway"
    "openidaas-auth-service"
    "openidaas-user-service"
    "openidaas-organization-service"
    "openidaas-role-service"
    "openidaas-application-service"
    "openidaas-audit-service"
    "openidaas-admin-ui"
)

for service in "${SERVICES[@]}"; do
    echo -e "\n${GREEN}构建 ${service}...${NC}"
    docker build -t ${REGISTRY}/${service}:${IMAGE_TAG} -f ${service}/Dockerfile .
    echo -e "${GREEN}✓ ${service} 构建完成${NC}"
done

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}所有镜像构建完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "${YELLOW}推送到 Registry:${NC}"
echo -e "docker push ${REGISTRY}/<service>:${IMAGE_TAG}"
