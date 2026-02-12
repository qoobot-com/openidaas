describe('Navigation', () => {
  beforeEach(() => {
    cy.login()
  })

  describe('Sidebar Navigation', () => {
    it('should display sidebar', () => {
      cy.get('[data-cy="sidebar"]').should('be.visible')
    })

    it('should display all navigation items', () => {
      cy.get('[data-cy="nav-item-dashboard"]').should('be.visible')
      cy.get('[data-cy="nav-item-users"]').should('be.visible')
      cy.get('[data-cy="nav-item-roles"]').should('be.visible')
      cy.get('[data-cy="nav-item-organizations"]').should('be.visible')
      cy.get('[data-cy="nav-item-audit-logs"]').should('be.visible')
    })

    it('should expand and collapse menu items', () => {
      cy.get('[data-cy="nav-item-system"]').click()
      cy.get('[data-cy="nav-sub-item-settings"]').should('be.visible')

      cy.get('[data-cy="nav-item-system"]').click()
      cy.get('[data-cy="nav-sub-item-settings"]').should('not.be.visible')
    })

    it('should navigate to dashboard', () => {
      cy.get('[data-cy="nav-item-dashboard"]').click()
      cy.url().should('include', '/dashboard')
    })

    it('should navigate to users page', () => {
      cy.get('[data-cy="nav-item-users"]').click()
      cy.url().should('include', '/users')
    })

    it('should navigate to roles page', () => {
      cy.get('[data-cy="nav-item-roles"]').click()
      cy.url().should('include', '/roles')
    })

    it('should navigate to organizations page', () => {
      cy.get('[data-cy="nav-item-organizations"]').click()
      cy.url().should('include', '/organizations')
    })
  })

  describe('Breadcrumb Navigation', () => {
    it('should display breadcrumb on dashboard', () => {
      cy.navigateTo('/dashboard')
      cy.get('[data-cy="breadcrumb"]').should('be.visible')
      cy.get('[data-cy="breadcrumb-item-dashboard"]').should('be.visible')
    })

    it('should display breadcrumb on nested pages', () => {
      cy.navigateTo('/users/detail/1')
      cy.get('[data-cy="breadcrumb"]').should('be.visible')
      cy.get('[data-cy="breadcrumb-item-users"]').should('be.visible')
      cy.get('[data-cy="breadcrumb-item-detail"]').should('be.visible')
    })

    it('should navigate using breadcrumb', () => {
      cy.navigateTo('/users/detail/1')
      cy.get('[data-cy="breadcrumb-item-users"]').click()
      cy.url().should('include', '/users')
    })
  })

  describe('Page Transitions', () => {
    it('should show loading indicator during page load', () => {
      cy.get('[data-cy="nav-item-users"]').click()
      cy.get('.el-loading-mask', { timeout: 3000 }).should('be.visible')
    })

    it('should complete page transition', () => {
      cy.get('[data-cy="nav-item-users"]').click()
      cy.waitForPageLoad()
      cy.url().should('include', '/users')
    })
  })

  describe('Back and Forward Navigation', () => {
    it('should navigate back using browser back button', () => {
      cy.navigateTo('/users')
      cy.navigateTo('/roles')
      cy.go('back')
      cy.url().should('include', '/users')
    })

    it('should navigate forward using browser forward button', () => {
      cy.navigateTo('/users')
      cy.navigateTo('/roles')
      cy.go('back')
      cy.go('forward')
      cy.url().should('include', '/roles')
    })
  })

  describe('Mobile Navigation', () => {
    beforeEach(() => {
      cy.viewport('iphone-x')
    })

    it('should show hamburger menu on mobile', () => {
      cy.get('[data-cy="mobile-menu-button"]').should('be.visible')
    })

    it('should open sidebar on mobile', () => {
      cy.get('[data-cy="mobile-menu-button"]').click()
      cy.get('[data-cy="sidebar"]').should('be.visible')
      cy.get('[data-cy="sidebar"]').should('have.class', 'is-open')
    })

    it('should close sidebar on mobile', () => {
      cy.get('[data-cy="mobile-menu-button"]').click()
      cy.get('[data-cy="sidebar-overlay"]').click()
      cy.get('[data-cy="sidebar"]').should('not.have.class', 'is-open')
    })

    it('should navigate and close sidebar on mobile', () => {
      cy.get('[data-cy="mobile-menu-button"]').click()
      cy.get('[data-cy="nav-item-users"]').click()
      cy.url().should('include', '/users')
      cy.get('[data-cy="sidebar"]').should('not.have.class', 'is-open')
    })
  })

  describe('Direct URL Navigation', () => {
    it('should navigate to valid URL', () => {
      cy.visit('/users')
      cy.url().should('include', '/users')
    })

    it('should redirect to 404 for invalid URL', () => {
      cy.visit('/invalid-page')
      cy.url().should('include', '/404')
      cy.get('[data-cy="error-page"]').should('be.visible')
    })

    it('should redirect to login for protected routes without auth', () => {
      cy.clearLocalStorage()
      cy.visit('/users')
      cy.url().should('include', '/login')
    })
  })
})
