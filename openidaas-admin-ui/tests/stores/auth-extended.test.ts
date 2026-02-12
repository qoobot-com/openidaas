import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'

describe('useAuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('Initial State', () => {
    it('should have correct initial state', () => {
      const authStore = useAuthStore()
      
      expect(authStore.token).toBe('')
      expect(authStore.userInfo).toBeNull()
      expect(authStore.permissions).toEqual([])
      expect(authStore.isAuthenticated).toBe(false)
    })
  })

  describe('Set Token', () => {
    it('should set token', () => {
      const authStore = useAuthStore()
      const token = 'test-token-123'
      
      authStore.setToken(token)
      
      expect(authStore.token).toBe(token)
      expect(authStore.isAuthenticated).toBe(true)
    })

    it('should clear token', () => {
      const authStore = useAuthStore()
      authStore.setToken('test-token')
      
      authStore.clearToken()
      
      expect(authStore.token).toBe('')
      expect(authStore.isAuthenticated).toBe(false)
    })
  })

  describe('Set User Info', () => {
    it('should set user info', () => {
      const authStore = useAuthStore()
      const userInfo = {
        id: 1,
        username: 'testuser',
        email: 'test@example.com',
        role: 'admin'
      }
      
      authStore.setUserInfo(userInfo)
      
      expect(authStore.userInfo).toEqual(userInfo)
    })

    it('should clear user info', () => {
      const authStore = useAuthStore()
      authStore.setUserInfo({
        id: 1,
        username: 'test',
        email: 'test@test.com',
        role: 'admin'
      })
      
      authStore.clearUserInfo()
      
      expect(authStore.userInfo).toBeNull()
    })
  })

  describe('Set Permissions', () => {
    it('should set permissions', () => {
      const authStore = useAuthStore()
      const permissions = ['user:read', 'user:write', 'role:read']
      
      authStore.setPermissions(permissions)
      
      expect(authStore.permissions).toEqual(permissions)
    })

    it('should clear permissions', () => {
      const authStore = useAuthStore()
      authStore.setPermissions(['user:read'])
      
      authStore.clearPermissions()
      
      expect(authStore.permissions).toEqual([])
    })
  })

  describe('Has Permission', () => {
    it('should return true for existing permission', () => {
      const authStore = useAuthStore()
      authStore.setPermissions(['user:read', 'user:write'])
      
      expect(authStore.hasPermission('user:read')).toBe(true)
    })

    it('should return false for non-existing permission', () => {
      const authStore = useAuthStore()
      authStore.setPermissions(['user:read'])
      
      expect(authStore.hasPermission('user:write')).toBe(false)
    })

    it('should return true for wildcard permission', () => {
      const authStore = useAuthStore()
      authStore.setPermissions(['*'])
      
      expect(authStore.hasPermission('any:permission')).toBe(true)
    })
  })

  describe('Has Role', () => {
    it('should return true for existing role', () => {
      const authStore = useAuthStore()
      authStore.setUserInfo({
        id: 1,
        username: 'admin',
        email: 'admin@example.com',
        role: 'admin'
      })
      
      expect(authStore.hasRole('admin')).toBe(true)
    })

    it('should return false for non-existing role', () => {
      const authStore = useAuthStore()
      authStore.setUserInfo({
        id: 1,
        username: 'user',
        email: 'user@example.com',
        role: 'user'
      })
      
      expect(authStore.hasRole('admin')).toBe(false)
    })
  })

  describe('Logout', () => {
    it('should clear all auth data on logout', () => {
      const authStore = useAuthStore()
      authStore.setToken('test-token')
      authStore.setUserInfo({
        id: 1,
        username: 'test',
        email: 'test@test.com',
        role: 'admin'
      })
      authStore.setPermissions(['user:read'])
      
      authStore.logout()
      
      expect(authStore.token).toBe('')
      expect(authStore.userInfo).toBeNull()
      expect(authStore.permissions).toEqual([])
      expect(authStore.isAuthenticated).toBe(false)
    })
  })
})
