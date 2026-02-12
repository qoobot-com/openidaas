// ***********************************************************
// This example support/e2e.ts is processed and
// loaded automatically before your test files.
//
// This is a great place to put global configuration and
// behavior that modifies Cypress.
//
// You can change the location of this file or turn off
// automatically serving support files with the
// 'supportFile' configuration option.
//
// You can read more here:
// https://on.cypress.io/configuration
// ***********************************************************

// Import commands.js using ES2015 syntax:
import './commands'

// Alternatively you can use CommonJS syntax:
// require('./commands')

// 全局 beforeEach 钩子
beforeEach(() => {
  // 在每个测试前清空 localStorage
  cy.clearLocalStorage()
  cy.clearCookies()

  // 设置默认语言
  cy.window().then((win) => {
    win.localStorage.setItem('language', 'zh-CN')
  })

  // 拦截所有 API 请求
  cy.intercept('POST', '/api/auth/login').as('loginRequest')
  cy.intercept('POST', '/api/auth/logout').as('logoutRequest')
  cy.intercept('GET', '/api/user/profile').as('profileRequest')
  cy.intercept('GET', '/api/**').as('apiRequest')
})

// 全局 afterEach 钩子
afterEach(() => {
  // 在每个测试后截图（仅在测试失败时）
  cy.screenshot({ capture: 'viewport' })
})

// 全局 before 钩子
before(() => {
  // 运行所有测试前执行一次
  cy.log('Starting E2E tests')
})

// 全局 after 钩子
after(() => {
  // 所有测试运行后执行
  cy.log('E2E tests completed')
})

// 忽略未捕获的异常
Cypress.on('uncaught:exception', (err, runnable) => {
  // 返回 false 阻止 Cypress 测试失败
  if (err.message.includes('ResizeObserver loop limit exceeded')) {
    return false
  }
  if (err.message.includes('Non-Error promise rejection captured')) {
    return false
  }
  return true
})
