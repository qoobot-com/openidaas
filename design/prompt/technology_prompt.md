# IDaaS系统技术架构设计提示词

## 技术架构总体要求

作为资深架构师，请为基于SpringBoot 3.5.10 + JDK 21 + Vue3 + MySQL的技术栈，设计一个现代化、高可用、易扩展的IDaaS系统技术架构。架构需满足企业级应用的性能、安全、稳定性要求，并具备良好的可观测性和运维友好性。

## 后端技术架构设计

### 核心框架选型

**Spring生态技术栈：**
- Spring Boot 3.5.10（JDK 21支持）
- Spring Security 6.x（新一代安全框架）
- Spring Authorization Server（OAuth2.1/OIDC标准实现）
- Spring Data JPA + MyBatis-Plus（双ORM模式）
- Spring Cloud 2023.x（微服务治理）

**基础设施组件：**
```xml
<!-- 核心依赖版本管理 -->
<properties>
    <java.version>21</java.version>
    <spring-boot.version>3.5.10</spring-boot.version>
    <spring-cloud.version>2023.0.0</spring-cloud.version>
    <spring-authorization-server.version>1.2.0</spring-authorization-server.version>
    <mybatis-plus.version>3.5.7</mybatis-plus.version>
    <mysql-connector.version>8.0.33</mysql-connector.version>
</properties>
```

### 微服务治理架构

**服务注册发现：**
- Nacos作为注册中心和服务配置中心
- 支持服务健康检查和自动故障转移
- 多环境配置管理（dev/test/prod）

**API网关层：**
```yaml
# Spring Cloud Gateway配置示例
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://openidaas-auth-service
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=2
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
```

**服务调用治理：**
- OpenFeign声明式HTTP客户端
- Resilience4j熔断降级
- LoadBalancer负载均衡
- Micrometer指标监控

### 安全技术架构

**认证授权体系：**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
```

**密码安全策略：**
- BCrypt强哈希算法（强度12）
- Argon2id现代密码哈希
- 密码复杂度实时校验
- 历史密码防重用机制

**数据安全防护：**
```java
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    @Convert(converter = AesEncryptConverter.class)  // 字段级加密
    private String mobile;
    
    // 敏感信息脱敏
    public String getMaskedMobile() {
        if (mobile == null) return null;
        return mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }
}
```

### 缓存架构设计

**多级缓存策略：**
```java
@Service
public class UserService {
    
    @Cacheable(value = "user", key = "#userId", unless = "#result == null")
    public User findById(Long userId) {
        return userMapper.selectById(userId);
    }
    
    @CacheEvict(value = "user", key = "#user.id")
    @Transactional
    public void updateUser(User user) {
        userMapper.updateById(user);
    }
}
```

**Redis缓存配置：**
```yaml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 2
    timeout: 2000ms
    
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1小时
      cache-null-values: false
```

### 消息队列架构

**Kafka事件驱动：**
```java
@Component
public class UserEventListener {
    
    @KafkaListener(topics = "user-events", groupId = "idaas-group")
    public void handleUserEvent(ConsumerRecord<String, String> record) {
        try {
            UserEvent event = objectMapper.readValue(record.value(), UserEvent.class);
            switch (event.getEventType()) {
                case USER_CREATED -> handleUserCreated(event);
                case USER_UPDATED -> handleUserUpdated(event);
                case USER_DELETED -> handleUserDeleted(event);
            }
        } catch (Exception e) {
            log.error("处理用户事件失败", e);
        }
    }
}
```

### 数据库连接池优化

**HikariCP配置：**
```yaml
spring:
  datasource:
    hikari:
      pool-name: IDaaS-HikariCP
      minimum-idle: 10
      maximum-pool-size: 50
      idle-timeout: 300000
      max-lifetime: 1800000
      connection-timeout: 30000
      leak-detection-threshold: 60000
```

## 前端技术架构设计

### Vue3核心架构

**项目结构规划：**
```
src/
├── api/                 # API接口层
├── assets/             # 静态资源
├── components/         # 通用组件
├── composables/        # Composition API逻辑复用
├── layouts/            # 页面布局
├── locales/            # 国际化配置
├── modules/            # 业务模块
├── pages/              # 页面组件
├── plugins/            # 插件配置
├── router/             # 路由配置
├── stores/             # 状态管理
├── styles/             # 样式文件
├── types/              # TypeScript类型定义
└── utils/              # 工具函数
```

**状态管理设计：**
```typescript
// stores/auth.ts
export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserInfo | null>(null)
  const token = ref<string | null>(localStorage.getItem('access_token'))
  
  const login = async (credentials: LoginCredentials) => {
    const response = await authApi.login(credentials)
    token.value = response.data.access_token
    localStorage.setItem('access_token', token.value)
    await fetchUserInfo()
  }
  
  const logout = () => {
    token.value = null
    user.value = null
    localStorage.removeItem('access_token')
  }
  
  return { user, token, login, logout, isAuthenticated: computed(() => !!token.value) }
})
```

### 前端安全架构

**请求拦截器：**
```typescript
// utils/request.ts
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000
})

service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('access_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

service.interceptors.response.use(
  response => response.data,
  async error => {
    if (error.response?.status === 401) {
      // Token过期处理
      localStorage.removeItem('access_token')
      router.push('/login')
    }
    return Promise.reject(error)
  }
)
```

**权限控制：**
```typescript
// composables/usePermission.ts
export function usePermission() {
  const authStore = useAuthStore()
  
  const hasPermission = (permission: string): boolean => {
    if (!authStore.user?.permissions) return false
    return authStore.user.permissions.includes(permission)
  }
  
  const hasRole = (role: string): boolean => {
    if (!authStore.user?.roles) return false
    return authStore.user.roles.includes(role)
  }
  
  return { hasPermission, hasRole }
}
```

## DevOps技术架构

### 容器化部署

**Dockerfile优化：**

```dockerfile
# 多阶段构建
FROM openjdk:21-jdk-slim AS builder
WORKDIR /app
COPY ../.. .
RUN ./mvnw clean package -DskipTests

FROM openjdk:21-jre-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# 创建非root用户
RUN addgroup --system appgroup && \
    adduser --system --group appuser && \
    chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Kubernetes部署配置：**
```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: openidaas-auth-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: auth-service
  template:
    metadata:
      labels:
        app: auth-service
    spec:
      containers:
      - name: auth-service
        image: registry.example.com/openidaas/auth-service:latest
        ports:
        - containerPort: 8080
        envFrom:
        - configMapRef:
            name: auth-service-config
        - secretRef:
            name: auth-service-secret
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
```

### 监控告警体系

**Prometheus指标采集：**
```java
@Component
public class CustomMetrics {
    
    private final Counter userLoginCounter;
    private final Timer authProcessingTimer;
    private final Gauge activeSessionsGauge;
    
    public CustomMetrics(MeterRegistry meterRegistry) {
        userLoginCounter = Counter.builder("user.login.count")
            .description("用户登录次数")
            .register(meterRegistry);
            
        authProcessingTimer = Timer.builder("auth.processing.time")
            .description("认证处理耗时")
            .register(meterRegistry);
            
        activeSessionsGauge = Gauge.builder("active.sessions")
            .description("活跃会话数")
            .register(meterRegistry);
    }
}
```

**日志架构设计：**
```yaml
# logback-spring.xml
<configuration>
    <springProfile name="default">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>
    
    <springProfile name="prod">
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>/var/log/openidaas/app.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>/var/log/openidaas/app.%d{yyyy-MM-dd}.%i.gz</fileNamePattern>
                <maxFileSize>100MB</maxFileSize>
                <maxHistory>30</maxHistory>
            </rollingPolicy>
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId}] %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        <root level="WARN">
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>
</configuration>
```

### CI/CD流水线设计

**GitHub Actions配置：**
```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
        
    - name: Cache Maven packages
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
        
    - name: Build with Maven
      run: mvn clean verify -U
      
    - name: SonarCloud Scan
      uses: SonarSource/sonarcloud-github-action@master
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        
    - name: Build Docker images
      run: |
        docker build -t ${{ secrets.REGISTRY }}/openidaas:${{ github.sha }} .
        
    - name: Push to Registry
      run: |
        echo ${{ secrets.REGISTRY_PASSWORD }} | docker login ${{ secrets.REGISTRY }} -u ${{ secrets.REGISTRY_USERNAME }} --password-stdin
        docker push ${{ secrets.REGISTRY }}/openidaas:${{ github.sha }}
```

## 性能优化架构

### JVM调优配置

**生产环境JVM参数：**
```bash
JAVA_OPTS="
-server
-Xms2g -Xmx4g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/var/log/openidaas/heapdump.hprof
-XX:+UseStringDeduplication
-XX:NativeMemoryTracking=summary
-Dfile.encoding=UTF-8
-Duser.timezone=Asia/Shanghai
"
```

### 数据库优化策略

**连接池监控：**
```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public Health health() {
        try {
            HikariDataSource hikariDS = (HikariDataSource) dataSource;
            HikariPoolMXBean poolBean = hikariDS.getHikariPoolMXBean();
            
            Map<String, Object> details = new HashMap<>();
            details.put("activeConnections", poolBean.getActiveConnections());
            details.put("idleConnections", poolBean.getIdleConnections());
            details.put("totalConnections", poolBean.getTotalConnections());
            details.put("threadsAwaitingConnection", poolBean.getThreadsAwaitingConnection());
            
            Status status = poolBean.getThreadsAwaitingConnection() > 10 ? 
                Status.DOWN : Status.UP;
                
            return Health.status(status).withDetails(details).build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
```

请基于以上技术架构要求，输出完整的技术架构设计方案，包括：
1. 技术选型论证与架构图
2. 核心组件详细设计
3. 性能优化具体实施方案
4. 安全加固技术措施
5. 运维监控完整方案