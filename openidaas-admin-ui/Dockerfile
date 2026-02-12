# 多阶段构建 - Admin UI
# Stage 1: Node.js 构建
FROM node:20-alpine AS builder

LABEL maintainer="QooBot <support@qoobot.com>"

WORKDIR /build

# 复制 package 文件
COPY package*.json ./

# 安装依赖（利用 Docker 缓存）
RUN npm ci --only=production

# 复制源码
COPY . .

# 构建生产版本
RUN npm run build

# Stage 2: Nginx 运行时
FROM nginx:1.25-alpine

LABEL maintainer="QooBot <support@qoobot.com>"
LABEL description="OpenIDaaS Admin UI"

# 安装 wget 用于健康检查
RUN apk add --no-cache wget

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/nginx.conf

# 从构建阶段复制构建产物
COPY --from=builder /build/dist /usr/share/nginx/html

# 创建非 root 用户
RUN addgroup -S nginx && adduser -S nginx -G nginx && \
    chown -R nginx:nginx /usr/share/nginx/html && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    chown -R nginx:nginx /etc/nginx/conf.d && \
    touch /var/run/nginx.pid && \
    chown -R nginx:nginx /var/run/nginx.pid

USER nginx

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost/health || exit 1

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
