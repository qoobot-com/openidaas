# Spring Cloud Alibaba 集成指南

## 目录
1. [概述](#概述)
2. [Nacos 服务发现与配置中心](#nacos-服务发现与配置中心)
3. [Sentinel 流量控制与熔断降级](#sentinel-流量控制与熔断降级)
4. [最佳实践](#最佳实践)
5. [故障排查](#故障排查)

---

## 概述

OpenIDaaS 项目全面集成 Spring Cloud Alibaba 生态系统，提供：
- **Nacos**: 服务注册发现、配置管理、动态 DNS 服务
- **Sentinel**: 流量控制、熔断降级、系统负载保护

### 技术架构

```
┌─────────────────────────────────────────────────────────┐
│                     API Gateway                          │
│              (Spring Cloud Gateway + Sentinel)            │
└──────────────────────┬──────────────────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
┌───────▼──────┐ ┌────▼─────┐ ┌─────▼──────┐
│ Auth Service │ │User Svc  │ │Org Service │
└───────┬──────┘ └────┬─────┘ └─────┬──────┘
        │             │             │
        └─────────────┼─────────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
┌───────▼──────┐ ┌────▼─────┐ ┌─────▼──────┐
│    Nacos     │ │ Sentinel │ │   Local DB │
│  (Discovery) │ │ (Control) │ │(Transaction)│
└──────────────┘ └──────────┘ └────────────┘
```

---

## Nacos 服务发现与配置中心

### 1. Nacos 简介

Nacos 是阿里巴巴开源的服务发现与配置管理平台，提供：
- 动态服务发现
- 服务健康检查
- 动态配置管理
- 服务和元数据管理

### 2. 安装 Nacos

#### 方式1: Docker 安装

```bash
# 拉取镜像
docker pull nacos/nacos-server:v2.2.3

# 单机模式启动
docker run -d \
  --name nacos \
  -e MODE=standalone \
  -p 8848:8848 \
  -p 9848:9848 \
  nacos/nacos-server:v2.2.3

# 访问控制台
http://localhost:8848/nacos
默认账号/密码: nacos/nacos
```

#### 方式2: 本地安装

```bash
# 下载安装包
wget https://github.com/alibaba/nacos/releases/download/2.2.3/nacos-server-2.2.3.zip

# 解压
unzip nacos-server-2.2.3.zip -d /opt/

# 启动
cd /opt/nacos/bin
./startup.sh -m standalone
```

### 3. Maven 依赖

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```

### 4. 服务注册配置

#### application.yml

```yaml
spring:
  application:
    name: user-service  # 服务名称
  
  cloud:
    nacos:
      discovery:
        # Nacos 服务器地址
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        # 命名空间
        namespace: ${NACOS_NAMESPACE:public}
        # 分组
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        # 集群名称
        cluster-name: ${NACOS_CLUSTER:default}
        # 权重
        weight: 1
        # 实例 IP
        ip: ${SERVER_IP:}
        # 实例端口
        port: ${SERVER_PORT:8081}
        # 是否开启
        enabled: true
        # 元数据
        metadata:
          version: 1.0.0
          region: ${REGION:local}
          preserved.register.source: SPRING_CLOUD
      
      config:
        # Nacos 配置中心地址
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        # 命名空间
        namespace: ${NACOS_NAMESPACE:public}
        # 分组
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        # 配置文件格式
        file-extension: yaml
        # 共享配置
        shared-configs:
          - data-id: common-config.yaml
            group: DEFAULT_GROUP
            refresh: true
        # 扩展配置
        extension-configs:
          - data-id: redis-config.yaml
            group: DEFAULT_GROUP
            refresh: true
```

### 5. 使用 Nacos 配置中心

#### 5.1 创建配置

在 Nacos 控制台创建配置：

**Data ID**: `user-service.yaml`  
**Group**: `DEFAULT_GROUP`  
**配置格式**: `YAML`

```yaml
# 用户服务配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/open_idaas
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root123}

app:
  user:
    max-login-attempts: 5
    lock-duration: 30
```

#### 5.2 动态刷新配置

使用 `@RefreshScope` 注解：

```java
@RestController
@RefreshScope
public class UserController {
    
    @Value("${app.user.max-login-attempts}")
    private Integer maxLoginAttempts;
    
    @GetMapping("/config/max-login-attempts")
    public Integer getMaxLoginAttempts() {
        return maxLoginAttempts;
    }
}
```

修改 Nacos 配置后，调用刷新接口：

```bash
curl -X POST http://localhost:8081/actuator/refresh
```

### 6. 服务调用

#### 使用 LoadBalancer

```java
@Service
public class UserService {
    
    @Autowired
    private LoadBalancerClient loadBalancerClient;
    
    @Autowired
    private RestTemplate restTemplate;
    
    public String callOrganizationService() {
        // 通过服务名调用
        ServiceInstance instance = loadBalancerClient.choose("organization-service");
        String url = instance.getUri() + "/api/organization/departments";
        return restTemplate.getForObject(url, String.class);
    }
}
```

#### 使用 OpenFeign

```java
@FeignClient(name = "organization-service")
public interface OrganizationClient {
    
    @GetMapping("/api/organization/departments")
    List<DepartmentDTO> getDepartments();
}

@Service
public class UserService {
    
    @Autowired
    private OrganizationClient organizationClient;
    
    public List<DepartmentDTO> getDepartments() {
        return organizationClient.getDepartments();
    }
}
```

---

## Sentinel 流量控制与熔断降级

### 1. Sentinel 简介

Sentinel 是阿里巴巴开源的流量控制与熔断降级组件，提供：
- 流量控制
- 熔断降级
- 系统负载保护
- 实时监控

### 2. 安装 Sentinel Dashboard

```bash
# 下载 JAR 包
wget https://github.com/alibaba/Sentinel/releases/download/1.8.6/sentinel-dashboard-1.8.6.jar

# 启动 Dashboard
java -Dserver.port=8858 -Dcsp.sentinel.dashboard.server=localhost:8858 \
     -Dproject.name=sentinel-dashboard \
     -jar sentinel-dashboard-1.8.6.jar

# 访问控制台
http://localhost:8858
默认账号/密码: sentinel/sentinel
```

### 3. Maven 依赖

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

### 4. Sentinel 配置

#### application.yml

```yaml
spring:
  cloud:
    sentinel:
      # 启用 Sentinel
      enabled: true
      # 传输配置
      transport:
        # Dashboard 地址
        dashboard: ${SENTINEL_DASHBOARD:localhost:8858}
        # 客户端端口
        port: 8719
        # 客户端 IP
        client-ip: ${SENTINEL_CLIENT_IP:}
      # 服务名称
      application: ${spring.application.name}
      # 心跳配置
      heartbeat:
        client-ip: ${SENTINEL_CLIENT_IP:}
      # 数据源
      datasource:
        # 文件数据源
        file:
          enabled: true
          file: classpath:sentinel/rules.json
          rule-type: flow
        # Nacos 数据源
        nacos:
          enabled: true
          server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
          namespace: ${NACOS_NAMESPACE:public}
          group-id: SENTINEL_GROUP
          rule-type: flow
          data-id: ${spring.application.name}-flow-rules
```

### 5. 流控规则

#### 5.1 注解方式

```java
@RestController
public class UserController {
    
    @GetMapping("/users/{id}")
    @SentinelResource(
        value = "getUserById",
        blockHandler = "handleGetUserBlock",
        fallback = "handleGetUserFallback"
    )
    public UserDTO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }
    
    // 流控处理
    public UserDTO handleGetUserBlock(Long id, BlockException ex) {
        return UserDTO.builder()
            .id(id)
            .username("Blocked")
            .message("请求被限流: " + ex.getClass().getSimpleName())
            .build();
    }
    
    // 降级处理
    public UserDTO handleGetUserFallback(Long id, Throwable ex) {
        return UserDTO.builder()
            .id(id)
            .username("Fallback")
            .message("服务降级: " + ex.getMessage())
            .build();
    }
}
```

#### 5.2 规则定义

```java
@Configuration
public class SentinelConfig {
    
    @PostConstruct
    public void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();
        
        // 用户查询流控规则
        FlowRule userQueryRule = new FlowRule();
        userQueryRule.setResource("getUserById");
        userQueryRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        userQueryRule.setCount(100);  // QPS 阈值
        userQueryRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        userQueryRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        rules.add(userQueryRule);
        
        // 用户创建流控规则
        FlowRule userCreateRule = new FlowRule();
        userCreateRule.setResource("createUser");
        userCreateRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        userCreateRule.setCount(10);  // QPS 阈值
        rules.add(userCreateRule);
        
        FlowRuleManager.loadRules(rules);
    }
}
```

### 6. 降级规则

```java
@PostConstruct
public void initDegradeRules() {
    List<DegradeRule> rules = new ArrayList<>();
    
    // 慢调用比例降级
    DegradeRule slowCallRule = new DegradeRule();
    slowCallRule.setResource("getUserById");
    slowCallRule.setGrade(RuleConstant.DEGRADE_GRADE_SLOW_REQUEST_RATIO);
    slowCallRule.setCount(500);  // 慢调用阈值(ms)
    slowCallRule.setTimeWindow(10);  // 降级时间窗口(s)
    slowCallRule.setMinRequestAmount(5);  // 最小请求数
    slowCallRule.setSlowRatioThreshold(0.5);  // 慢调用比例
    rules.add(slowCallRule);
    
    // 异常比例降级
    DegradeRule exceptionRatioRule = new DegradeRule();
    exceptionRatioRule.setResource("createUser");
    exceptionRatioRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
    exceptionRatioRule.setCount(0.5);  // 异常比例
    exceptionRatioRule.setTimeWindow(10);
    exceptionRatioRule.setMinRequestAmount(5);
    rules.add(exceptionRatioRule);
    
    DegradeRuleManager.loadRules(rules);
}
```

### 7. 网关流控

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - name: RequestRateLimiter
              args:
                # 使用 Redis 限流
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
                # 限流 Key
                key-resolver: "#{@userKeyResolver}"

    sentinel:
      scg:
        enabled: true
        order: 100
      filter:
        enabled: true
```

---

## 最佳实践

### 1. 服务注册

- ✅ 服务名称使用小写字母和连字符
- ✅ 合理设置权重和集群
- ✅ 生产环境使用命名空间隔离
- ✅ 定期检查服务健康状态

### 2. 流量控制

- ✅ 关键接口设置流控规则
- ✅ 使用 Warm Up 预热保护
- ✅ 配置合理的降级策略
- ✅ 监控流控和降级效果

### 3. 分布式事务

- ✅ 尽量避免分布式事务
- ✅ 使用本地事务 + 消息队列实现最终一致性
- ✅ 跨服务操作使用Saga模式补偿
- ✅ 设置合理的超时时间

### 4. 配置管理

- ✅ 敏感信息使用环境变量
- ✅ 配置变更测试后再发布
- ✅ 使用命名空间隔离环境
- ✅ 配置版本管理

---

## 故障排查

### 1. 服务注册失败

**检查清单**:
- Nacos 服务是否正常
- 网络连接是否正常
- 配置是否正确
- 服务端口是否被占用

**排查命令**:
```bash
# 查看 Nacos 日志
tail -f /opt/nacos/logs/nacos.log

# 检查服务注册
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=user-service
```

### 2. 流控不生效

**检查清单**:
- Sentinel Dashboard 是否正常
- 应用是否连接到 Dashboard
- 规则是否正确配置
- 是否使用了正确的 Resource 名称

**排查命令**:
```bash
# 查看应用日志
grep Sentinel application.log

# 测试规则
curl http://localhost:8081/actuator/sentinel
```

### 3. 分布式事务回滚失败

**检查清单**:
- 本地事务配置是否正确
- 消息队列是否正常
- 补偿机制是否完善

**排查命令**:
```bash
# 查看应用日志
grep Transaction application.log

# 检查消息队列状态
# 根据实际使用的MQ类型查询
```

---

## 参考资料

- [Nacos 官方文档](https://nacos.io/zh-cn/)
- [Sentinel 官方文档](https://sentinelguard.io/zh-cn/)
- [Spring Cloud Alibaba 文档](https://spring-cloud-alibaba-group.github.io/github-pages/zh-cn/)

---

## 附录

### 端口占用表

| 服务 | 默认端口 | 说明 |
|-----|---------|------|
| Nacos | 8848 | 控制台 HTTP |
| Nacos | 9848 | gRPC |
| Sentinel | 8858 | Dashboard |
| Gateway | 8080 | 网关 |
| Auth Service | 8082 | 认证服务 |
| User Service | 8081 | 用户服务 |
| Organization Service | 8084 | 组织服务 |

### 环境变量清单

| 变量名 | 说明 | 默认值 |
|-------|------|--------|
| NACOS_SERVER_ADDR | Nacos 地址 | localhost:8848 |
| NACOS_NAMESPACE | 命名空间 | public |
| NACOS_GROUP | 分组 | DEFAULT_GROUP |
| SENTINEL_DASHBOARD | Sentinel 地址 | localhost:8858 |
