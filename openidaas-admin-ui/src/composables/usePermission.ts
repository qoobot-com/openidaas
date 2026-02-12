// 权限检查组合式函数
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

export const usePermission = () => {
  const authStore = useAuthStore()
  
  const permissions = computed(() => authStore.permissions)
  const roles = computed(() => authStore.roles)
  
  // 检查是否有指定权限
  const hasPermission = (permission: string): boolean => {
    return authStore.hasPermission(permission)
  }
  
  // 检查是否有任意一个权限
  const hasAnyPermission = (permissionList: string[]): boolean => {
    return authStore.hasAnyPermission(permissionList)
  }
  
  // 检查是否有所有权限
  const hasAllPermissions = (permissionList: string[]): boolean => {
    return permissionList.every(permission => hasPermission(permission))
  }
  
  // 检查是否有指定角色
  const hasRole = (role: string): boolean => {
    return authStore.hasRole(role)
  }
  
  // 检查是否有任意一个角色
  const hasAnyRole = (roleList: string[]): boolean => {
    return roleList.some(role => hasRole(role))
  }
  
  // 检查是否有所有角色
  const hasAllRoles = (roleList: string[]): boolean => {
    return roleList.every(role => hasRole(role))
  }
  
  // 路由权限检查
  const checkRoutePermission = (routeMeta: any): boolean => {
    if (!routeMeta?.permissions) {
      return true
    }
    
    const requiredPermissions = Array.isArray(routeMeta.permissions) 
      ? routeMeta.permissions 
      : [routeMeta.permissions]
      
    return hasAnyPermission(requiredPermissions)
  }
  
  return {
    permissions,
    roles,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    hasRole,
    hasAnyRole,
    hasAllRoles,
    checkRoutePermission
  }
}