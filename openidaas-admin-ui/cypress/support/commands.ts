/// <reference types="cypress" />

// 全局命令扩展
declare global {
  namespace Cypress {
    interface Chainable {
      /**
       * 登录系统
       * @param username 用户名
       * @param password 密码
       */
      login(username?: string, password?: string): Chainable<Element>

      /**
       * 登出系统
       */
      logout(): Chainable<Element>

      /**
       * 等待页面加载完成
       */
      waitForPageLoad(): Chainable<Element>

      /**
       * 检查是否有错误提示
       */
      checkErrorMessage(): Chainable<Element>

      /**
       * 检查是否有成功提示
       */
      checkSuccessMessage(): Chainable<Element>

      /**
       * 导航到指定页面
       */
      navigateTo(path: string): Chainable<Element>

      /**
       * 选择语言
       */
      selectLanguage(lang: string): Chainable<Element>

      /**
       * 上传文件
       */
      uploadFile(selector: string, fileName: string): Chainable<Element>

      /**
       * 等待 API 请求完成
       */
      waitForApi(alias: string): Chainable<Element>

      /**
       * 检查表格数据
       */
      checkTableRow(rowIndex: number, data: Record<string, string>): Chainable<Element>

      /**
       * 填写表单
       */
      fillForm(formData: Record<string, string>): Chainable<Element>
    }
  }
}

// 登录命令
Cypress.Commands.add('login', (username = 'admin', password = 'admin123') => {
  cy.visit('/login')
  cy.get('[data-cy="login-username"]').clear().type(username)
  cy.get('[data-cy="login-password"]').clear().type(password)
  cy.get('[data-cy="login-submit"]').click()
  cy.url().should('not.include', '/login')
  cy.waitForPageLoad()
})

// 登出命令
Cypress.Commands.add('logout', () => {
  cy.get('[data-cy="user-dropdown"]').click()
  cy.get('[data-cy="logout-button"]').click()
  cy.url().should('include', '/login')
})

// 等待页面加载完成
Cypress.Commands.add('waitForPageLoad', () => {
  cy.get('body').should('not.have.class', 'el-loading-parent--relative')
  cy.get('.el-loading-mask').should('not.exist')
})

// 检查错误提示
Cypress.Commands.add('checkErrorMessage', () => {
  cy.get('.el-message--error', { timeout: 5000 }).should('be.visible')
})

// 检查成功提示
Cypress.Commands.add('checkSuccessMessage', () => {
  cy.get('.el-message--success', { timeout: 5000 }).should('be.visible')
})

// 导航到指定页面
Cypress.Commands.add('navigateTo', (path: string) => {
  cy.intercept('GET', '/api/**').as('apiRequest')
  cy.visit(path, { timeout: 60000 })
  cy.waitForApi('@apiRequest')
  cy.waitForPageLoad()
})

// 选择语言
Cypress.Commands.add('selectLanguage', (lang: string) => {
  cy.get('[data-cy="language-switcher"]').click()
  cy.get(`[data-cy="lang-${lang}"]`).click()
  cy.get('[data-cy="language-switcher"]').should('contain.text', lang.toUpperCase())
})

// 上传文件
Cypress.Commands.add('uploadFile', (selector: string, fileName: string) => {
  cy.get(selector).selectFile(`cypress/fixtures/${fileName}`)
})

// 等待 API 请求
Cypress.Commands.add('waitForApi', (alias: string) => {
  cy.wait(alias, { timeout: 30000 }).its('response.statusCode').should('be.oneOf', [200, 201, 204])
})

// 检查表格数据
Cypress.Commands.add('checkTableRow', (rowIndex: number, data: Record<string, string>) => {
  Object.entries(data).forEach(([key, value]) => {
    cy.get(`.el-table__body tr:nth-child(${rowIndex})`)
      .find(`[data-cy="${key}"]`)
      .should('contain.text', value)
  })
})

// 填写表单
Cypress.Commands.add('fillForm', (formData: Record<string, string>) => {
  Object.entries(formData).forEach(([key, value]) => {
    cy.get(`[data-cy="${key}"]`).clear().type(value)
  })
})
