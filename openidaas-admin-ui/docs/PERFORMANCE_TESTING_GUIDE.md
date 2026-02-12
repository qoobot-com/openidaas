# 性能测试指南

## 目录

- [首屏加载性能测试](#首屏加载性能测试)
- [大数据量列表性能测试](#大数据量列表性能测试)
- [路由跳转测试](#路由跳转测试)
- [错误处理测试](#错误处理测试)

---

## 首屏加载性能测试

### 测试目标

- 首屏加载时间 < 1.5秒
- Time to Interactive (TTI) < 2秒
- First Contentful Paint (FCP) < 1秒
- Bundle size (gzipped) < 500KB

### 测试方法

#### 1. 使用 Lighthouse

```bash
# 安装 Lighthouse
npm install -g lighthouse

# 运行 Lighthouse 测试
lighthouse http://localhost:3000 --view
```

#### 2. 使用 Chrome DevTools

1. 打开 Chrome DevTools (F12)
2. 切换到 Performance 面板
3. 点击 Record 开始录制
4. 刷新页面
5. 停止录制并分析结果

#### 3. 使用 WebPageTest

访问 https://www.webpagetest.org 并输入测试 URL

### 性能指标说明

| 指标 | 说明 | 目标值 |
|------|------|--------|
| FCP (First Contentful Paint) | 首次内容绘制 | < 1.0s |
| LCP (Largest Contentful Paint) | 最大内容绘制 | < 2.5s |
| TTI (Time to Interactive) | 可交互时间 | < 3.8s |
| TBT (Total Blocking Time) | 总阻塞时间 | < 200ms |
| CLS (Cumulative Layout Shift) | 累积布局偏移 | < 0.1 |
| FID (First Input Delay) | 首次输入延迟 | < 100ms |

### 优化建议

- **代码分割**: 使用动态导入和路由懒加载
- **资源压缩**: 启用 Gzip/Brotli 压缩
- **缓存策略**: 合理设置 HTTP 缓存头
- **CDN 加速**: 使用 CDN 分发静态资源
- **图片优化**: 使用 WebP 格式，实现懒加载
- **减少请求**: 合并小文件，内联关键 CSS

---

## 大数据量列表性能测试

### 测试目标

- 渲染 10,000 条数据时 FPS 保持 > 30
- 滚动流畅，无明显卡顿
- 虚拟滚动正常工作

### 测试用例

#### 1. 用户列表测试

```typescript
// tests/performance/user-list-performance.test.ts
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import UserList from '@/modules/user/UserList.vue'

describe('UserList Performance', () => {
  it('should render 10000 users efficiently', async () => {
    const users = Array.from({ length: 10000 }, (_, i) => ({
      id: i + 1,
      username: `user${i}`,
      email: `user${i}@example.com`,
      status: 1
    }))

    const startTime = performance.now()
    const wrapper = mount(UserList, {
      props: { users }
    })
    const renderTime = performance.now() - startTime

    expect(renderTime).toBeLessThan(1000) // 渲染时间 < 1秒
    wrapper.unmount()
  })
})
```

#### 2. 虚拟滚动测试

```typescript
// tests/performance/virtual-scroll.test.ts
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import VirtualTable from '@/components/VirtualTable.vue'

describe('VirtualTable Performance', () => {
  it('should handle 100000 items with virtual scrolling', () => {
    const items = Array.from({ length: 100000 }, (_, i) => ({
      id: i,
      name: `Item ${i}`
    }))

    const wrapper = mount(VirtualTable, {
      props: { items, itemHeight: 50, bufferSize: 10 }
    })

    // 验证只渲染可见的项目
    const visibleItems = wrapper.vm.visibleItems
    expect(visibleItems.length).toBeLessThan(100)

    wrapper.unmount()
  })
})
```

### 性能测试命令

```bash
# 运行性能测试
npm run test:performance

# 使用 Chrome DevTools Performance 面板
# 1. 打开包含大数据列表的页面
# 2. 切换到 Performance 面板
# 3. 开始录制
# 4. 滚动列表
# 5. 停止录制并分析 FPS
```

---

## 路由跳转测试

### 测试目标

- 所有路由都能正常跳转
- 页面切换无白屏
- 路由参数正确传递
- 路由守卫正常工作

### E2E 测试

```typescript
// cypress/e2e/navigation-test.cy.ts
describe('Navigation', () => {
  it('should navigate between routes correctly', () => {
    cy.visit('/login')
    cy.contains('登录').should('be.visible')

    cy.get('[data-testid="username"]').type('admin')
    cy.get('[data-testid="password"]').type('admin123')
    cy.contains('登录').click()

    // 应该跳转到首页
    cy.url().should('include', '/dashboard')
    cy.contains('首页').should('be.visible')

    // 测试用户管理页面
    cy.contains('用户管理').click()
    cy.url().should('include', '/user/list')
    cy.contains('用户列表').should('be.visible')

    // 测试部门管理页面
    cy.contains('组织架构').click()
    cy.url().should('include', '/organization/departments')
    cy.contains('部门管理').should('be.visible')

    // 测试角色管理页面
    cy.contains('角色权限').click()
    cy.url().should('include', '/role/list')
    cy.contains('角色管理').should('be.visible')
  })

  it('should handle route parameters correctly', () => {
    cy.login('admin', 'admin123')
    cy.visit('/user/detail/1')
    cy.contains('用户详情').should('be.visible')
    cy.contains('用户 ID: 1').should('be.visible')
  })

  it('should show 404 page for unknown routes', () => {
    cy.visit('/unknown-route')
    cy.contains('页面不存在').should('be.visible')
  })
})
```

### 单元测试

```typescript
// tests/router/router.test.ts
import { describe, it, expect, vi } from 'vitest'
import { createRouter, createWebHistory } from 'vue-router'
import routes from '@/router/routes'

describe('Router', () => {
  it('should have all required routes', () => {
    const router = createRouter({
      history: createWebHistory(),
      routes
    })

    const expectedRoutes = [
      '/dashboard',
      '/user/list',
      '/user/detail/:id',
      '/organization/departments',
      '/role/list',
      '/permission/tree',
      '/audit/log/list',
      '/audit/security-events'
    ]

    expectedRoutes.forEach(path => {
      const route = router.resolve(path)
      expect(route.matched.length).toBeGreaterThan(0)
    })
  })

  it('should redirect to login for protected routes when not authenticated', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes
    })

    await router.push('/dashboard')
    expect(router.currentRoute.value.path).toBe('/login')
  })
})
```

---

## 错误处理测试

### 测试目标

- 网络错误正确显示
- 服务器错误正确显示
- 404 错误正确显示
- 401/403 权限错误正确处理
- 未知错误有友好提示

### API 错误处理测试

```typescript
// tests/api/error-handling.test.ts
import { describe, it, expect, vi } from 'vitest'
import { userApi } from '@/api/user'
import request from '@/utils/request'

vi.mock('@/utils/request')

describe('API Error Handling', () => {
  it('should handle 401 unauthorized error', async () => {
    const error = new Error('Unauthorized')
    error.response = { status: 401 }
    
    vi.mocked(request.get).mockRejectedValue(error)
    
    await expect(userApi.getUsers()).rejects.toThrow()
    // 验证是否跳转到登录页
  })

  it('should handle 403 forbidden error', async () => {
    const error = new Error('Forbidden')
    error.response = { status: 403 }
    
    vi.mocked(request.get).mockRejectedValue(error)
    
    await expect(userApi.getUsers()).rejects.toThrow()
    // 验证是否显示权限错误提示
  })

  it('should handle 404 not found error', async () => {
    const error = new Error('Not Found')
    error.response = { status: 404 }
    
    vi.mocked(request.get).mockRejectedValue(error)
    
    await expect(userApi.getUser(99999)).rejects.toThrow()
    // 验证是否显示 404 提示
  })

  it('should handle 500 server error', async () => {
    const error = new Error('Internal Server Error')
    error.response = { status: 500 }
    
    vi.mocked(request.get).mockRejectedValue(error)
    
    await expect(userApi.getUsers()).rejects.toThrow()
    // 验证是否显示服务器错误提示
  })

  it('should handle network error', async () => {
    const error = new Error('Network Error')
    error.message = 'Network Error'
    
    vi.mocked(request.get).mockRejectedValue(error)
    
    await expect(userApi.getUsers()).rejects.toThrow()
    // 验证是否显示网络错误提示
  })

  it('should handle timeout error', async () => {
    const error = new Error('Request Timeout')
    error.code = 'ECONNABORTED'
    
    vi.mocked(request.get).mockRejectedValue(error)
    
    await expect(userApi.getUsers()).rejects.toThrow()
    // 验证是否显示超时提示
  })
})
```

### 组件错误处理测试

```typescript
// tests/components/GlobalError.test.ts
import { describe, it, expect } from 'vitest'
import { render } from '@vue/test-utils'
import GlobalError from '@/components/GlobalError.vue'

describe('GlobalError Component', () => {
  it('should display network error', () => {
    const wrapper = render(GlobalError, {
      props: {
        type: 'network',
        message: '网络连接失败'
      }
    })

    expect(wrapper.text()).toContain('网络连接失败')
  })

  it('should display server error', () => {
    const wrapper = render(GlobalError, {
      props: {
        type: 'server',
        message: '服务器错误'
      }
    })

    expect(wrapper.text()).toContain('服务器错误')
  })

  it('should emit retry event when retry button is clicked', async () => {
    const wrapper = render(GlobalError, {
      props: {
        type: 'network',
        message: '网络错误'
      }
    })

    await wrapper.find('[data-testid="retry-button"]').trigger('click')
    expect(wrapper.emitted('retry')).toBeTruthy()
  })
})
```

---

## 完整测试流程

### 1. 运行所有测试

```bash
# 单元测试
npm run test:unit

# E2E 测试
npm run test:e2e

# 性能测试
npm run test:performance

# 所有测试
npm run test:all
```

### 2. API 配置验证

```bash
node scripts/verify-api-config.js
```

### 3. Bundle Size 分析

```bash
npm run build
node scripts/bundle-size-analyzer.js
```

### 4. Lighthouse 测试

```bash
npm run dev
# 在另一个终端运行
lighthouse http://localhost:3000 --view
```

---

## 测试报告模板

### 首屏加载性能

| 指标 | 实测值 | 目标值 | 状态 |
|------|--------|--------|------|
| FCP |  | < 1.0s |  |
| LCP |  | < 2.5s |  |
| TTI |  | < 3.8s |  |
| TBT |  | < 200ms |  |
| CLS |  | < 0.1 |  |
| FID |  | < 100ms |  |
| Bundle Size (gzip) |  | < 500KB |  |

### 大数据量性能

| 测试场景 | 数据量 | FPS | 内存使用 | 状态 |
|----------|--------|-----|----------|------|
| 用户列表渲染 | 10,000 | > 30 | < 200MB |  |
| 用户列表滚动 | 10,000 | > 30 | 稳定 |  |
| 虚拟表格渲染 | 100,000 | > 30 | < 300MB |  |

### 路由测试

| 路由 | 状态 | 备注 |
|------|------|------|
| /dashboard | ✅ |  |
| /user/list | ✅ |  |
| /organization/departments | ✅ |  |
| /role/list | ✅ |  |
| /permission/tree | ✅ |  |

### 错误处理

| 错误类型 | 状态 | 备注 |
|----------|------|------|
| 网络错误 | ✅ |  |
| 服务器错误 (500) | ✅ |  |
| 未授权 (401) | ✅ |  |
| 禁止访问 (403) | ✅ |  |
| 未找到 (404) | ✅ |  |
| 超时错误 | ✅ |  |
