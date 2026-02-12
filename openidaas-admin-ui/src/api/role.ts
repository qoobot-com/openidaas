import { http } from '@/utils/http'

/**
 * 角色创建DTO
 */
export interface RoleCreateDTO {
  roleCode: string
  roleName: string
  roleType?: number
  parentId?: number
  description?: string
  sortOrder?: number
  enabled?: boolean
}

/**
 * 角色更新DTO
 */
export interface RoleUpdateDTO {
  id: number
  roleCode: string
  roleName: string
  roleType?: number
  parentId?: number
  description?: string
  sortOrder?: number
  enabled?: boolean
}

/**
 * 角色VO
 */
export interface RoleVO {
  id: number
  roleCode: string
  roleName: string
  roleType: number
  roleTypeDesc?: string
  parentId: number
  parentName?: string
  description?: string
  isBuiltin?: boolean
  enabled?: boolean
  sortOrder?: number
  createdAt?: string
  updatedAt?: string
  tenantId?: number
  children?: RoleVO[]
  userCount?: number
  permissionCount?: number
}

/**
 * 角色API
 */
export const roleApi = {
  /**
   * 获取角色列表
   */
  getRoleList: (roleType?: number) =>
    http.get<RoleVO[]>('/api/roles', { params: { roleType } }),

  /**
   * 获取角色树
   */
  getRoleTree: (parentId?: number) =>
    http.get<RoleVO[]>('/api/roles/tree', { params: { parentId } }),

  /**
   * 创建角色
   */
  createRole: (data: RoleCreateDTO) =>
    http.post<RoleVO>('/api/roles', data),

  /**
   * 更新角色
   */
  updateRole: (data: RoleUpdateDTO) =>
    http.put<RoleVO>('/api/roles', data),

  /**
   * 删除角色
   */
  deleteRole: (id: number) =>
    http.delete(`/api/roles/${id}`),

  /**
   * 获取角色详情
   */
  getRoleById: (id: number) =>
    http.get<RoleVO>(`/api/roles/${id}`),

  /**
   * 分配权限给角色
   */
  assignPermissions: (roleId: number, permIds: number[]) =>
    http.post(`/api/roles/${roleId}/permissions`, permIds),

  /**
   * 移除角色的权限
   */
  removePermissions: (roleId: number, permIds: number[]) =>
    http.delete(`/api/roles/${roleId}/permissions`, { data: permIds }),

  /**
   * 获取角色的权限列表
   */
  getRolePermissions: (roleId: number) =>
    http.get<number[]>(`/api/roles/${roleId}/permissions`),

  /**
   * 分配角色给用户
   */
  assignRoleToUser: (userId: number, roleId: number, scopeType?: number, scopeId?: number) =>
    http.post(`/api/roles/users/${userId}/roles/${roleId}`, null, {
      params: { scopeType, scopeId }
    }),

  /**
   * 移除用户的角色
   */
  removeRoleFromUser: (userId: number, roleId: number) =>
    http.delete(`/api/roles/users/${userId}/roles/${roleId}`),

  /**
   * 获取用户的角色列表
   */
  getUserRoles: (userId: number) =>
    http.get<RoleVO[]>(`/api/roles/users/${userId}/roles`)
}
