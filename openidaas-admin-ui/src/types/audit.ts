// 审计日志相关类型

/**
 * 审计日志
 */
export interface AuditLog {
  id: number
  operationType: string
  operationDesc?: string
  module?: string
  subModule?: string
  targetType?: string
  targetId?: number
  targetName?: string
  requestUrl?: string
  requestMethod?: string
  requestParams?: string
  responseResult?: string
  operatorId?: number
  operatorName?: string
  operatorIp?: string
  userAgent?: string
  operationTime: string
  executionTime?: number
  result?: string
  errorMessage?: string
  tenantId?: number
  appId?: number
  createdAt: string
}

/**
 * 审计日志查询条件
 */
export interface AuditLogQuery {
  operationType?: string
  module?: string
  subModule?: string
  targetType?: string
  targetId?: number
  operatorId?: number
  operatorName?: string
  result?: string
  tenantId?: number
  appId?: number
  startTime?: string
  endTime?: string
  page?: number
  size?: number
}

/**
 * 审计日志创建请求
 */
export interface AuditLogCreate {
  operationType: string
  operationDesc?: string
  module?: string
  subModule?: string
  targetType?: string
  targetId?: number
  targetName?: string
  requestUrl?: string
  requestMethod?: string
  requestParams?: string
  responseResult?: string
  operatorId: number
  operatorName?: string
  operatorIp?: string
  userAgent?: string
  operationTime?: string
  executionTime?: number
  result?: string
  errorMessage?: string
  tenantId?: number
  appId?: number
}

/**
 * 审计统计数据
 */
export interface AuditStatistics {
  totalOperations: number
  successCount: number
  failureCount: number
  operationTypeDistribution: Record<string, number>
  moduleDistribution: Record<string, number>
  topUsers: Record<string, number>
}

/**
 * 分页响应
 */
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

/**
 * 权限检查请求（保留兼容性）
 */
export interface PermissionCheckRequest {
  userId?: number
  permissions: string[]
}

/**
 * 权限检查响应（保留兼容性）
 */
export interface PermissionCheckResponse {
  userId: number
  results: Record<string, boolean>
}

/**
 * 安全事件严重程度枚举
 */
export enum SecuritySeverity {
  LOW = 1,
  MEDIUM = 2,
  HIGH = 3,
  EMERGENCY = 4
}

/**
 * 安全事件类型枚举
 */
export enum SecurityEventType {
  LOGIN_FAILED = 'LOGIN_FAILED',
  PASSWORD_ERROR = 'PASSWORD_ERROR',
  ABNORMAL_LOGIN = 'ABNORMAL_LOGIN',
  UNAUTHORIZED_ACCESS = 'UNAUTHORIZED_ACCESS',
  DATA_LEAK = 'DATA_LEAK',
  BRUTE_FORCE = 'BRUTE_FORCE',
  ACCOUNT_LOCKED = 'ACCOUNT_LOCKED',
  MFA_BYPASS = 'MFA_BYPASS',
  PRIVILEGE_ESCALATION = 'PRIVILEGE_ESCALATION',
  SUSPICIOUS_ACTIVITY = 'SUSPICIOUS_ACTIVITY'
}

/**
 * 安全事件
 */
export interface SecurityEvent {
  id: number
  eventType: SecurityEventType
  severity: SecuritySeverity
  userId?: number
  username?: string
  ipAddress?: string
  deviceInfo?: string
  eventData?: Record<string, any>
  handled: boolean
  handleTime?: string
  createdAt: string
}

/**
 * 安全事件查询条件
 */
export interface SecurityEventQuery {
  eventType?: SecurityEventType
  severity?: SecuritySeverity
  username?: string
  ipAddress?: string
  handled?: boolean
  startTime?: string
  endTime?: string
  page?: number
  size?: number
}

/**
 * 安全统计
 */
export interface SecurityStatistics {
  totalEvents: number
  emergencyEvents: number
  highEvents: number
  mediumEvents: number
  lowEvents: number
  handledEvents: number
  unhandledEvents: number
  eventDistribution: Record<SecurityEventType, number>
  topThreatIPs: Array<{ ip: string; count: number }>
  topThreatUsers: Array<{ username: string; count: number }>
}
