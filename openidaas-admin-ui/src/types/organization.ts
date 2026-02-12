// 组织架构相关类型
export interface Department {
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
}

export interface DepartmentTree extends Department {
  children?: DepartmentTree[]
}

export interface Position {
  id: number
  positionCode: string
  positionName: string
  deptId: number
  level?: number
  jobGrade?: string
  reportsTo?: number
  isManager?: boolean
  description?: string
}

export interface CreateDepartmentRequest {
  deptCode: string
  deptName: string
  parentId?: number
  sortOrder?: number
  managerId?: number
  description?: string
}

export interface UpdateDepartmentRequest {
  id: number
  deptName?: string
  parentId?: number
  sortOrder?: number
  managerId?: number
  description?: string
  status?: number
}

export interface CreatePositionRequest {
  positionCode: string
  positionName: string
  deptId: number
  level?: number
  jobGrade?: string
  reportsTo?: number
  isManager?: boolean
  description?: string
}