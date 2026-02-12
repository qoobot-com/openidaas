import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'

describe('Auth Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should initialize with default values', () => {
    const store = useAuthStore()
    
    expect(store.token).toBeNull()
    expect(store.userInfo).toBeNull()
    expect(store.isLoginLoading).toBe(false)
    expect(store.isAuthenticated).toBe(false)
    expect(store.permissions).toEqual([])
    expect(store.roles).toEqual([])
  })

  it('should handle login successfully', async () => {
    const store = useAuthStore()
    const mockLoginData = {
      username: 'testuser',
      password: 'password123'
    }
    
    const mockResponse = {
      accessToken: 'mock-token',
      refreshToken: 'mock-refresh-token',
      expiresIn: 3600,
      tokenType: 'Bearer',
      userInfo: {
        id: 1,
        username: 'testuser',
        fullName: 'Test User',
        avatar: '',
        permissions: ['user:list', 'user:create'],
        roles: ['USER']
      }
    }
    
    // Mock the API call
    vi.spyOn(store, 'login').mockResolvedValue(mockResponse)
    
    const result = await store.login(mockLoginData)
    
    expect(result).toEqual(mockResponse)
    expect(store.token).toBe('mock-token')
    expect(store.userInfo).toEqual(mockResponse.userInfo)
    expect(store.isAuthenticated).toBe(true)
  })

  it('should handle logout', async () => {
    const store = useAuthStore()
    
    // 先模拟登录状态
    store.token = 'mock-token'
    store.userInfo = {
      id: 1,
      username: 'testuser',
      fullName: 'Test User',
      avatar: '',
      permissions: ['user:list'],
      roles: ['USER']
    }
    
    // Mock the API call
    vi.spyOn(store, 'logout').mockResolvedValue()
    
    await store.logout()
    
    expect(store.token).toBeNull()
    expect(store.userInfo).toBeNull()
    expect(store.isAuthenticated).toBe(false)
  })

  it('should check permissions correctly', () => {
    const store = useAuthStore()
    
    // 设置用户权限
    store.userInfo = {
      id: 1,
      username: 'testuser',
      fullName: 'Test User',
      avatar: '',
      permissions: ['user:list', 'user:create', 'role:manage'],
      roles: ['USER']
    }
    
    expect(store.hasPermission('user:list')).toBe(true)
    expect(store.hasPermission('user:delete')).toBe(false)
    expect(store.hasAnyPermission(['user:list', 'user:delete'])).toBe(true)
    expect(store.hasAnyPermission(['user:update', 'user:delete'])).toBe(false)
  })
})