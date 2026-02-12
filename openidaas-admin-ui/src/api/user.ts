import { http } from '@/utils/http'

/**
 * 用户查询DTO
 */
export interface UserQueryDTO {
  page?: number
  size?: number
  username?: string
  email?: string
  status?: number
  sort?: string
}

/**
 * 用户创建DTO
 */
export interface UserCreateDTO {
  username: string
  email?: string
  mobile?: string
  password: string
  profile?: {
    fullName?: string
    nickname?: string
    gender?: number
    birthDate?: string
    employeeId?: string
    avatarUrl?: string
  }
  departmentIds?: number[]
}

/**
 * 用户更新DTO
 */
export interface UserUpdateDTO {
  email?: string
  mobile?: string
  status?: number
  profile?: {
    fullName?: string
    nickname?: string
    gender?: number
    birthDate?: string
    employeeId?: string
    avatarUrl?: string
  }
}

/**
 * 用户密码DTO
 */
export interface UserPasswordDTO {
  userId: number
  newPassword: string
  confirmPassword: string
  resetBy?: number
  resetReason?: string
}

/**
 * 用户VO
 */
export interface UserVO {
  id: number
  username: string
  email?: string
  mobile?: string
  status: number
  createdAt: string
  updatedAt: string
  profile?: {
    fullName?: string
    nickname?: string
    gender?: number
    birthDate?: string
    avatarUrl?: string
    employeeId?: string
    hireDate?: string
  }
  departments?: Array<{
    deptId: number
    positionId?: number
    isPrimary: boolean
    startDate?: string
    endDate?: string
  }>
  roles?: Array<{
    roleId: number
    scopeType?: number
    scopeId?: number
    expireTime?: string
  }>
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

/**
 * 用户API
 */
export const userApi = {
  /**
   * 查询用户列表
   */
  getUsers: (params: UserQueryDTO) => http.get<PageResult<UserVO>>('/api/users', { params }),

  /**
   * 获取用户详情
   */
  getUserById: (id: number) => http.get<UserVO>(`/api/users/${id}`),

  /**
   * 创建用户
   */
  createUser: (data: UserCreateDTO) => http.post<UserVO>('/api/users', data),

  /**
   * 更新用户
   */
  updateUser: (id: number, data: UserUpdateDTO) => http.put<UserVO>(`/api/users/${id}`, data),

  /**
   * 删除用户
   */
  deleteUser: (id: number) => http.delete(`/api/users/${id}`),

  /**
   * 重置密码
   */
  resetPassword: (id: number, data: Omit<UserPasswordDTO, 'userId'>) =>
    http.post(`/api/users/${id}/reset-password`, { ...data, userId: id }),

  /**
   * 分配用户部门
   */
  assignDepartments: (id: number, data: { deptIds: number[]; positionId?: number; isPrimary?: boolean }) =>
    http.post(`/api/users/${id}/departments`, data),

  /**
   * 分配用户角色
   */
  assignRoles: (id: number, data: { roleIds: number[]; scopeType?: number; scopeId?: number }) =>
    http.post(`/api/users/${id}/roles`, data),

  /**
   * 移除用户角色
   */
  removeRoles: (id: number, roleIds: number[]) => http.delete(`/api/users/${id}/roles`, { data: roleIds }),

  /**
   * 锁定用户
   */
  lockUser: (id: number) => http.post(`/api/users/${id}/lock`),

  /**
   * 解锁用户
   */
  unlockUser: (id: number) => http.post(`/api/users/${id}/unlock`),

  /**
   * 停用用户
   */
  disableUser: (id: number) => http.post(`/api/users/${id}/disable`),

  /**
   * 启用用户
   */
  enableUser: (id: number) => http.post(`/api/users/${id}/enable`)
}
