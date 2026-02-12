describe('Authentication Flow', () => {
  const validUser = {
    username: 'admin',
    password: 'admin123'
  }

  const invalidUser = {
    username: 'invalid',
    password: 'wrongpassword'
  }

  beforeEach(() => {
    cy.visit('/login')
  })

  describe('Login Page', () => {
    it('should display login form', () => {
      cy.get('[data-cy="login-page"]').should('be.visible')
      cy.get('[data-cy="login-title"]').should('contain.text', '登录')
      cy.get('[data-cy="login-username"]').should('be.visible')
      cy.get('[data-cy="login-password"]').should('be.visible')
      cy.get('[data-cy="login-submit"]').should('be.visible')
    })

    it('should show validation errors for empty fields', () => {
      cy.get('[data-cy="login-submit"]').click()
      cy.get('[data-cy="login-username-error"]').should('be.visible')
      cy.get('[data-cy="login-password-error"]').should('be.visible')
    })

    it('should show error message for invalid credentials', () => {
      cy.get('[data-cy="login-username"]').type(invalidUser.username)
      cy.get('[data-cy="login-password"]').type(invalidUser.password)
      cy.get('[data-cy="login-submit"]').click()

      cy.wait('@loginRequest')
      cy.checkErrorMessage()
    })

    it('should login successfully with valid credentials', () => {
      cy.get('[data-cy="login-username"]').type(validUser.username)
      cy.get('[data-cy="login-password"]').type(validUser.password)
      cy.get('[data-cy="login-submit"]').click()

      cy.wait('@loginRequest').then((xhr) => {
        expect(xhr.response?.statusCode).to.eq(200)
      })

      cy.url().should('not.include', '/login')
      cy.url().should('include', '/dashboard')

      cy.get('[data-cy="user-dropdown"]').should('be.visible')
    })

    it('should remember username if "remember me" is checked', () => {
      cy.get('[data-cy="login-username"]').type(validUser.username)
      cy.get('[data-cy="login-password"]').type(validUser.password)
      cy.get('[data-cy="login-remember"]').check()
      cy.get('[data-cy="login-submit"]').click()

      cy.wait('@loginRequest')

      cy.reload()
      cy.get('[data-cy="login-username"]').should('have.value', validUser.username)
    })
  })

  describe('Logout', () => {
    beforeEach(() => {
      cy.login(validUser.username, validUser.password)
    })

    it('should logout successfully', () => {
      cy.get('[data-cy="user-dropdown"]').click()
      cy.get('[data-cy="logout-button"]').click()

      cy.wait('@logoutRequest')

      cy.url().should('include', '/login')
      cy.window().then((win) => {
        expect(win.localStorage.getItem('token')).to.be.null
      })
    })

    it('should redirect to login when accessing protected routes after logout', () => {
      cy.logout()
      cy.visit('/dashboard')
      cy.url().should('include', '/login')
    })
  })

  describe('Token Management', () => {
    it('should store token in localStorage after login', () => {
      cy.get('[data-cy="login-username"]').type(validUser.username)
      cy.get('[data-cy="login-password"]').type(validUser.password)
      cy.get('[data-cy="login-submit"]').click()

      cy.wait('@loginRequest')

      cy.window().then((win) => {
        const token = win.localStorage.getItem('token')
        expect(token).to.not.be.null
        expect(token).to.have.length.greaterThan(0)
      })
    })

    it('should clear token on logout', () => {
      cy.login(validUser.username, validUser.password)
      cy.logout()

      cy.window().then((win) => {
        expect(win.localStorage.getItem('token')).to.be.null
      })
    })

    it('should redirect to login when token is expired', () => {
      cy.login(validUser.username, validUser.password)

      // 模拟 token 过期
      cy.window().then((win) => {
        win.localStorage.removeItem('token')
      })

      cy.visit('/dashboard')
      cy.url().should('include', '/login')
    })
  })

  describe('Password Reset', () => {
    it('should show forgot password link', () => {
      cy.get('[data-cy="forgot-password-link"]').should('be.visible')
    })

    it('should navigate to forgot password page', () => {
      cy.get('[data-cy="forgot-password-link"]').click()
      cy.url().should('include', '/forgot-password')
    })

    it('should send password reset email', () => {
      cy.get('[data-cy="forgot-password-link"]').click()
      cy.get('[data-cy="reset-email-input"]').type('admin@example.com')
      cy.get('[data-cy="send-reset-link"]').click()

      cy.checkSuccessMessage()
    })
  })
})
