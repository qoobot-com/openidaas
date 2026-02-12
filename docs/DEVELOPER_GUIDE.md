# OpenIDaaS 开发者指南

## 目录
1. [开发环境搭建](#开发环境搭建)
2. [项目结构](#项目结构)
3. [开发规范](#开发规范)
4. [代码开发](#代码开发)
5. [测试指南](#测试指南)
6. [调试技巧](#调试技巧)
7. [贡献流程](#贡献流程)

---

## 开发环境搭建

### 1. 安装开发工具

#### IDE 推荐

- **IntelliJ IDEA** (推荐)
- **Visual Studio Code**
- **Eclipse**

#### 必装插件

- **Lombok Plugin**
- **MyBatis Plugin**
- **Maven Helper**
- **Rainbow Brackets**
- **String Manipulation**

### 2. 安装开发依赖

#### 后端开发

```bash
# 安装 JDK 17
brew install openjdk@17  # macOS
sudo apt install openjdk-17-jdk  # Ubuntu

# 安装 Maven
brew install maven  # macOS
sudo apt install maven  # Ubuntu

# 安装 MySQL
brew install mysql  # macOS
sudo apt install mysql-server  # Ubuntu

# 安装 Redis
brew install redis  # macOS
sudo apt install redis-server  # Ubuntu
```

#### 前端开发

```bash
# 安装 Node.js 18+
brew install node  # macOS
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs  # Ubuntu

# 安装 pnpm（推荐）
npm install -g pnpm

# 安装依赖
cd openidaas-admin-ui
pnpm install
```

### 3. 配置 IDE

#### IntelliJ IDEA 配置

1. **安装 Lombok 插件**
   - File → Settings → Plugins
   - 搜索 Lombok 并安装
   - 重启 IDEA

2. **配置 JDK**
   - File → Project Structure → Project
   - 设置 Project SDK 为 JDK 17

3. **配置 Maven**
   - File → Settings → Build, Execution, Deployment → Build Tools → Maven
   - 设置 Maven home directory

4. **配置代码格式**
   - File → Settings → Editor → Code Style → Java
   - 导入阿里巴巴 Java 代码规范

#### VS Code 配置

```json
{
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-17",
      "path": "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"
    }
  ],
  "java.format.settings.url": "https://raw.githubusercontent.com/alibaba/p3c/master/p3c-formatter/eclipse-p3c-formatter.xml"
}
```

### 4. 克隆项目

```bash
# 克隆仓库
git clone https://github.com/qoobot-com/openidaas.git
cd openidaas

# 配置 Git
git config user.name "Your Name"
git config user.email "your.email@example.com"
```

### 5. 初始化数据库

```bash
# 启动 MySQL
brew services start mysql

# 创建数据库
mysql -u root -p -e "CREATE DATABASE open_idaas CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 执行初始化脚本
mysql -u root -p open_idaas < db/schema.sql
mysql -u root -p open_idaas < db/init_data.sql
```

### 6. 启动基础设施

```bash
# 启动 Redis
brew services start redis

# 启动 Nacos
cd /opt/nacos/bin
./startup.sh -m standalone

# 启动 Sentinel Dashboard
java -jar sentinel-dashboard-1.8.6.jar &
```

### 7. 编译项目

```bash
# 清理并编译
mvn clean install -DskipTests

# 或跳过所有检查
mvn clean install -DskipTests -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true
```

### 8. 运行项目

#### 后端服务

```bash
# 方式1: 使用 Maven
cd openidaas-gateway
mvn spring-boot:run

# 方式2: 使用 IDEA
# 右键点击 GatewayApplication → Run 'GatewayApplication'
```

#### 前端服务

```bash
cd openidaas-admin-ui
pnpm dev
```

---

## 项目结构

### 后端项目结构

```
openidaas/
├── openidaas-common/                    # 公共模块
│   ├── src/main/java/
│   │   ├── entity/                      # 公共实体
│   │   ├── vo/                          # 视图对象
│   │   ├── dto/                         # 数据传输对象
│   │   ├── enums/                       # 枚举类
│   │   ├── exception/                   # 异常类
│   │   ├── util/                        # 工具类
│   │   ├── constant/                    # 常量
│   │   ├── config/                      # 公共配置
│   │   ├── feign/                       # Feign 配置
│   │   └── sentinel/                    # Sentinel 配置
│   └── src/main/resources/
│       └── mapper/                      # MyBatis Mapper XML
├── openidaas-core/                      # 核心模块
│   ├── entity/                          # 领域实体
│   ├── repository/                      # 数据访问接口
│   └── service/                         # 核心服务
├── openidaas-gateway/                   # 网关服务
│   └── src/main/java/
│       ├── controller/                  # 控制器
│       ├── filter/                      # 过滤器
│       └── config/                      # 网关配置
├── openidaas-auth-service/              # 认证服务
│   ├── controller/                      # 认证控制器
│   ├── service/                         # 认证服务
│   ├── config/                          # 认证配置
│   └── mapper/                          # 数据访问
├── openidaas-user-service/              # 用户服务
│   ├── controller/                      # 用户控制器
│   ├── service/                         # 用户服务
│   └── mapper/                          # 数据访问
├── openidaas-organization-service/      # 组织服务
├── openidaas-role-service/              # 角色服务
├── openidaas-application-service/      # 应用服务
├── openidaas-authorization-service/      # 授权服务
└── openidaas-audit-service/              # 审计服务
```

### 前端项目结构

```
openidaas-admin-ui/
├── public/                              # 静态资源
├── src/
│   ├── api/                             # API 接口
│   ├── assets/                          # 资源文件
│   ├── components/                      # 公共组件
│   ├── composables/                     # 组合式函数
│   ├── layouts/                         # 布局组件
│   ├── router/                          # 路由配置
│   ├── stores/                          # 状态管理
│   ├── types/                           # TypeScript 类型
│   ├── utils/                           # 工具函数
│   ├── views/                           # 页面视图
│   │   ├── auth/                        # 认证相关
│   │   ├── user/                        # 用户管理
│   │   ├── organization/                # 组织管理
│   │   ├── role/                        # 角色管理
│   │   ├── application/                 # 应用管理
│   │   └── system/                      # 系统设置
│   ├── App.vue                          # 根组件
│   └── main.ts                          # 入口文件
├── package.json                         # 项目配置
├── tsconfig.json                        # TS 配置
├── vite.config.ts                       # Vite 配置
└── tailwind.config.js                   # Tailwind 配置
```

### 目录结构约定

#### Controller 层

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    // 控制器逻辑
}
```

#### Service 层

```java
public interface UserService {
    // 服务接口
}

@Service
public class UserServiceImpl implements UserService {
    // 服务实现
}
```

#### Mapper 层

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // MyBatis Mapper
}
```

---

## 开发规范

### 1. 命名规范

#### 类命名

- **Entity**: 使用业务名词，如 `User`、`Department`
- **DTO**: 使用业务名词 + DTO，如 `UserDTO`、`DepartmentDTO`
- **VO**: 使用业务名词 + VO，如 `UserVO`、`DepartmentVO`
- **Controller**: 使用业务名词 + Controller，如 `UserController`
- **Service**: 使用业务名词 + Service，如 `UserService`
- **Mapper**: 使用业务名词 + Mapper，如 `UserMapper`
- **Util**: 使用功能 + Util，如 `PasswordUtil`、`JwtUtil`

#### 方法命名

- **查询**: `get`、`list`、`query`、`search`、`find`
- **创建**: `create`、`add`、`insert`、`save`
- **更新**: `update`、`modify`、`edit`
- **删除**: `delete`、`remove`
- **判断**: `is`、`has`、`can`、`should`

#### 变量命名

- 使用驼峰命名法
- 避免使用缩写
- 使用有意义的名称

```java
// ✅ 推荐
String userName;
Integer maxRetryCount;
boolean isActive;

// ❌ 不推荐
String un;
Integer mrc;
boolean f;
```

### 2. 代码规范

#### 使用 Lombok

```java
// ✅ 推荐
@Data
@Entity
@Table(name = "users")
public class User {
    private Long id;
    private String username;
    private String password;
}

// ❌ 不推荐
@Entity
@Table(name = "users")
public class User {
    private Long id;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    // ...
}
```

#### 异常处理

```java
// ✅ 推荐
try {
    userService.createUser(userDTO);
} catch (UserAlreadyExistsException e) {
    log.error("User already exists: {}", userDTO.getUsername());
    throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
}

// ❌ 不推荐
try {
    userService.createUser(userDTO);
} catch (Exception e) {
    e.printStackTrace();
}
```

#### 日志规范

```java
// ✅ 推荐
log.info("Creating user: {}", userDTO.getUsername());
log.warn("User login failed: {}", username);
log.error("Failed to create user", e);

// ❌ 不推荐
System.out.println("Creating user");
log.info("Creating user " + userDTO.getUsername());
```

### 3. 注释规范

#### 类注释

```java
/**
 * 用户服务实现
 *
 * <p>提供用户管理的核心功能，包括用户创建、查询、更新、删除等操作
 *
 * @author QooBot
 * @since 1.0.0
 */
@Service
public class UserServiceImpl implements UserService {
    // ...
}
```

#### 方法注释

```java
/**
 * 根据用户 ID 查询用户信息
 *
 * @param userId 用户 ID
 * @return 用户信息，如果不存在则返回 null
 * @throws IllegalArgumentException 如果 userId 为 null 或小于等于 0
 */
public UserDTO getUserById(Long userId) {
    // ...
}
```

#### 字段注释

```java
@Data
public class UserDTO {
    /** 用户 ID */
    private Long id;

    /** 用户名，唯一 */
    private String username;

    /** 密码（加密后） */
    private String password;
}
```

### 4. 数据库规范

#### 表命名

- 使用小写字母和下划线
- 使用复数形式
- 添加业务前缀

```sql
-- ✅ 推荐
CREATE TABLE users (...)
CREATE TABLE user_roles (...)
CREATE TABLE user_departments (...)

-- ❌ 不推荐
CREATE TABLE User (...)
CREATE TABLE userRole (...)
```

#### 字段命名

- 使用小写字母和下划线
- 使用有意义的名称
- 添加适当的前缀

```sql
-- ✅ 推荐
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_name VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ❌ 不推荐
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    createTime DATETIME
);
```

---

## 代码开发

### 1. 创建新功能

#### 步骤1: 创建实体类

```java
@Data
@Entity
@Table(name = "permissions")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;
}
```

#### 步骤2: 创建 Mapper

```java
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
    // 自定义查询方法
    @Select("SELECT * FROM permissions WHERE code = #{code}")
    Permission findByCode(String code);
}
```

#### 步骤3: 创建 DTO

```java
@Data
public class PermissionDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
}
```

#### 步骤4: 创建 Service

```java
public interface PermissionService {
    PermissionDTO createPermission(PermissionDTO dto);
    PermissionDTO getPermissionById(Long id);
    List<PermissionDTO> listPermissions();
    void deletePermission(Long id);
}

@Service
public class PermissionServiceImpl implements PermissionService {
    
    @Autowired
    private PermissionMapper permissionMapper;
    
    @Override
    @Transactional
    public PermissionDTO createPermission(PermissionDTO dto) {
        Permission permission = PermissionConverter.toEntity(dto);
        permissionMapper.insert(permission);
        return PermissionConverter.toDTO(permission);
    }

    @Override
    public PermissionDTO getPermissionById(Long id) {
        Permission permission = permissionMapper.selectById(id);
        return PermissionConverter.toDTO(permission);
    }

    @Override
    public List<PermissionDTO> listPermissions() {
        List<Permission> permissions = permissionMapper.selectList(null);
        return permissions.stream()
            .map(PermissionConverter::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        permissionMapper.deleteById(id);
    }
}
```

#### 步骤5: 创建 Controller

```java
@RestController
@RequestMapping("/api/permissions")
@Tag(name = "权限管理", description = "权限相关接口")
public class PermissionController {
    
    @Autowired
    private PermissionService permissionService;
    
    @PostMapping
    @Operation(summary = "创建权限")
    @SentinelResource(value = "createPermission", blockHandler = "handleBlock")
    public Result<PermissionDTO> createPermission(@RequestBody @Valid PermissionDTO dto) {
        PermissionDTO result = permissionService.createPermission(dto);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询权限")
    @SentinelResource(value = "getPermission", blockHandler = "handleBlock")
    public Result<PermissionDTO> getPermission(@PathVariable Long id) {
        PermissionDTO result = permissionService.getPermissionById(id);
        return Result.success(result);
    }

    @GetMapping
    @Operation(summary = "查询权限列表")
    @SentinelResource(value = "listPermissions", blockHandler = "handleBlock")
    public Result<List<PermissionDTO>> listPermissions() {
        List<PermissionDTO> result = permissionService.listPermissions();
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除权限")
    @SentinelResource(value = "deletePermission", blockHandler = "handleBlock")
    public Result<Void> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return Result.success();
    }

    // 流控处理
    public Result<?> handleBlock(String resourceName, BlockException ex) {
        return Result.error(429, "系统繁忙，请稍后重试");
    }
}
```

### 2. 使用 Feign 调用服务

```java
// 定义 Feign 客户端
@FeignClient(name = "user-service")
public interface UserClient {
    
    @GetMapping("/api/users/{id}")
    Result<UserDTO> getUserById(@PathVariable("id") Long id);

    @PostMapping("/api/users")
    Result<UserDTO> createUser(@RequestBody UserDTO dto);
}

// 使用 Feign 客户端
@Service
public class OrganizationServiceImpl implements OrganizationService {
    
    @Autowired
    private UserClient userClient;

    @Override
    public void addUserToDepartment(Long userId, Long departmentId) {
        // 调用用户服务
        Result<UserDTO> result = userClient.getUserById(userId);
        if (result.isSuccess()) {
            UserDTO user = result.getData();
            // 处理业务逻辑
        }
    }
}
```

### 3. 使用 OpenFeign Helper

```java
@Service
public class OrganizationServiceImpl implements OrganizationService {
    
    @Autowired
    private UserClient userClient;

    @Override
    public UserDTO getUserSafe(Long userId) {
        // 安全调用，自动处理异常
        return FeignHelper.safeCall(() -> userClient.getUserById(userId))
            .map(Result::getData)
            .orElse(null);
    }

    @Override
    public CompletableFuture<UserDTO> getUserAsync(Long userId) {
        // 异步调用
        return FeignHelper.asyncCall(() -> userClient.getUserById(userId))
            .thenApply(Result::getData);
    }

    @Override
    public UserDTO getUserWithRetry(Long userId) {
        // 带重试的调用
        return FeignHelper.callWithRetry(() -> userClient.getUserById(userId), 3)
            .map(Result::getData)
            .orElse(null);
    }
}
```

---

## 测试指南

### 1. 单元测试

```java
@SpringBootTest
class UserServiceTest {
    
    @Autowired
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @Test
    void testCreateUser() {
        // Arrange
        UserDTO dto = new UserDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");

        // Act
        UserDTO result = userService.createUser(dto);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }
}
```

### 2. 集成测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetUser() throws Exception {
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.username").value("admin"));
    }
}
```

### 3. 运行测试

```bash
# 运行所有测试
mvn test

# 运行指定测试类
mvn test -Dtest=UserServiceTest

# 运行指定测试方法
mvn test -Dtest=UserServiceTest#testCreateUser

# 跳过测试
mvn clean install -DskipTests
```

---

## 调试技巧

### 1. 远程调试

```bash
# 启动服务时添加调试参数
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
  -jar openidaas-user-service.jar

# IDEA 连接
Run → Edit Configurations → Remote → Add → 配置 Host: localhost, Port: 5005
```

### 2. 日志调试

```yaml
# application-dev.yml
logging:
  level:
    com.qoobot.openidaas: DEBUG
    com.qoobot.openidaas.user: TRACE
```

### 3. Actuator 端点

```bash
# 查看所有端点
curl http://localhost:8081/actuator

# 查看环境变量
curl http://localhost:8081/actuator/env

# 查看配置属性
curl http://localhost:8081/actuator/configprops

# 查看 Beans
curl http://localhost:8081/actuator/beans

# 查看日志级别
curl http://localhost:8081/actuator/loggers
```

---

## 贡献流程

### 1. Fork 项目

```bash
# Fork 项目到自己的账号
# 克隆 Fork 后的仓库
git clone https://github.com/your-username/openidaas.git
cd openidaas
```

### 2. 创建分支

```bash
# 创建功能分支
git checkout -b feature/new-feature

# 或创建修复分支
git checkout -b fix/bug-fix
```

### 3. 提交代码

```bash
# 添加文件
git add .

# 提交代码
git commit -m "feat: add user profile feature"

# 或
git commit -m "fix: fix login bug"
```

### 4. 推送代码

```bash
# 推送到远程仓库
git push origin feature/new-feature
```

### 5. 发起 Pull Request

- 在 GitHub 上发起 Pull Request
- 填写 PR 模板
- 等待 Code Review

---

## 参考资料

- [阿里巴巴 Java 开发手册](https://github.com/alibaba/p3c)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Spring Cloud Alibaba 文档](https://spring-cloud-alibaba-group.github.io/github-pages/)
- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [Vue 3 官方文档](https://vuejs.org/)
