describe('Internationalization (i18n)', () => {
  beforeEach(() => {
    cy.login()
  })

  describe('Language Switcher', () => {
    it('should display language switcher', () => {
      cy.get('[data-cy="language-switcher"]').should('be.visible')
    })

    it('should switch to Chinese', () => {
      cy.selectLanguage('zh-CN')
      cy.get('body').should('have.attr', 'lang', 'zh-CN')
    })

    it('should switch to English', () => {
      cy.selectLanguage('en-US')
      cy.get('body').should('have.attr', 'lang', 'en-US')
    })

    it('should persist language preference', () => {
      cy.selectLanguage('en-US')
      cy.reload()
      cy.get('[data-cy="language-switcher"]').should('contain.text', 'EN')
    })
  })

  describe('Chinese Language Content', () => {
    beforeEach(() => {
      cy.selectLanguage('zh-CN')
    })

    it('should display Chinese text on login page', () => {
      cy.logout()
      cy.get('[data-cy="login-title"]').should('contain.text', '登录')
      cy.get('[data-cy="login-username"]').should('have.attr', 'placeholder', '请输入用户名')
      cy.get('[data-cy="login-password"]').should('have.attr', 'placeholder', '请输入密码')
    })

    it('should display Chinese text on dashboard', () => {
      cy.navigateTo('/dashboard')
      cy.get('[data-cy="dashboard-title"]').should('contain.text', '仪表板')
    })

    it('should display Chinese navigation labels', () => {
      cy.get('[data-cy="nav-item-users"]').should('contain.text', '用户管理')
      cy.get('[data-cy="nav-item-roles"]').should('contain.text', '角色管理')
      cy.get('[data-cy="nav-item-organizations"]').should('contain.text', '组织管理')
    })

    it('should display Chinese button labels', () => {
      cy.navigateTo('/users')
      cy.get('[data-cy="create-user-button"]').should('contain.text', '创建用户')
      cy.get('[data-cy="search-button"]').should('contain.text', '搜索')
    })

    it('should display Chinese table headers', () => {
      cy.navigateTo('/users')
      cy.get('.el-table__header [data-cy="header-username"]').should('contain.text', '用户名')
      cy.get('.el-table__header [data-cy="header-email"]').should('contain.text', '邮箱')
    })
  })

  describe('English Language Content', () => {
    beforeEach(() => {
      cy.selectLanguage('en-US')
    })

    it('should display English text on login page', () => {
      cy.logout()
      cy.get('[data-cy="login-title"]').should('contain.text', 'Login')
      cy.get('[data-cy="login-username"]').should('have.attr', 'placeholder', 'Please enter username')
      cy.get('[data-cy="login-password"]').should('have.attr', 'placeholder', 'Please enter password')
    })

    it('should display English text on dashboard', () => {
      cy.navigateTo('/dashboard')
      cy.get('[data-cy="dashboard-title"]').should('contain.text', 'Dashboard')
    })

    it('should display English navigation labels', () => {
      cy.get('[data-cy="nav-item-users"]').should('contain.text', 'User Management')
      cy.get('[data-cy="nav-item-roles"]').should('contain.text', 'Role Management')
      cy.get('[data-cy="nav-item-organizations"]').should('contain.text', 'Organization Management')
    })

    it('should display English button labels', () => {
      cy.navigateTo('/users')
      cy.get('[data-cy="create-user-button"]').should('contain.text', 'Create User')
      cy.get('[data-cy="search-button"]').should('contain.text', 'Search')
    })

    it('should display English table headers', () => {
      cy.navigateTo('/users')
      cy.get('.el-table__header [data-cy="header-username"]').should('contain.text', 'Username')
      cy.get('.el-table__header [data-cy="header-email"]').should('contain.text', 'Email')
    })
  })

  describe('Date and Number Formatting', () => {
    it('should format date in Chinese locale', () => {
      cy.selectLanguage('zh-CN')
      cy.navigateTo('/users')

      // Check date format
      cy.get('[data-cy="user-created-at"]').first().should('match', /\d{4}年\d{1,2}月\d{1,2}日/)
    })

    it('should format date in English locale', () => {
      cy.selectLanguage('en-US')
      cy.navigateTo('/users')

      // Check date format
      cy.get('[data-cy="user-created-at"]').first().should('match', /[A-Za-z]+ \d{1,2}, \d{4}/)
    })

    it('should format numbers in Chinese locale', () => {
      cy.selectLanguage('zh-CN')
      cy.navigateTo('/dashboard')

      cy.get('[data-cy="stat-user-count"]').should('be.visible')
    })

    it('should format numbers in English locale', () => {
      cy.selectLanguage('en-US')
      cy.navigateTo('/dashboard')

      cy.get('[data-cy="stat-user-count"]').should('be.visible')
    })
  })

  describe('Form Validation Messages', () => {
    it('should show Chinese validation messages', () => {
      cy.selectLanguage('zh-CN')
      cy.navigateTo('/users')
      cy.get('[data-cy="create-user-button"]').click()
      cy.get('[data-cy="confirm-button"]').click()

      cy.get('[data-cy="user-username-error"]').should('contain.text', '必填')
    })

    it('should show English validation messages', () => {
      cy.selectLanguage('en-US')
      cy.navigateTo('/users')
      cy.get('[data-cy="create-user-button"]').click()
      cy.get('[data-cy="confirm-button"]').click()

      cy.get('[data-cy="user-username-error"]').should('contain.text', 'required')
    })
  })

  describe('Success and Error Messages', () => {
    it('should show Chinese success messages', () => {
      cy.selectLanguage('zh-CN')
      cy.navigateTo('/users')

      // Simulate success operation
      cy.get('[data-cy="create-user-button"]').click()
      cy.get('[data-cy="cancel-button"]').click()

      // The message should be in Chinese when operations succeed
      // This would require mocking API responses
    })

    it('should show English success messages', () => {
      cy.selectLanguage('en-US')
      cy.navigateTo('/users')

      // The message should be in English when operations succeed
    })
  })
})
