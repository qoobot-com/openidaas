#!/bin/bash

################################################################################
# OpenIDaaS Multi-Architecture Docker Build Script
#
# Builds Docker images for both AMD64 and ARM64 architectures
#
# Usage: ./build-multiarch.sh [options]
#
# Options:
#   -r, --registry REGISTRY   Docker registry (default: openidaas)
#   -v, --version VERSION    Image version (default: latest)
#   -p, --push              Push images to registry
#   -h, --help              Show this help message
################################################################################

set -e

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Default values
REGISTRY="openidaas"
VERSION="latest"
PUSH=false

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -r|--registry)
            REGISTRY="$2"
            shift 2
            ;;
        -v|--version)
            VERSION="$2"
            shift 2
            ;;
        -p|--push)
            PUSH=true
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [options]"
            echo ""
            echo "Options:"
            echo "  -r, --registry REGISTRY   Docker registry (default: openidaas)"
            echo "  -v, --version VERSION    Image version (default: latest)"
            echo "  -p, --push              Push images to registry"
            echo "  -h, --help              Show this help message"
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            exit 1
            ;;
    esac
done

# Architectures to build
ARCHITECTURES=("linux/amd64" "linux/arm64")

# Services to build
SERVICES=("auth" "user" "tenant" "gateway")

# Build function
build_image() {
    local service=$1
    local arch=$2

    echo -e "${YELLOW}Building $REGISTRY/$service:$VERSION for $arch...${NC}"

    docker buildx build \
        --platform $arch \
        --file Dockerfile.multiarch \
        --build-arg PLATFORM=$arch \
        --tag $REGISTRY/$service:$VERSION-$arch \
        --load \
        .

    if [ "$PUSH" = true ]; then
        echo -e "${YELLOW}Pushing $REGISTRY/$service:$VERSION-$arch...${NC}"
        docker push $REGISTRY/$service:$VERSION-$arch
    fi
}

# Create manifest for multi-arch image
create_manifest() {
    local service=$1

    echo -e "${YELLOW}Creating manifest for $REGISTRY/$service:$VERSION...${NC}"

    docker manifest create $REGISTRY/$service:$VERSION \
        $REGISTRY/$service:$VERSION-linux-amd64 \
        $REGISTRY/$service:$VERSION-linux-arm64

    if [ "$PUSH" = true ]; then
        echo -e "${YELLOW}Pushing manifest $REGISTRY/$service:$VERSION...${NC}"
        docker manifest push $REGISTRY/$service:$VERSION
    fi
}

# Main build loop
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}OpenIDaaS Multi-Architecture Build${NC}"
echo -e "${GREEN}========================================${NC}"
echo "Registry: $REGISTRY"
echo "Version: $VERSION"
echo "Push: $PUSH"
echo ""

for service in "${SERVICES[@]}"; do
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Building $service${NC}"
    echo -e "${GREEN}========================================${NC}"

    for arch in "${ARCHITECTURES[@]}"; do
        build_image $service $arch
    done

    create_manifest $service

    echo -e "${GREEN}✓ $service build completed${NC}"
    echo ""
done

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}All builds completed successfully!${NC}"
echo -e "${GREEN}========================================${NC}"
