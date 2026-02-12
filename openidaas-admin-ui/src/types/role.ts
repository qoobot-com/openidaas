// 角色权限相关类型
export interface Role {
  id: number
  roleCode: string
  roleName: string
  roleType: number
  parentId: number
  description?: string
}

export interface Permission {
  id: number
  permCode: string
  permName: string
  resourceType: string
  resourceId: string
  action: string
}

export interface CreateRoleRequest {
  roleCode: string
  roleName: string
  roleType?: number
  parentId?: number
  description?: string
  permissionIds?: number[]
}

export interface AssignPermissionRequest {
  permissionIds: number[]
}

export interface PageResponseRole {
  content: Role[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}