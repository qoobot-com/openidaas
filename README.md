# OpenIDaaS 企业级身份认证即服务系统

## 项目简介

OpenIDaaS是一个基于SpringBoot 3.5.10 + Vue3 + MySQL构建的企业级身份认证和授权管理系统。系统采用微服务架构设计，提供统一的身份认证、权限管理、组织架构管理等功能。

## 系统架构

### 技术栈
- **后端**: SpringBoot 3.5.10, Spring Cloud 2023.x, JDK 21
- **前端**: Vue 3.4, Element Plus, TypeScript
- **数据库**: MySQL 8.0, Redis 7.0
- **安全**: Spring Security 6.x, OAuth2.1, JWT
- **部署**: Docker, Kubernetes

### 微服务架构

```
openidaas/
├── openidaas-common/              # 公共模块
├── openidaas-core/                # 核心领域模块
├── openidaas-auth-service/        # 认证服务 (8081)
├── openidaas-user-service/        # 用户服务 (8082)
├── openidaas-organization-service/ # 组织服务 (8083)
├── openidaas-role-service/        # 角色权限服务 (8084)
├── openidaas-application-service/ # 应用管理服务 (8085)
├── openidaas-authorization-service/ # 授权服务 (8086)
├── openidaas-audit-service/       # 审计服务 (8087)
├── openidaas-gateway/             # API网关 (8080)
└── openidaas-admin-ui/            # 管理前端界面
```

## 功能特性

### 核心功能
- ✅ 统一身份认证 (Username/Password, MFA, Social Login)
- ✅ 基于RBAC/ABAC的权限管理
- ✅ 组织架构管理
- ✅ OAuth2.0/OpenID Connect支持
- ✅ SAML 2.0集成
- ✅ 审计日志和安全监控
- ✅ 多租户支持
- ✅ API网关和流量控制

### 安全特性
- 🔒 JWT Token认证
- 🔒 多因子认证(MFA)
- 🔒 密码策略和加密
- 🔒 访问控制和权限验证
- 🔒 安全审计和日志
- 🔒 GDPR合规支持

## 快速开始

### 环境要求
- JDK 17+
- MySQL 8.0+
- Redis 7.0+
- Node.js 18+ (前端开发)
- Maven 3.8+
- Nacos Server 2.2.3+
- Sentinel Dashboard 1.8.6+

### 数据库初始化

1. 创建数据库:
```sql
CREATE DATABASE open_idaas CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行数据库脚本:
```bash
mysql -u root -p open_idaas < db/schema.sql
```

### 后端服务启动

1. 编译项目:
```bash
mvn clean install
```

2. 启动各服务:
```bash
# 启动网关服务
cd openidaas-gateway
mvn spring-boot:run

# 启动认证服务
cd ../openidaas-auth-service
mvn spring-boot:run

# 启动用户服务
cd ../openidaas-user-service
mvn spring-boot:run

# 启动组织服务
cd ../openidaas-organization-service
mvn spring-boot:run
```

### 前端启动

```bash
cd openidaas-admin-ui
npm install
npm run dev
```

## API文档

系统提供完整的OpenAPI 3.0规范文档，可通过以下方式访问:

- Swagger UI: http://localhost:8080/swagger-ui.html
- API文档: http://localhost:8080/v3/api-docs

## 配置说明

### 环境配置文件

各服务均支持多环境配置:
- `application.yml` - 默认配置
- `application-dev.yml` - 开发环境
- `application-prod.yml` - 生产环境

### 重要配置项

```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/open_idaas
    username: your_username
    password: your_password

# Redis配置
  redis:
    host: localhost
    port: 6379

# JWT配置
app:
  jwt:
    secret: your_jwt_secret_key
    expiration: 3600000
```

## 部署指南

详细的部署指南请参考: [docs/DEPLOYMENT_GUIDE.md](docs/DEPLOYMENT_GUIDE.md)

### Docker部署

```bash
# 构建镜像
docker-compose build

# 启动服务
docker-compose up -d
```

### Kubernetes部署

```bash
# 部署到K8s集群
kubectl apply -f k8s/
```

## 监控和运维

### 健康检查
- 服务健康: http://localhost:8080/actuator/health
- 指标监控: http://localhost:8080/actuator/metrics

### Spring Cloud Alibaba监控
- Nacos服务列表: http://localhost:8848/nacos
- Sentinel实时监控: http://localhost:8080

### 日志管理
系统使用Logback进行日志管理，支持:
- 结构化日志输出
- 日志级别动态调整
- 日志文件滚动和归档

### 查看服务日志
```bash
# 查看各服务日志
tail -f gateway.log
tail -f auth-service.log
tail -f user-service.log
```

## 开发指南

详细的开发指南请参考: [docs/DEVELOPER_GUIDE.md](docs/DEVELOPER_GUIDE.md)

### 代码规范
- 遵循阿里巴巴Java开发手册
- 使用Lombok简化代码
- 统一异常处理机制
- 完整的单元测试覆盖

### 目录结构约定
```
src/main/java/com/qoobot/openidaas/
├── controller/     # 控制器层
├── service/        # 业务逻辑层
├── repository/     # 数据访问层
├── entity/         # 实体类
├── dto/            # 数据传输对象
└── config/         # 配置类
```

## 贡献指南

欢迎提交Issue和Pull Request来改进项目。

### 开发流程
1. Fork项目
2. 创建功能分支
3. 提交更改
4. 发起Pull Request

## Spring Cloud Alibaba集成

本项目已全面集成Spring Cloud Alibaba生态系统：

- **Nacos**: 服务发现与配置管理
- **Sentinel**: 流量控制与熔断降级

详细集成指南请参考: [docs/SPRING_CLOUD_ALIBABA_GUIDE.md](docs/SPRING_CLOUD_ALIBABA_GUIDE.md)

## 核心服务介绍

### 认证服务 (openidaas-auth-service)
- 端口: 8081
- 功能: 用户认证、JWT令牌管理、OAuth2.0支持

### 用户服务 (openidaas-user-service)
- 端口: 8082
- 功能: 用户管理、个人信息维护、用户状态管理

### 组织服务 (openidaas-organization-service)
- 端口: 8083
- 功能: 组织架构管理、部门管理、组织树结构维护

### 网关服务 (openidaas-gateway)
- 端口: 8080
- 功能: API网关、路由转发、统一认证、流量控制

## 许可证

本项目采用Apache License 2.0许可证。

## 联系方式

- 项目主页: https://github.com/qoobot-com/openidaas
- 邮箱: dev@qoobot.com
- 文档: https://docs.qoobot.com/openidaas
- Spring Cloud Alibaba指南: [docs/SPRING_CLOUD_ALIBABA_GUIDE.md](docs/SPRING_CLOUD_ALIBABA_GUIDE.md)
- 部署指南: [docs/DEPLOYMENT_GUIDE.md](docs/DEPLOYMENT_GUIDE.md)
- 开发者指南: [docs/DEVELOPER_GUIDE.md](docs/DEVELOPER_GUIDE.md)
- 数据库安全指南: [docs/DATABASE_SECURITY.md](docs/DATABASE_SECURITY.md)
- Sentinel指南: [docs/SENTINEL_GUIDE.md](docs/SENTINEL_GUIDE.md)
- Feign使用指南: [docs/FEIGN_USAGE.md](docs/FEIGN_USAGE.md)
- 测试指南: [docs/TEST_GUIDE.md](docs/TEST_GUIDE.md)

---
© 2024 Qoobot Team. All rights reserved.