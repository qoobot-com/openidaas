describe('User Management', () => {
  const testUser = {
    username: `testuser_${Date.now()}`,
    email: `testuser_${Date.now()}@example.com`,
    realName: 'Test User',
    phone: '13800138000',
    password: 'Test123456'
  }

  beforeEach(() => {
    cy.login()
    cy.navigateTo('/users')
  })

  describe('User List', () => {
    it('should display user list', () => {
      cy.get('[data-cy="user-list"]').should('be.visible')
      cy.get('.el-table__body tr').should('have.length.greaterThan', 0)
    })

    it('should search users by username', () => {
      cy.get('[data-cy="search-input"]').type('admin')
      cy.get('[data-cy="search-button"]').click()

      cy.wait('@apiRequest')
      cy.get('.el-table__body tr').should('have.length.at.least', 1)
    })

    it('should filter users by status', () => {
      cy.get('[data-cy="status-filter"]').click()
      cy.get('[data-cy="status-active"]').click()

      cy.wait('@apiRequest')
      cy.get('.el-table__body tr').should('have.length.at.least', 1)
    })

    it('should paginate users', () => {
      cy.get('[data-cy="pagination"]').should('be.visible')
      cy.get('[data-cy="next-page"]').click()

      cy.wait('@apiRequest')
      cy.url().should('include', 'page=')
    })
  })

  describe('Create User', () => {
    it('should open create user dialog', () => {
      cy.get('[data-cy="create-user-button"]').click()
      cy.get('[data-cy="user-dialog"]').should('be.visible')
    })

    it('should create a new user successfully', () => {
      cy.intercept('POST', '/api/v1/users').as('createUser')

      cy.get('[data-cy="create-user-button"]').click()

      cy.get('[data-cy="user-username"]').type(testUser.username)
      cy.get('[data-cy="user-email"]').type(testUser.email)
      cy.get('[data-cy="user-real-name"]').type(testUser.realName)
      cy.get('[data-cy="user-phone"]').type(testUser.phone)
      cy.get('[data-cy="user-password"]').type(testUser.password)

      cy.get('[data-cy="confirm-button"]').click()

      cy.wait('@createUser').then((xhr) => {
        expect(xhr.response?.statusCode).to.eq(201)
      })

      cy.checkSuccessMessage()
    })

    it('should show validation errors for invalid user data', () => {
      cy.get('[data-cy="create-user-button"]').click()
      cy.get('[data-cy="confirm-button"]').click()

      cy.get('[data-cy="user-username-error"]').should('be.visible')
      cy.get('[data-cy="user-email-error"]').should('be.visible')
      cy.get('[data-cy="user-password-error"]').should('be.visible')
    })

    it('should validate email format', () => {
      cy.get('[data-cy="create-user-button"]').click()
      cy.get('[data-cy="user-email"]').type('invalid-email')
      cy.get('[data-cy="user-email"]').blur()

      cy.get('[data-cy="user-email-error"]').should('contain.text', '邮箱格式')
    })
  })

  describe('Edit User', () => {
    it('should open edit user dialog', () => {
      cy.get('.el-table__body tr:first-child [data-cy="edit-button"]').click()
      cy.get('[data-cy="user-dialog"]').should('be.visible')
    })

    it('should update user information successfully', () => {
      cy.intercept('PUT', '/api/v1/users/*').as('updateUser')

      cy.get('.el-table__body tr:first-child [data-cy="edit-button"]').click()

      const updatedName = 'Updated Name'
      cy.get('[data-cy="user-real-name"]').clear().type(updatedName)
      cy.get('[data-cy="confirm-button"]').click()

      cy.wait('@updateUser').then((xhr) => {
        expect(xhr.response?.statusCode).to.eq(200)
      })

      cy.checkSuccessMessage()
    })
  })

  describe('Delete User', () => {
    it('should show delete confirmation dialog', () => {
      cy.get('.el-table__body tr:first-child [data-cy="delete-button"]').click()
      cy.get('[data-cy="delete-confirm-dialog"]').should('be.visible')
    })

    it('should delete user successfully', () => {
      cy.intercept('DELETE', '/api/v1/users/*').as('deleteUser')

      cy.get('.el-table__body tr:first-child [data-cy="delete-button"]').click()
      cy.get('[data-cy="confirm-delete-button"]').click()

      cy.wait('@deleteUser').then((xhr) => {
        expect(xhr.response?.statusCode).to.eq(204)
      })

      cy.checkSuccessMessage()
    })

    it('should cancel delete operation', () => {
      cy.get('.el-table__body tr:first-child [data-cy="delete-button"]').click()
      cy.get('[data-cy="cancel-delete-button"]').click()

      cy.get('[data-cy="delete-confirm-dialog"]').should('not.exist')
    })
  })

  describe('User Actions', () => {
    it('should enable a disabled user', () => {
      cy.intercept('PUT', '/api/v1/users/*/status').as('updateStatus')

      cy.get('[data-cy="status-filter"]').click()
      cy.get('[data-cy="status-disabled"]').click()

      cy.wait('@apiRequest')

      cy.get('.el-table__body tr:first-child [data-cy="toggle-status"]').click()

      cy.wait('@updateStatus').then((xhr) => {
        expect(xhr.response?.statusCode).to.eq(200)
      })

      cy.checkSuccessMessage()
    })

    it('should disable an enabled user', () => {
      cy.intercept('PUT', '/api/v1/users/*/status').as('updateStatus')

      cy.get('.el-table__body tr:first-child [data-cy="toggle-status"]').click()

      cy.wait('@updateStatus').then((xhr) => {
        expect(xhr.response?.statusCode).to.eq(200)
      })

      cy.checkSuccessMessage()
    })

    it('should reset user password', () => {
      cy.intercept('POST', '/api/v1/users/*/reset-password').as('resetPassword')

      cy.get('.el-table__body tr:first-child [data-cy="reset-password"]').click()
      cy.get('[data-cy="confirm-reset-password"]').click()

      cy.wait('@resetPassword').then((xhr) => {
        expect(xhr.response?.statusCode).to.eq(200)
      })

      cy.checkSuccessMessage()
    })
  })
})
