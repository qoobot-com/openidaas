# 多阶段构建 - User Service
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
COPY openidaas-common/pom.xml openidaas-common/
COPY openidaas-core/pom.xml openidaas-core/
COPY openidaas-user-service/pom.xml openidaas-user-service/

RUN mvn dependency:go-offline -B -q

COPY openidaas-common/src openidaas-common/src
COPY openidaas-core/src openidaas-core/src
COPY openidaas-user-service/src openidaas-user-service/src

RUN mvn clean package -DskipTests -pl openidaas-user-service -am -B

FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="QooBot <support@qoobot.com>"
LABEL description="OpenIDaaS User Service"

WORKDIR /app

RUN addgroup -S openidaas && adduser -S openidaas -G openidaas

COPY --from=builder /build/openidaas-user-service/target/openidaas-user-service-*.jar app.jar

RUN mkdir -p /app/logs && chown -R openidaas:openidaas /app

USER openidaas

ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=prod"

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
