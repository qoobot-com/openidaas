import { http } from '@/utils/http'

/**
 * 部门创建DTO
 */
export interface DepartmentCreateDTO {
  deptCode: string
  deptName: string
  parentId?: number
  sortOrder?: number
  managerId?: number
  description?: string
}

/**
 * 部门更新DTO
 */
export interface DepartmentUpdateDTO {
  id: number
  deptName?: string
  parentId?: number
  sortOrder?: number
  managerId?: number
  description?: string
  status?: number
}

/**
 * 部门VO
 */
export interface DepartmentVO {
  id: number
  deptCode: string
  deptName: string
  parentId: number
  levelPath: string
  levelDepth: number
  sortOrder: number
  managerId?: number
  description?: string
  status: number
  createdAt: string
  updatedAt: string
  children?: DepartmentVO[]
}

/**
 * 职位创建请求
 */
export interface PositionCreateRequest {
  positionCode: string
  positionName: string
  deptId?: number
  level?: number
  jobGrade?: string
  reportsTo?: number
  isManager?: number
  description?: string
}

/**
 * 职位更新请求
 */
export interface PositionUpdateRequest {
  id: number
  positionCode: string
  positionName: string
  deptId?: number
  level?: number
  jobGrade?: string
  reportsTo?: number
  isManager?: number
  description?: string
}

/**
 * 职位VO
 */
export interface Position {
  id: number
  positionCode: string
  positionName: string
  deptId?: number
  deptName?: string
  level?: number
  jobGrade?: string
  reportsTo?: number
  reportsToName?: string
  isManager: number
  manager?: boolean
  description?: string
  createdAt?: string
  updatedAt?: string
  tenantId?: number
  userCount?: number
}

/**
 * 组织架构API
 */
export const organizationApi = {
  /**
   * 获取部门树
   */
  getDepartmentTree: (parentId?: number) =>
    http.get<DepartmentVO[]>('/api/organizations/departments', { params: { parentId } }),

  /**
   * 创建部门
   */
  createDepartment: (data: DepartmentCreateDTO) =>
    http.post<DepartmentVO>('/api/organizations/departments', data),

  /**
   * 更新部门
   */
  updateDepartment: (data: DepartmentUpdateDTO) =>
    http.put<DepartmentVO>('/api/organizations/departments', data),

  /**
   * 删除部门
   */
  deleteDepartment: (id: number) =>
    http.delete('/api/organizations/departments', { params: { id } }),

  /**
   * 获取部门详情
   */
  getDepartmentById: (id: number) =>
    http.get<DepartmentVO>(`/api/organizations/departments/${id}`),

  /**
   * 获取职位列表
   */
  getPositions: (deptId?: number) =>
    http.get<Position[]>('/api/organizations/positions', { params: { deptId } }),

  /**
   * 创建职位
   */
  createPosition: (data: PositionCreateRequest) =>
    http.post<Position>('/api/organizations/positions', data),

  /**
   * 更新职位
   */
  updatePosition: (data: PositionUpdateRequest) =>
    http.put<Position>('/api/organizations/positions', data),

  /**
   * 删除职位
   */
  deletePosition: (id: number) =>
    http.delete(`/api/organizations/positions/${id}`),

  /**
   * 获取职位详情
   */
  getPositionById: (id: number) =>
    http.get<Position>(`/api/organizations/positions/${id}`)
}
