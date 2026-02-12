import { http } from '@/utils/http'

/**
 * 授权API
 */
export const authorizationApi = {
  /**
   * 检查用户权限
   */
  checkPermission: (userId: number, permCode: string) =>
    http.get<boolean>('/api/authorization/check-permission', {
      params: { userId, permCode }
    }),

  /**
   * 检查用户是否有任意权限
   */
  checkAnyPermission: (userId: number, permCodes: string[]) =>
    http.post<boolean>('/api/authorization/check-any-permission', permCodes, {
      params: { userId }
    }),

  /**
   * 检查用户是否拥有所有权限
   */
  checkAllPermissions: (userId: number, permCodes: string[]) =>
    http.post<boolean>('/api/authorization/check-all-permissions', permCodes, {
      params: { userId }
    }),

  /**
   * 检查用户角色
   */
  checkRole: (userId: number, roleCode: string) =>
    http.get<boolean>('/api/authorization/check-role', {
      params: { userId, roleCode }
    }),

  /**
   * 检查用户是否有任意角色
   */
  checkAnyRole: (userId: number, roleCodes: string[]) =>
    http.post<boolean>('/api/authorization/check-any-role', roleCodes, {
      params: { userId }
    }),

  /**
   * 检查用户是否拥有所有角色
   */
  checkAllRoles: (userId: number, roleCodes: string[]) =>
    http.post<boolean>('/api/authorization/check-all-roles', roleCodes, {
      params: { userId }
    }),

  /**
   * 检查资源访问权限
   */
  checkResourceAccess: (userId: number, resourceType: string, resourceId: string, action: string) =>
    http.get<boolean>('/api/authorization/check-resource', {
      params: { userId, resourceType, resourceId, action }
    }),

  /**
   * 获取用户权限列表
   */
  getUserPermissions: (userId: number) =>
    http.get<string[]>(`/api/authorization/users/${userId}/permissions`),

  /**
   * 获取用户角色列表
   */
  getUserRoles: (userId: number) =>
    http.get<string[]>(`/api/authorization/users/${userId}/roles`),

  /**
   * 清除用户权限缓存
   */
  clearUserCache: (userId: number) =>
    http.delete(`/api/authorization/users/${userId}/cache`)
}
