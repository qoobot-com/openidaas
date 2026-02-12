# E2E 测试指南

本文档介绍 OpenIDaaS 管理系统的前端端到端（E2E）测试配置和最佳实践。

## 目录

- [概述](#概述)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [安装和配置](#安装和配置)
- [运行测试](#运行测试)
- [编写测试](#编写测试)
- [最佳实践](#最佳实践)
- [CI/CD 集成](#cicd-集成)
- [故障排除](#故障排除)

## 概述

本项目使用 Cypress 进行端到端测试，覆盖了以下核心功能：

- 用户认证流程（登录、登出、密码重置）
- 用户管理（创建、编辑、删除、搜索）
- 角色管理（创建、编辑、删除、权限分配）
- 组织管理（树形结构、CRUD 操作）
- 导航和路由
- 国际化（i18n）

## 技术栈

- **测试框架**: Cypress 13.x
- **浏览器**: Chrome, Electron, Firefox
- **TypeScript**: 类型安全
- **Element Plus**: UI 组件库

## 项目结构

```
openidaas-admin-ui/
├── cypress/
│   ├── e2e/                  # E2E 测试文件
│   │   ├── auth.cy.ts        # 认证测试
│   │   ├── user-management.cy.ts     # 用户管理测试
│   │   ├── role-management.cy.ts     # 角色管理测试
│   │   ├── organization-management.cy.ts  # 组织管理测试
│   │   ├── navigation.cy.ts   # 导航测试
│   │   └── i18n.cy.ts         # 国际化测试
│   ├── fixtures/              # 测试数据
│   │   └── example.json       # 示例数据
│   ├── support/               # 支持文件
│   │   ├── commands.ts        # 自定义命令
│   │   ├── e2e.ts            # E2E 测试配置
│   │   └── component.ts      # 组件测试配置
│   ├── plugins/              # 插件
│   │   └── index.ts
│   ├── tsconfig.json         # TypeScript 配置
│   └── screenshots/          # 截图（自动生成）
├── cypress.config.ts         # Cypress 配置
└── package.json              # 项目依赖
```

## 安装和配置

### 1. 安装依赖

```bash
npm install
```

### 2. 环境变量配置

在 `cypress.env.json` 中配置测试环境变量：

```json
{
  "apiBaseUrl": "http://localhost:8080/api",
  "username": "admin",
  "password": "admin123",
  "language": "zh-CN"
}
```

### 3. 确保 API 服务运行

在运行 E2E 测试之前，确保后端服务已启动：

```bash
# 启动所有服务
docker-compose up -d

# 或者单独启动网关服务
cd openidaas-gateway
mvn spring-boot:run
```

### 4. 确保 Vite 开发服务器运行

```bash
npm run dev
```

## 运行测试

### 交互式运行

```bash
npx cypress open
```

这将打开 Cypress Test Runner，可以交互式地运行和调试测试。

### 命令行运行所有测试

```bash
npm run test:e2e
```

### 运行特定测试文件

```bash
npx cypress run --spec "cypress/e2e/auth.cy.ts"
```

### 运行匹配模式的测试

```bash
npx cypress run --spec "cypress/e2e/*.cy.ts"
```

### 在不同浏览器中运行

```bash
# Chrome
npx cypress run --browser chrome

# Firefox
npx cypress run --browser firefox

# Electron（默认）
npx cypress run --browser electron
```

### 记录模式（慢动作）

```bash
npx cypress run --config slowMo=500
```

### 运行并行测试（需要 Cypress Dashboard）

```bash
npx cypress run --parallel --record --key <your-key>
```

### 生成测试报告

```bash
# 安装报告生成器
npm install --save-dev cypress-multi-reporters mochawesome

# 运行测试并生成报告
npx cypress run --reporter mochawesome

# 查看报告
npx mochawesome-report generate
```

## 编写测试

### 测试文件结构

```typescript
describe('Feature Name', () => {
  beforeEach(() => {
    // 在每个测试前运行
    cy.login()
  })

  describe('Specific Scenario', () => {
    it('should do something', () => {
      // 测试代码
    })
  })
})
```

### 常用 Cypress 命令

#### 自定义命令

```typescript
// 登录
cy.login('admin', 'admin123')

// 登出
cy.logout()

// 导航到页面
cy.navigateTo('/users')

// 等待 API 请求
cy.waitForApi('@apiRequest')

// 检查成功消息
cy.checkSuccessMessage()

// 检查错误消息
cy.checkErrorMessage()

// 填写表单
cy.fillForm({
  username: 'admin',
  email: 'admin@example.com'
})

// 检查表格数据
cy.checkTableRow(1, {
  username: 'admin',
  email: 'admin@example.com'
})

// 选择语言
cy.selectLanguage('en-US')
```

#### 基本选择器

```typescript
// 通过 data-cy 属性选择
cy.get('[data-cy="login-submit"]').click()

// 通过 CSS 选择器选择
cy.get('.el-button--primary').click()

// 通过文本选择
cy.contains('登录').click()
```

#### 等待和断言

```typescript
// 等待元素可见
cy.get('[data-cy="login-page"]').should('be.visible')

// 等待 URL 匹配
cy.url().should('include', '/dashboard')

// 等待元素包含文本
cy.get('[data-cy="message"]').should('contain.text', '成功')

// 等待 API 请求
cy.intercept('POST', '/api/auth/login').as('loginRequest')
cy.wait('@loginRequest')
```

### 示例：编写新的测试用例

```typescript
describe('New Feature', () => {
  beforeEach(() => {
    cy.login()
    cy.navigateTo('/new-feature')
  })

  it('should display the feature page', () => {
    cy.get('[data-cy="feature-page"]').should('be.visible')
  })

  it('should create new item', () => {
    cy.intercept('POST', '/api/v1/items').as('createItem')

    cy.get('[data-cy="create-button"]').click()
    cy.get('[data-cy="item-name"]').type('Test Item')
    cy.get('[data-cy="confirm-button"]').click()

    cy.wait('@createItem').then((xhr) => {
      expect(xhr.response?.statusCode).to.eq(201)
    })

    cy.checkSuccessMessage()
  })

  it('should show validation errors', () => {
    cy.get('[data-cy="create-button"]').click()
    cy.get('[data-cy="confirm-button"]').click()

    cy.get('[data-cy="item-name-error"]').should('be.visible')
  })
})
```

## 最佳实践

### 1. 使用 data-cy 属性

在应用中使用 `data-cy` 属性作为测试选择器，避免依赖 UI 类名或文本：

```vue
<template>
  <el-button data-cy="login-submit" type="primary">登录</el-button>
</template>
```

### 2. 保持测试独立性

每个测试应该可以独立运行，不依赖其他测试：

```typescript
beforeEach(() => {
  // 清理数据
  cy.clearLocalStorage()
  cy.clearCookies()
})
```

### 3. 使用 API 拦截

拦截 API 请求以验证请求和响应：

```typescript
cy.intercept('POST', '/api/v1/users').as('createUser')
cy.get('[data-cy="submit"]').click()
cy.wait('@createUser').its('response.statusCode').should('eq', 201)
```

### 4. 使用固定数据

使用 fixtures 文件存储测试数据：

```typescript
cy.fixture('example.json').then((data) => {
  cy.get('[data-cy="username"]').type(data.testUser.username)
})
```

### 5. 等待元素而不是固定时间

使用 `should()` 而不是 `wait()`：

```typescript
// 不好
cy.wait(5000)
cy.get('.modal').should('be.visible')

// 好
cy.get('.modal').should('be.visible', { timeout: 10000 })
```

### 6. 使用 Page Object 模式

对于复杂的页面，使用 Page Object 模式组织代码：

```typescript
// cypress/support/pages/LoginPage.ts
export class LoginPage {
  visit() {
    cy.visit('/login')
  }

  login(username: string, password: string) {
    cy.get('[data-cy="username"]').type(username)
    cy.get('[data-cy="password"]').type(password)
    cy.get('[data-cy="submit"]').click()
  }

  verifyLoginSuccess() {
    cy.url().should('not.include', '/login')
  }
}

// 使用
import { LoginPage } from '../support/pages/LoginPage'

it('should login successfully', () => {
  const loginPage = new LoginPage()
  loginPage.visit()
  loginPage.login('admin', 'admin123')
  loginPage.verifyLoginSuccess()
})
```

### 7. 使用自定义命令封装重复逻辑

创建可重用的自定义命令（见 `cypress/support/commands.ts`）。

### 8. 测试用户行为而非实现细节

测试用户看到的和操作的，而不是内部实现：

```typescript
// 好
cy.get('[data-cy="login-form"]').submit()

// 不好
cy.get('form').find('input').eq(0).type('admin')
cy.get('form').find('input').eq(1).type('password')
cy.get('form').find('button').click()
```

### 9. 使用环境配置

在不同环境中使用不同的配置：

```typescript
// cypress.config.ts
export default defineConfig({
  e2e: {
    env: {
      apiUrl: Cypress.env('apiUrl'),
      baseUrl: Cypress.env('baseUrl')
    }
  }
})
```

### 10. 编写有意义的断言

使用描述性的断言消息：

```typescript
cy.get('[data-cy="username"]', { timeout: 10000 })
  .should('be.visible')
  .and('have.value', 'admin')
```

## CI/CD 集成

### GitHub Actions 示例

```yaml
name: E2E Tests

on: [push, pull_request]

jobs:
  cypress:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:14
        env:
          POSTGRES_PASSWORD: postgres
        ports:
          - 5432:5432

    steps:
      - uses: actions/checkout@v3

      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'

      - name: Install dependencies
        run: npm ci

      - name: Start services
        run: docker-compose up -d

      - name: Wait for services
        run: |
          npx wait-on http://localhost:8080/actuator/health
          npx wait-on http://localhost:3000

      - name: Run E2E tests
        run: npm run test:e2e

      - name: Upload screenshots
        uses: actions/upload-artifact@v3
        if: failure()
        with:
          name: cypress-screenshots
          path: cypress/screenshots

      - name: Upload videos
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: cypress-videos
          path: cypress/videos
```

### GitLab CI 示例

```yaml
e2e:
  image: cypress/base:18
  stage: test
  services:
    - postgres:14
  variables:
    POSTGRES_PASSWORD: postgres
  script:
    - npm ci
    - npm run dev &
    - npx wait-on http://localhost:3000
    - npm run test:e2e
  artifacts:
    when: always
    paths:
      - cypress/screenshots
      - cypress/videos
```

### Jenkins Pipeline 示例

```groovy
pipeline {
  agent any

  stages {
    stage('Install') {
      steps {
        sh 'npm ci'
      }
    }

    stage('Start Services') {
      steps {
        sh 'docker-compose up -d'
        sh 'npx wait-on http://localhost:8080/actuator/health'
      }
    }

    stage('E2E Tests') {
      steps {
        sh 'npm run test:e2e'
      }
    }
  }

  post {
    always {
      sh 'docker-compose down'
      archiveArtifacts artifacts: 'cypress/screenshots/**/*', allowEmptyArchive: true
      archiveArtifacts artifacts: 'cypress/videos/**/*', allowEmptyArchive: true
    }
  }
}
```

## 故障排除

### 测试失败常见问题

1. **元素未找到**

   ```typescript
   // 增加超时时间
   cy.get('[data-cy="element"]', { timeout: 10000 }).should('be.visible')
   ```

2. **API 请求超时**

   ```typescript
   // 在配置中增加超时时间
   // cypress.config.ts
   defaultCommandTimeout: 10000,
   requestTimeout: 10000
   ```

3. **CORS 错误**

   确保在开发环境中配置了正确的代理：

   ```typescript
   // vite.config.ts
   server: {
     proxy: {
       '/api': {
         target: 'http://localhost:8080',
         changeOrigin: true
       }
     }
   }
   ```

4. **测试运行过慢**

   - 使用 `cy.intercept()` 减少不必要的等待
   - 减少截图和视频录制
   - 使用并行测试

5. **内存不足**

   ```bash
   # 增加 Node.js 内存限制
   NODE_OPTIONS=--max-old-space-size=4096 npm run test:e2e
   ```

### 调试技巧

1. **使用 `.debug()`**

   ```typescript
   cy.get('[data-cy="element"]').debug()
   ```

2. **暂停测试**

   ```typescript
   cy.pause()
   ```

3. **查看元素**

   ```typescript
   cy.log(cy.get('[data-cy="element"]'))
   ```

4. **运行单个测试**

   ```bash
   npx cypress run --spec "cypress/e2e/auth.cy.ts" --grep "should login successfully"
   ```

### 日志和报告

1. **查看测试日志**

   ```bash
   npx cypress run --config video=false
   ```

2. **生成覆盖率报告**

   需要安装 `@cypress/code-coverage`：

   ```bash
   npm install --save-dev @cypress/code-coverage
   ```

3. **查看详细输出**

   ```bash
   DEBUG=cypress:* npx cypress run
   ```

## 参考资源

- [Cypress 官方文档](https://docs.cypress.io/)
- [Cypress 最佳实践](https://docs.cypress.io/guides/references/best-practices)
- [Element Plus 文档](https://element-plus.org/)
- [Vue.js 测试指南](https://vuejs.org/guide/scaling-up/testing.html)

## 总结

本指南提供了 OpenIDaaS 系统 E2E 测试的完整参考，包括：

- 项目结构和配置
- 安装和运行测试
- 编写测试的最佳实践
- CI/CD 集成示例
- 故障排除指南

遵循这些指南可以帮助团队保持高质量的 E2E 测试套件，确保应用程序的稳定性和可靠性。
