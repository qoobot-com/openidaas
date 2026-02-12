import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import { LoginRequest, UserInfo } from '@/types'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref<string | null>(getToken())
  const userInfo = ref<UserInfo | null>(null)
  const isLoginLoading = ref(false)

  // 计算属性
  const isAuthenticated = computed(() => !!token.value)
  const permissions = computed(() => userInfo.value?.permissions || [])
  const roles = computed(() => userInfo.value?.roles || [])

  // Actions
  const login = async (loginData: LoginRequest) => {
    try {
      isLoginLoading.value = true
      const response = await authApi.login(loginData)
      
      token.value = response.accessToken
      userInfo.value = response.userInfo
      
      setToken(response.accessToken)
      
      return response
    } finally {
      isLoginLoading.value = false
    }
  }

  const logout = async () => {
    try {
      await authApi.logout()
    } finally {
      resetState()
    }
  }

  const refreshToken = async () => {
    if (!token.value) return false
    
    try {
      const response = await authApi.refreshToken({
        refreshToken: token.value
      })
      
      token.value = response.accessToken
      setToken(response.accessToken)
      return true
    } catch (error) {
      resetState()
      return false
    }
  }

  const getUserInfo = async () => {
    // 这里可以从API获取用户信息，或者从token中解析
    // 暂时返回已存储的用户信息
    return userInfo.value
  }

  const hasPermission = (permission: string): boolean => {
    return permissions.value.includes(permission)
  }

  const hasAnyPermission = (perms: string[]): boolean => {
    return perms.some(permission => hasPermission(permission))
  }

  const hasRole = (role: string): boolean => {
    return roles.value.includes(role)
  }

  const resetState = () => {
    token.value = null
    userInfo.value = null
    removeToken()
  }

  return {
    // 状态
    token,
    userInfo,
    isLoginLoading,
    
    // 计算属性
    isAuthenticated,
    permissions,
    roles,
    
    // Actions
    login,
    logout,
    refreshToken,
    getUserInfo,
    hasPermission,
    hasAnyPermission,
    hasRole,
    resetState
  }
})