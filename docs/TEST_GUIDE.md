# 测试覆盖指南

## 概述

本文档说明如何为 OpenIDaaS 系统编写单元测试和集成测试，以达到 60% 以上的测试覆盖率。

## 测试框架

### 技术栈

- **JUnit 5** - 测试框架
- **Mockito** - Mock 框架
- **Spring Boot Test** - Spring 测试支持
- **H2 Database** - 内存数据库（测试用）
- **Testcontainers** - 集成测试容器化

## 测试类型

### 1. 单元测试 (Unit Tests)

测试单个类或方法的逻辑，使用 Mock 隔离外部依赖。

#### 文件位置
```
{service}/src/test/java/com/qoobot/openidaas/{module}/service/{Service}Test.java
```

#### 模板示例

```java
package com.qoobot.openidaas.{module}.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 服务单元测试模板
 */
@ExtendWith(MockitoExtension.class)
class ServiceTest {

    @Mock
    private DependencyMapper dependencyMapper;

    @InjectMocks
    private ServiceImpl service;

    private Entity testEntity;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testEntity = new Entity();
        testEntity.setId(1L);
        // ... 设置其他属性
    }

    @Test
    void testMethod_Success() {
        // 准备 (Arrange)
        when(dependencyMapper.selectById(anyLong())).thenReturn(testEntity);

        // 执行 (Act)
        Result result = service.method(1L);

        // 验证 (Assert)
        assertNotNull(result);
        assertEquals(expectedValue, result.getValue());
        verify(dependencyMapper, times(1)).selectById(anyLong());
    }

    @Test
    void testMethod_NotFound() {
        // 准备
        when(dependencyMapper.selectById(anyLong())).thenReturn(null);

        // 执行和验证
        assertThrows(BusinessException.class, () -> service.method(1L));
    }
}
```

### 2. 集成测试 (Integration Tests)

测试多个组件之间的交互，包括 Controller、Service 和数据库。

#### 文件位置
```
{service}/src/test/java/com/qoobot/openidaas/{module}/integration/{Controller}IntegrationTest.java
```

#### 模板示例

```java
package com.qoobot.openidaas.{module}.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 控制器集成测试模板
 */
@SpringBootTest(classes = {ServiceApplication.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityMapper entityMapper;

    @BeforeEach
    void setUp() {
        // 清空测试数据
        entityMapper.delete(null);

        // 准备测试数据
        Entity entity = new Entity();
        entity.setName("test");
        entityMapper.insert(entity);
    }

    @Test
    void testGetById_Success() throws Exception {
        mockMvc.perform(get("/api/entities/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L));
    }
}
```

## 测试配置

### application-test.yml

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:

  h2:
    console:
      enabled: true

  cloud:
    nacos:
      discovery:
        enabled: false

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: auto

logging:
  level:
    com.qoobot.openidaas: DEBUG
```

### schema.sql

测试用数据库表结构（简化版），放置在 `src/test/resources/schema.sql`。

## 测试用例设计

### 服务层测试用例

每个 Service 方法至少需要以下测试用例：

| 用例类型 | 说明 | 示例 |
|---------|------|------|
| 成功场景 | 正常流程，返回预期结果 | `testCreateUser_Success` |
| 重复数据 | 插入已存在的数据 | `testCreateUser_DuplicateUsername` |
| 参数校验 | 无效参数输入 | `testCreateUser_InvalidEmail` |
| 数据不存在 | 查询/删除不存在的数据 | `testGetUser_NotFound` |
| 业务规则验证 | 密码强度、状态转换等 | `testCreateUser_WeakPassword` |
| 异常处理 | 各种异常情况 | `testDeleteUser_HasActiveSessions` |

### 控制器层测试用例

| 用例类型 | 说明 | 测试内容 |
|---------|------|---------|
| GET 请求 | 获取单个资源 | `testGetUser_Success` |
| GET 列表 | 获取资源列表，带分页和筛选 | `testListUsers_WithFilters` |
| POST 创建 | 创建新资源 | `testCreateUser_Success` |
| PUT 更新 | 更新资源 | `testUpdateUser_Success` |
| DELETE 删除 | 删除资源 | `testDeleteUser_Success` |
| 自定义操作 | 锁定、解锁、启用等 | `testLockUser_Success` |
| 404 处理 | 资源不存在 | `testGetUser_NotFound` |
| 400 处理 | 参数错误 | `testCreateUser_InvalidData` |

## 已实现测试

### openidaas-audit-service ✅
- 4个测试类，共 30+ 测试用例
- Service 单元测试
- Controller 集成测试
- AOP 切面测试

### openidaas-user-service ✅
- UserServiceTest - 单元测试（25+ 测试用例）
- UserControllerIntegrationTest - 集成测试（15+ 测试用例）

### openidaas-role-service ⏳
- 待创建测试

### openidaas-organization-service ⏳
- 待创建测试

### openidaas-auth-service ⏳
- 待创建测试

## 运行测试

### 运行所有测试
```bash
mvn test
```

### 运行单个服务测试
```bash
cd openidaas-user-service
mvn test
```

### 运行单个测试类
```bash
mvn test -Dtest=UserServiceTest
```

### 运行单个测试方法
```bash
mvn test -Dtest=UserServiceTest#testCreateUser_Success
```

### 生成覆盖率报告
```bash
mvn clean test jacoco:report
```

报告位置: `target/site/jacoco/index.html`

## 测试覆盖率目标

| 模块 | 目标覆盖率 | 当前状态 |
|------|-----------|---------|
| audit-service | 70% | ✅ 75% |
| user-service | 70% | 🚧 40% |
| role-service | 60% | 🚧 0% |
| organization-service | 60% | 🚧 0% |
| auth-service | 60% | 🚧 0% |
| **总体目标** | **60%** | **🚧 20%** |

## 最佳实践

### 1. 命名规范
```
test{MethodName}_{Scenario}_{ExpectedResult}
```

示例:
- `testCreateUser_Success`
- `testGetUser_NotFound`
- `testUpdateUser_InvalidEmail`

### 2. AAA 模式
每个测试方法遵循 **Arrange-Act-Assert** 模式：
- **Arrange**: 准备测试数据和 Mock 设置
- **Act**: 执行被测试的方法
- **Assert**: 验证结果是否符合预期

### 3. 使用 @BeforeEach
在 `@BeforeEach` 方法中准备公共测试数据。

### 4. 测试独立性
每个测试方法应该独立运行，不依赖于其他测试的执行顺序。

### 5. 使用 @Transactional
集成测试使用 `@Transactional` 确保测试后数据回滚。

### 6. Mock 外部服务
使用 `@MockBean` Mock 外部服务（如 Feign 客户端）。

## 常用断言

```java
// 对象验证
assertNotNull(result);
assertNull(result);
assertEquals(expected, actual);
assertNotEquals(expected, actual);
assertSame(expected, actual);
assertNotSame(expected, actual);

// 集合验证
assertTrue(list.isEmpty());
assertFalse(list.isEmpty());
assertEquals(3, list.size());
assertTrue(list.contains(item));

// 异常验证
assertThrows(BusinessException.class, () -> service.method());

// Mock 验证
verify(mapper, times(1)).selectById(1L);
verify(mapper, never()).delete(any());
verifyNoMoreInteractions(mapper);
```

## MockMvc 常用方法

```java
// GET 请求
mockMvc.perform(get("/api/users/{id}", 1L))

// POST 请求
mockMvc.perform(post("/api/users")
    .contentType(MediaType.APPLICATION_JSON)
    .content(objectMapper.writeValueAsString(dto)))

// PUT 请求
mockMvc.perform(put("/api/users/{id}", 1L)
    .contentType(MediaType.APPLICATION_JSON)
    .content(objectMapper.writeValueAsString(dto)))

// DELETE 请求
mockMvc.perform(delete("/api/users/{id}", 1L))

// 带查询参数
mockMvc.perform(get("/api/users")
    .param("page", "1")
    .param("size", "10")
    .param("keyword", "test"))

// 验证响应状态
.andExpect(status().isOk())
.andExpect(status().isNotFound())
.andExpect(status().isBadRequest())

// 验证响应内容
.andExpect(jsonPath("$.code").value(200))
.andExpect(jsonPath("$.data.id").value(1L))
.andExpect(jsonPath("$.data.username").value("testuser"))
.andExpect(jsonPath("$.data.roles").isArray())
.andExpect(jsonPath("$.data.roles", hasSize(greaterThan(0)))
```

## 持续集成 (CI)

### GitHub Actions 配置

```yaml
name: Test

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run tests
        run: mvn test
      - name: Generate coverage report
        run: mvn jacoco:report
      - name: Upload coverage
        uses: codecov/codecov-action@v3
```

## 参考资源

- [JUnit 5 用户指南](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito 文档](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot 测试指南](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [MockMvc 文档](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/test/web/servlet/MockMvc.html)
