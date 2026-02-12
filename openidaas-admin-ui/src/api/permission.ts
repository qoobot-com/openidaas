import { http } from '@/utils/http'

/**
 * 权限创建DTO
 */
export interface PermissionCreateDTO {
  permCode: string
  permName: string
  permType: string
  parentId?: number
  path?: string
  component?: string
  icon?: string
  sortOrder?: number
  description?: string
}

/**
 * 权限更新DTO
 */
export interface PermissionUpdateDTO {
  id: number
  permCode?: string
  permName?: string
  permType?: string
  parentId?: number
  path?: string
  component?: string
  icon?: string
  sortOrder?: number
  description?: string
}

/**
 * 权限VO
 */
export interface PermissionVO {
  id: number
  permCode: string
  permName: string
  permType: string
  permTypeDesc?: string
  parentId: number
  path?: string
  component?: string
  icon?: string
  sortOrder: number
  description?: string
  createdAt?: string
  updatedAt?: string
  tenantId?: number
  children?: PermissionVO[]
}

/**
 * 权限API
 */
export const permissionApi = {
  /**
   * 获取所有权限
   */
  getAllPermissions: () =>
    http.get<PermissionVO[]>('/api/permissions'),

  /**
   * 获取权限树
   */
  getPermissionTree: (parentId?: number) =>
    http.get<PermissionVO[]>('/api/permissions/tree', { params: { parentId } }),

  /**
   * 创建权限
   */
  createPermission: (data: PermissionCreateDTO) =>
    http.post<PermissionVO>('/api/permissions', data),

  /**
   * 更新权限
   */
  updatePermission: (data: PermissionUpdateDTO) =>
    http.put<PermissionVO>('/api/permissions', data),

  /**
   * 删除权限
   */
  deletePermission: (id: number) =>
    http.delete(`/api/permissions/${id}`),

  /**
   * 获取权限详情
   */
  getPermissionById: (id: number) =>
    http.get<PermissionVO>(`/api/permissions/${id}`)
}
