describe('Organization Management', () => {
  const testOrg = {
    name: `Test Organization ${Date.now()}`,
    code: `TEST_ORG_${Date.now()}`,
    description: 'Test organization description'
  }

  beforeEach(() => {
    cy.login()
    cy.navigateTo('/organizations')
  })

  describe('Organization Tree', () => {
    it('should display organization tree', () => {
      cy.get('[data-cy="org-tree"]').should('be.visible')
      cy.get('.el-tree-node').should('have.length.greaterThan', 0)
    })

    it('should expand and collapse organization nodes', () => {
      cy.get('[data-cy="org-tree"]').get('.el-tree-node__expand-icon').first().click()
      cy.get('[data-cy="org-tree"]').get('.el-tree-node__expand-icon').first().click()
    })

    it('should select organization node', () => {
      cy.get('[data-cy="org-tree"]').get('.el-tree-node__content').first().click()
      cy.get('[data-cy="selected-org-info"]').should('be.visible')
    })
  })

  describe('Search Organizations', () => {
    it('should search organizations by name', () => {
      cy.get('[data-cy="search-input"]').type('test')
      cy.get('[data-cy="search-button"]').click()

      cy.wait('@apiRequest')
      cy.get('.el-tree-node').should('have.length.at.least', 1)
    })

    it('should filter organizations by status', () => {
      cy.get('[data-cy="status-filter"]').click()
      cy.get('[data-cy="status-active"]').click()

      cy.wait('@apiRequest')
      cy.get('.el-tree-node').should('have.length.at.least', 1)
    })
  })

  describe('Create Organization', () => {
    it('should open create organization dialog', () => {
      cy.get('[data-cy="create-org-button"]').click()
      cy.get('[data-cy="org-dialog"]').should('be.visible')
    })

    it('should create root organization successfully', () => {
      cy.intercept('POST', '/api/v1/organizations').as('createOrg')

      cy.get('[data-cy="create-org-button"]').click()

      cy.get('[data-cy="org-name"]').type(testOrg.name)
      cy.get('[data-cy="org-code"]').type(testOrg.code)
      cy.get('[data-cy="org-description"]').type(testOrg.description)

      cy.get('[data-cy="confirm-button"]').click()

      cy.wait('@createOrg').then((xhr) => {
        expect(xhr.response?.statusCode).to.eq(201)
      })

      cy.checkSuccessMessage()
    })

    it('should create child organization successfully', () => {
      cy.intercept('POST', '/api/v1/organizations').as('createOrg')

      // Select parent organization
      cy.get('.el-tree-node__content').first().click()
      cy.get('[data-cy="create-org-button"]').click()

      cy.get('[data-cy="org-name"]').type(testOrg.name)
      cy.get('[data-cy="org-code"]').type(testOrg.code)

      cy.get('[data-cy="confirm-button"]').click()

      cy.wait('@createOrg').then((xhr) => {
        expect(xhr.response?.statusCode).to.eq(201)
      })

      cy.checkSuccessMessage()
    })

    it('should show validation errors for invalid org data', () => {
      cy.get('[data-cy="create-org-button"]').click()
      cy.get('[data-cy="confirm-button"]').click()

      cy.get('[data-cy="org-name-error"]').should('be.visible')
      cy.get('[data-cy="org-code-error"]').should('be.visible')
    })
  })

  describe('Edit Organization', () => {
    it('should open edit organization dialog', () => {
      cy.get('.el-tree-node__content').first().click()
      cy.get('[data-cy="edit-org-button"]').click()
      cy.get('[data-cy="org-dialog"]').should('be.visible')
    })

    it('should update organization information successfully', () => {
      cy.intercept('PUT', '/api/v1/organizations/*').as('updateOrg')

      cy.get('.el-tree-node__content').first().click()
      cy.get('[data-cy="edit-org-button"]').click()

      const updatedDescription = 'Updated description'
      cy.get('[data-cy="org-description"]').clear().type(updatedDescription)
      cy.get('[data-cy="confirm-button"]').click()

      cy.wait('@updateOrg').then((xhr) => {
        expect(xhr.response?.statusCode).to.eq(200)
      })

      cy.checkSuccessMessage()
    })

    it('should update organization status', () => {
      cy.intercept('PUT', '/api/v1/organizations/*/status').as('updateStatus')

      cy.get('.el-tree-node__content').first().click()
      cy.get('[data-cy="toggle-status"]').click()

      cy.wait('@updateStatus').then((xhr) => {
        expect(xhr.response?.statusCode).to.eq(200)
      })

      cy.checkSuccessMessage()
    })
  })

  describe('Delete Organization', () => {
    it('should show delete confirmation dialog', () => {
      cy.get('.el-tree-node__content').first().click()
      cy.get('[data-cy="delete-org-button"]').click()
      cy.get('[data-cy="delete-confirm-dialog"]').should('be.visible')
    })

    it('should delete organization successfully', () => {
      cy.intercept('DELETE', '/api/v1/organizations/*').as('deleteOrg')

      cy.get('.el-tree-node__content').first().click()
      cy.get('[data-cy="delete-org-button"]').click()
      cy.get('[data-cy="confirm-delete-button"]').click()

      cy.wait('@deleteOrg').then((xhr) => {
        expect(xhr.response?.statusCode).to.eq(204)
      })

      cy.checkSuccessMessage()
    })

    it('should cancel delete operation', () => {
      cy.get('.el-tree-node__content').first().click()
      cy.get('[data-cy="delete-org-button"]').click()
      cy.get('[data-cy="cancel-delete-button"]').click()

      cy.get('[data-cy="delete-confirm-dialog"]').should('not.exist')
    })

    it('should show warning when deleting organization with children', () => {
      // Select an organization with children
      cy.get('.el-tree-node__expand-icon').first().click()

      // Find a node with children and try to delete
      cy.get('.el-tree-node__content').first().click()
      cy.get('[data-cy="delete-org-button"]').click()

      cy.get('[data-cy="delete-confirm-dialog"]').should('contain.text', '子组织')
    })
  })

  describe('Organization Details', () => {
    it('should display organization details', () => {
      cy.get('.el-tree-node__content').first().click()
      cy.get('[data-cy="org-details-panel"]').should('be.visible')

      cy.get('[data-cy="detail-name"]').should('be.visible')
      cy.get('[data-cy="detail-code"]').should('be.visible')
      cy.get('[data-cy="detail-description"]').should('be.visible')
      cy.get('[data-cy="detail-status"]').should('be.visible')
    })

    it('should display organization statistics', () => {
      cy.get('.el-tree-node__content').first().click()

      cy.get('[data-cy="stat-user-count"]').should('be.visible')
      cy.get('[data-cy="stat-sub-org-count"]').should('be.visible')
      cy.get('[data-cy="stat-role-count"]').should('be.visible')
    })

    it('should display organization members', () => {
      cy.get('.el-tree-node__content').first().click()
      cy.get('[data-cy="org-members-tab"]').click()

      cy.get('[data-cy="member-list"]').should('be.visible')
    })
  })

  describe('Drag and Drop Organizations', () => {
    it('should allow moving organization to new parent', () => {
      cy.intercept('PUT', '/api/v1/organizations/*/move').as('moveOrg')

      // Drag first node to second node
      cy.get('.el-tree-node__content')
        .first()
        .drag('.el-tree-node__content').eq(1)

      cy.wait('@moveOrg').then((xhr) => {
        expect(xhr.response?.statusCode).to.eq(200)
      })

      cy.checkSuccessMessage()
    })
  })
})
