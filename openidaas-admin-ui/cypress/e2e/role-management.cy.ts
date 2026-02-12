describe('Role Management', () => {
  const testRole = {
    name: `Test Role ${Date.now()}`,
    code: `TEST_ROLE_${Date.now()}`,
    description: 'Test role description'
  }

  beforeEach(() => {
    cy.login()
    cy.navigateTo('/roles')
  })

  describe('Role List', () => {
    it('should display role list', () => {
      cy.get('[data-cy="role-list"]').should('be.visible')
      cy.get('.el-table__body tr').should('have.length.greaterThan', 0)
    })

    it('should search roles by name', () => {
      cy.get('[data-cy="search-input"]').type('admin')
      cy.get('[data-cy="search-button"]').click()

      cy.wait('@apiRequest')
      cy.get('.el-table__body tr').should('have.length.at.least', 1)
    })

    it('should filter roles by status', () => {
      cy.get('[data-cy="status-filter"]').click()
      cy.get('[data-cy="status-active"]').click()

      cy.wait('@apiRequest')
      cy.get('.el-table__body tr').should('have.length.at.least', 1)
    })
  })

  describe('Create Role', () => {
    it('should open create role dialog', () => {
      cy.get('[data-cy="create-role-button"]').click()
      cy.get('[data-cy="role-dialog"]').should('be.visible')
    })

    it('should create a new role successfully', () => {
      cy.intercept('POST', '/api/v1/roles').as('createRole')

      cy.get('[data-cy="create-role-button"]').click()

      cy.get('[data-cy="role-name"]').type(testRole.name)
      cy.get('[data-cy="role-code"]').type(testRole.code)
      cy.get('[data-cy="role-description"]').type(testRole.description)

      // 选择权限
      cy.get('[data-cy="permission-selector"]').click()
      cy.get('[data-cy="perm-user-read"]').click()
      cy.get('[data-cy="perm-role-read"]').click()

      cy.get('[data-cy="confirm-button"]').click()

      cy.wait('@createRole').then((xhr) => {
        expect(xhr.response?.statusCode).to.eq(201)
      })

      cy.checkSuccessMessage()
    })

    it('should show validation errors for invalid role data', () => {
      cy.get('[data-cy="create-role-button"]').click()
      cy.get('[data-cy="confirm-button"]').click()

      cy.get('[data-cy="role-name-error"]').should('be.visible')
      cy.get('[data-cy="role-code-error"]').should('be.visible')
    })
  })

  describe('Edit Role', () => {
    it('should open edit role dialog', () => {
      cy.get('.el-table__body tr:first-child [data-cy="edit-button"]').click()
      cy.get('[data-cy="role-dialog"]').should('be.visible')
    })

    it('should update role information successfully', () => {
      cy.intercept('PUT', '/api/v1/roles/*').as('updateRole')

      cy.get('.el-table__body tr:first-child [data-cy="edit-button"]').click()

      const updatedDescription = 'Updated description'
      cy.get('[data-cy="role-description"]').clear().type(updatedDescription)
      cy.get('[data-cy="confirm-button"]').click()

      cy.wait('@updateRole').then((xhr) => {
        expect(xhr.response?.statusCode).to.eq(200)
      })

      cy.checkSuccessMessage()
    })

    it('should update role permissions', () => {
      cy.intercept('PUT', '/api/v1/roles/*/permissions').as('updatePermissions')

      cy.get('.el-table__body tr:first-child [data-cy="edit-button"]').click()

      cy.get('[data-cy="permission-selector"]').click()
      cy.get('[data-cy="perm-user-write"]').click()
      cy.get('[data-cy="perm-role-write"]').click()

      cy.get('[data-cy="confirm-button"]').click()

      cy.wait('@updatePermissions').then((xhr) => {
        expect(xhr.response?.statusCode).to.eq(200)
      })

      cy.checkSuccessMessage()
    })
  })

  describe('Delete Role', () => {
    it('should show delete confirmation dialog', () => {
      cy.get('.el-table__body tr:first-child [data-cy="delete-button"]').click()
      cy.get('[data-cy="delete-confirm-dialog"]').should('be.visible')
    })

    it('should delete role successfully', () => {
      cy.intercept('DELETE', '/api/v1/roles/*').as('deleteRole')

      cy.get('.el-table__body tr:first-child [data-cy="delete-button"]').click()
      cy.get('[data-cy="confirm-delete-button"]').click()

      cy.wait('@deleteRole').then((xhr) => {
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

  describe('Permission Assignment', () => {
    it('should display permission tree', () => {
      cy.get('.el-table__body tr:first-child [data-cy="edit-button"]').click()
      cy.get('[data-cy="permission-tree"]').should('be.visible')
    })

    it('should expand and collapse permission groups', () => {
      cy.get('.el-table__body tr:first-child [data-cy="edit-button"]').click()

      cy.get('[data-cy="perm-group-users"]').click()
      cy.get('[data-cy="perm-group-roles"]').click()

      cy.get('[data-cy="perm-user-read"]').should('be.visible')
    })

    it('should select all permissions in a group', () => {
      cy.get('.el-table__body tr:first-child [data-cy="edit-button"]').click()

      cy.get('[data-cy="perm-group-users"]').click()
      cy.get('[data-cy="select-all-user-perms"]').check()

      cy.get('[data-cy="perm-user-read"]').should('be.checked')
      cy.get('[data-cy="perm-user-write"]').should('be.checked')
      cy.get('[data-cy="perm-user-delete"]').should('be.checked')
    })
  })
})
