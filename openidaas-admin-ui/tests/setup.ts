// 测试环境设置文件
import { vi, beforeEach, afterEach } from 'vitest'
import { config } from '@vue/test-utils'

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {}
  
  return {
    getItem(key: string) {
      return store[key] || null
    },
    setItem(key: string, value: string) {
      store[key] = value.toString()
    },
    removeItem(key: string) {
      delete store[key]
    },
    clear() {
      store = {}
    }
  }
})()

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock
})

// Mock matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
})

// 配置 Vue Test Utils
config.global.mocks = {
  $t: (key: string) => key,
  $router: {
    push: vi.fn(),
    go: vi.fn(),
    replace: vi.fn()
  },
  $route: {
    path: '/',
    params: {},
    query: {}
  }
}

// 全局 beforeEach
beforeEach(() => {
  // 清理 localStorage
  localStorage.clear()
  // 重置所有 mocks
  vi.clearAllMocks()
})

// 全局 afterEach
afterEach(() => {
  // 清理 DOM
  document.body.innerHTML = ''
})