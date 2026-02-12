// 认证相关组合式函数
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

export const useAuth = () => {
  const authStore = useAuthStore()
  
  const isAuthenticated = computed(() => authStore.isAuthenticated)
  const userInfo = computed(() => authStore.userInfo)
  const permissions = computed(() => authStore.permissions)
  const roles = computed(() => authStore.roles)
  
  const login = async (loginData: any) => {
    return await authStore.login(loginData)
  }
  
  const logout = async () => {
    return await authStore.logout()
  }
  
  const hasPermission = (permission: string) => {
    return authStore.hasPermission(permission)
  }
  
  const hasAnyPermission = (permissions: string[]) => {
    return authStore.hasAnyPermission(permissions)
  }
  
  const hasRole = (role: string) => {
    return authStore.hasRole(role)
  }
  
  return {
    isAuthenticated,
    userInfo,
    permissions,
    roles,
    login,
    logout,
    hasPermission,
    hasAnyPermission,
    hasRole
  }
}