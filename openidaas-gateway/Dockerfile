# 多阶段构建 - Gateway Service
# Stage 1: Maven 构建
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

# 复制 pom.xml 和源码
COPY pom.xml .
COPY openidaas-common/pom.xml openidaas-common/
COPY openidaas-gateway/pom.xml openidaas-gateway/

# 下载依赖（利用 Docker 缓存）
RUN mvn dependency:go-offline -B -q

# 复制所有源码
COPY openidaas-common/src openidaas-common/src
COPY openidaas-gateway/src openidaas-gateway/src

# 构建项目
RUN mvn clean package -DskipTests -pl openidaas-gateway -am -B

# Stage 2: 运行时镜像
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="QooBot <support@qoobot.com>"
LABEL description="OpenIDaaS Gateway Service"

WORKDIR /app

# 创建非 root 用户
RUN addgroup -S openidaas && adduser -S openidaas -G openidaas

# 从构建阶段复制 jar 包
COPY --from=builder /build/openidaas-gateway/target/openidaas-gateway-*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs && chown -R openidaas:openidaas /app

# 切换用户
USER openidaas

# JVM 优化参数
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+UnlockExperimentalVMOptions \
               -XX:+UseStringDeduplication \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=prod"

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
