# 测试覆盖率配置与使用指南

## 概述

本文档说明如何配置和使用 Jacoco 生成测试覆盖率报告，以及如何达到 60% 以上的测试覆盖率目标。

## Jacoco 配置

### 在各服务的 pom.xml 中添加 Jacoco Maven 插件

```xml
<build>
    <plugins>
        <!-- Jacoco Maven Plugin -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
            <executions>
                <!-- 准备 agent -->
                <execution>
                    <id>prepare-agent</id>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>

                <!-- 生成报告 -->
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>

                <!-- 检查覆盖率 -->
                <execution>
                    <id>check</id>
                    <goals>
                        <goal>check</goal>
                    </goals>
                    <configuration>
                        <rules>
                            <rule>
                                <element>PACKAGE</element>
                                <limits>
                                    <limit>
                                        <counter>LINE</counter>
                                        <value>COVEREDRATIO</value>
                                        <minimum>0.60</minimum>
                                    </limit>
                                </limits>
                            </rule>
                        </rules>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## 使用 Jacoco

### 运行测试并生成覆盖率报告

```bash
# 在单个服务中
cd openidaas-user-service
mvn clean test jacoco:report

# 在整个项目中
mvn clean test jacoco:report
```

### 查看报告

报告生成在各服务的 `target/site/jacoco/index.html`。

在浏览器中打开：
```
openidaas-user-service/target/site/jacoco/index.html
```

### 命令行查看覆盖率摘要

```bash
# 查看指令覆盖率
mvn jacoco:check

# 查看详细报告
mvn jacoco:report
```

## 覆盖率指标解读

Jacoco 提供以下覆盖率指标：

| 指标 | 说明 | 目标值 |
|------|------|--------|
| **Instruction (指令)** | 字节码指令覆盖率 | 60% |
| **Branch (分支)** | if/switch 等分支语句覆盖率 | 50% |
| **Line (行)** | Java 源代码行覆盖率 | 60% |
| **Method (方法)** | 方法覆盖率 | 70% |
| **Class (类)** | 类覆盖率 | 80% |

### 各服务覆盖率目标

| 服务 | 当前覆盖率 | 目标覆盖率 | 状态 |
|------|-----------|-----------|------|
| audit-service | 75% | 70% | ✅ |
| user-service | 40% | 70% | 🚧 |
| role-service | 0% | 60% | ⏳ |
| organization-service | 0% | 60% | ⏳ |
| auth-service | 0% | 60% | ⏳ |
| **总体** | **20%** | **60%** | **🚧** |

## 提高测试覆盖率的方法

### 1. 补充单元测试

针对每个 Service 方法编写单元测试：

```java
@Test
void testMethodName_Success() { }
@Test
void testMethodName_NotFound() { }
@Test
void testMethodName_InvalidParameter() { }
```

### 2. 添加边界条件测试

测试各种边界情况：

```java
@Test
void testWithNullParameter() { }
@Test
void testWithEmptyList() { }
@Test
void testWithMaxValue() { }
@Test
void testWithMinValue() { }
```

### 3. 测试异常路径

```java
@Test
void testThrowsBusinessException() {
    assertThrows(BusinessException.class, () -> service.method());
}
```

### 4. 覆盖所有代码分支

使用 Jacoco 报告识别未覆盖的分支，添加测试用例：

```java
@Test
void testBranchConditionTrue() { }
@Test
void testBranchConditionFalse() { }
```

### 5. 集成测试覆盖

集成测试可以覆盖那些难以用单元测试覆盖的代码：

- 数据库操作
- 事务处理
- 缓存逻辑
- 外部服务调用

## 测试最佳实践

### 1. 遵循 AAA 模式

```java
@Test
void testCreateUser_Success() {
    // Arrange (准备)
    UserCreateDTO dto = new UserCreateDTO();
    dto.setUsername("testuser");

    // Act (执行)
    UserVO result = userService.createUser(dto);

    // Assert (验证)
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
}
```

### 2. 使用有意义的测试名称

```java
// ❌ 不好
void test1() { }

// ✅ 好
void testCreateUser_WithDuplicateUsername_ThrowsException() { }
```

### 3. 测试一个关注点

每个测试方法应该只测试一个特定的场景或关注点。

### 4. 使用 Mock 隔离依赖

```java
@Mock
private UserMapper userMapper;

@Test
void testGetUser_Success() {
    when(userMapper.selectById(1L)).thenReturn(testUser);

    User result = userService.getUser(1L);

    assertNotNull(result);
}
```

### 5. 避免硬编码

```java
// ❌ 不好
assertEquals(1, result.getId());

// ✅ 好
assertEquals(testUser.getId(), result.getId());
```

## 常见问题

### Q1: 某些类不需要测试怎么办？

在 pom.xml 中配置排除规则：

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <excludes>
            <exclude>**/dto/**</exclude>
            <exclude>**/vo/**</exclude>
            <exclude>**/entity/**</exclude>
            <exclude>**/config/**</exclude>
            <exclude>**/*Application.class</exclude>
        </excludes>
    </configuration>
</plugin>
```

### Q2: 覆盖率一直上不去怎么办？

1. 查看 Jacoco 报告中红色的未覆盖代码
2. 针对红色部分编写测试用例
3. 优先覆盖核心业务逻辑
4. 避免为了提高覆盖率而写无意义的测试

### Q3: 单元测试还是集成测试？

- **单元测试**: 快速、隔离、适合测试业务逻辑
- **集成测试**: 慢、真实环境、适合测试组件交互

建议：单元测试占 70%，集成测试占 30%。

### Q4: 如何测试私有方法？

不建议直接测试私有方法。应该：
1. 测试调用私有方法的公共方法
2. 如果必须测试，考虑使用反射或重构为包私有方法

## CI/CD 集成

### GitHub Actions 示例

```yaml
name: Test

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK
        uses: actions/setup-java@v3
        with:
          java-version: '21'

      - name: Run tests with coverage
        run: mvn clean test jacoco:report

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./target/site/jacoco/jacoco.xml

      - name: Check coverage
        run: mvn jacoco:check
```

### SonarQube 集成

```bash
mvn clean test jacoco:report sonar:sonar \
  -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
```

## 参考资源

- [Jacoco 官方文档](https://www.jacoco.org/jacoco/trunk/doc/)
- [JUnit 5 用户指南](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito 文档](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [测试最佳实践](https://testing.googleblog.com/2015/04/testing-on-toilet-seven-testing-mistakes.html)
