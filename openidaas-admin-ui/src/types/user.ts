// 用户相关类型
export interface User {
  id: number
  username: string
  email?: string
  mobile?: string
  status: UserStatus
  createdAt: string
  updatedAt: string
}

export interface UserDetail extends User {
  profile?: UserProfile
  departments?: UserDepartment[]
  roles?: UserRoleAssignment[]
}

export interface UserProfile {
  fullName?: string
  nickname?: string
  gender?: number
  birthDate?: string
  avatarUrl?: string
  employeeId?: string
}

export interface UserDepartment {
  deptId: number
  deptName: string
  isPrimary: boolean
  positionId?: number
  positionName?: string
}

export interface UserRoleAssignment {
  roleId: number
  roleName: string
  scopeType?: number
  scopeId?: number
  expireTime?: string
}

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  LOCKED = 'LOCKED',
  DISABLED = 'DISABLED',
  DELETED = 'DELETED'
}

export interface CreateUserRequest {
  username: string
  email?: string
  password: string
  profile?: UserProfile
  departmentIds?: number[]
}

export interface UpdateUserRequest {
  email?: string
  mobile?: string
  profile?: UserProfile
  status?: UserStatus
}

export interface AssignDepartmentRequest {
  deptId: number
  positionId?: number
  isPrimary: boolean
  startDate?: string
  endDate?: string
}

export interface AssignRoleRequest {
  roleId: number
  scopeType?: number
  scopeId?: number
  expireTime?: string
  grantReason?: string
}

export interface PageResponseUser {
  content: User[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}