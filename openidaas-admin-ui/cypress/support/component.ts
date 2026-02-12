// ***********************************************************
// This example support/component.ts is processed and
// loaded automatically before your component tests.
//
// This is a great place to put global configuration and
// behavior that modifies Cypress.
//
// You can read more here:
// https://on.cypress.io/configuration
// ***********************************************************

// Import commands.js using ES2015 syntax:
import './commands'

// Alternatively you can use CommonJS syntax:
// require('./commands')

// 组件测试全局配置
import { mount } from 'cypress/vue'
import { createPinia } from 'pinia'
import { createI18n } from 'vue-i18n'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import '@/styles/index.scss'

// 为所有组件测试设置 Pinia 和 I18n
Cypress.Commands.add('mount', (component, options = {}) => {
  const pinia = createPinia()

  const i18n = createI18n({
    legacy: false,
    locale: 'zh-CN',
    fallbackLocale: 'en-US',
    messages: {
      'zh-CN': {},
      'en-US': {}
    }
  })

  const globalOptions = {
    plugins: [pinia, i18n, ElementPlus],
    ...options.global
  }

  return mount(component, { ...options, global: globalOptions })
})

// 每个测试前清空存储
beforeEach(() => {
  cy.clearLocalStorage()
  cy.clearCookies()
})
