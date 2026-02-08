#!/bin/bash

################################################################################
# OpenIDaaS Kubernetes Deployment Script
#
# Deploys OpenIDaaS to Kubernetes cluster
#
# Usage: ./k8s-deploy.sh [options]
#
# Options:
#   -n, --namespace NAMESPACE  Kubernetes namespace (default: openidaas)
#   -e, --env ENV           Environment (dev, staging, prod)
#   -d, --delete             Delete all resources
#   -h, --help              Show this help message
################################################################################

set -e

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Default values
NAMESPACE="openidaas"
ENV="dev"
DELETE=false

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -e|--env)
            ENV="$2"
            shift 2
            ;;
        -d|--delete)
            DELETE=true
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [options]"
            echo ""
            echo "Options:"
            echo "  -n, --namespace NAMESPACE  Kubernetes namespace (default: openidaas)"
            echo "  -e, --env ENV           Environment (dev, staging, prod)"
            echo "  -d, --delete             Delete all resources"
            echo "  -h, --help              Show this help message"
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            exit 1
            ;;
    esac
done

# Kubernetes manifest directory
K8S_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/k8s"

# Delete resources
if [ "$DELETE" = true ]; then
    echo -e "${YELLOW}Deleting OpenIDaaS resources from namespace: $NAMESPACE${NC}"

    kubectl delete namespace $NAMESPACE --ignore-not-found=true

    echo -e "${GREEN}✓ Resources deleted${NC}"
    exit 0
fi

# Create namespace
echo -e "${YELLOW}Creating namespace: $NAMESPACE${NC}"
kubectl apply -f $K8S_DIR/namespace.yaml

# Create service account and RBAC
echo -e "${YELLOW}Creating service account and RBAC...${NC}"
kubectl apply -f $K8S_DIR/serviceaccount.yaml

# Create config map
echo -e "${YELLOW}Creating config map...${NC}"
kubectl apply -f $K8S_DIR/configmap.yaml

# Create secrets (use existing or create placeholder)
echo -e "${YELLOW}Creating secrets...${NC}"
if ! kubectl get secret openidaas-secrets -n $NAMESPACE &> /dev/null; then
    echo -e "${YELLOW}Secret not found, creating placeholder...${NC}"
    kubectl apply -f $K8S_DIR/secret.yaml
    echo -e "${RED}⚠ Please update secrets with actual values!${NC}"
else
    kubectl apply -f $K8S_DIR/secret.yaml
fi

# Create PVCs
echo -e "${YELLOW}Creating persistent volume claims...${NC}"
kubectl apply -f $K8S_DIR/pvc.yaml

# Wait for PVCs to be ready
echo -e "${YELLOW}Waiting for PVCs to be ready...${NC}"
kubectl wait --for=condition=bound pvc/postgres-pvc -n $NAMESPACE --timeout=60s
kubectl wait --for=condition=bound pvc/redis-pvc -n $NAMESPACE --timeout=60s

# Create deployments
echo -e "${YELLOW}Creating deployments...${NC}"
kubectl apply -f $K8S_DIR/deployment.yaml

# Create services
echo -e "${YELLOW}Creating services...${NC}"
kubectl apply -f $K8S_DIR/service.yaml

# Create ingress
echo -e "${YELLOW}Creating ingress...${NC}"
kubectl apply -f $K8S_DIR/ingress.yaml

# Wait for deployments to be ready
echo -e "${YELLOW}Waiting for deployments to be ready...${NC}"
kubectl wait --for=condition=available deployment/postgres -n $NAMESPACE --timeout=300s
kubectl wait --for=condition=available deployment/redis -n $NAMESPACE --timeout=300s
kubectl wait --for=condition=available deployment/openidaas-auth -n $NAMESPACE --timeout=300s
kubectl wait --for=condition=available deployment/openidaas-user -n $NAMESPACE --timeout=300s
kubectl wait --for=condition=available deployment/openidaas-gateway -n $NAMESPACE --timeout=300s

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Deployment completed successfully!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Namespace: $NAMESPACE"
echo "Environment: $ENV"
echo ""
echo "Services:"
kubectl get svc -n $NAMESPACE
echo ""
echo "Pods:"
kubectl get pods -n $NAMESPACE
echo ""
echo -e "${YELLOW}To access the application:${NC}"
echo "  Port forward: kubectl port-forward -n $NAMESPACE svc/openidaas-gateway-service 8080:8080"
echo "  Ingress:     Check your ingress configuration for external URL"
