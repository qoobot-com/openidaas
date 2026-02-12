import request from '@/utils/request'
import {
  PageResponse,
  AuditLog,
  AuditLogQuery,
  AuditStatistics,
  AuditLogCreate,
  SecurityEvent,
  SecurityEventQuery,
  SecurityStatistics
} from '@/types/audit'

// 审计日志API
export const auditApi = {
  // 记录审计日志
  createAuditLog: (data: AuditLogCreate): Promise<void> => {
    return request.post('/api/audit/logs', data)
  },

  // 批量记录审计日志
  batchCreateAuditLogs: (data: AuditLogCreate[]): Promise<void> => {
    return request.post('/api/audit/logs/batch', data)
  },

  // 异步记录审计日志
  createAuditLogAsync: (data: AuditLogCreate): Promise<void> => {
    return request.post('/api/audit/logs/async', data)
  },

  // 获取审计日志详情
  getAuditLog: (logId: number): Promise<AuditLog> => {
    return request.get(`/api/audit/logs/${logId}`)
  },

  // 分页查询审计日志
  queryAuditLogs: (query: AuditLogQuery): Promise<PageResponse<AuditLog>> => {
    return request.post('/api/audit/logs/query', query)
  },

  // 获取用户操作日志
  getUserAuditLogs: (userId: number, page: number = 1, size: number = 20): Promise<PageResponse<AuditLog>> => {
    return request.get(`/api/audit/logs/users/${userId}`, {
      params: { page, size }
    })
  },

  // 获取租户操作日志
  getTenantAuditLogs: (tenantId: number, page: number = 1, size: number = 20): Promise<PageResponse<AuditLog>> => {
    return request.get(`/api/audit/logs/tenants/${tenantId}`, {
      params: { page, size }
    })
  },

  // 获取应用操作日志
  getAppAuditLogs: (appId: number, page: number = 1, size: number = 20): Promise<PageResponse<AuditLog>> => {
    return request.get(`/api/audit/logs/apps/${appId}`, {
      params: { page, size }
    })
  },

  // 按操作类型获取审计日志
  getLogsByOperationType: (operationType: string, page: number = 1, size: number = 20): Promise<PageResponse<AuditLog>> => {
    return request.get(`/api/audit/logs/operation-type/${operationType}`, {
      params: { page, size }
    })
  },

  // 按模块获取审计日志
  getLogsByModule: (module: string, page: number = 1, size: number = 20): Promise<PageResponse<AuditLog>> => {
    return request.get(`/api/audit/logs/module/${module}`, {
      params: { page, size }
    })
  },

  // 按时间范围获取审计日志
  getLogsByTimeRange: (startTime: string, endTime: string, page: number = 1, size: number = 20): Promise<PageResponse<AuditLog>> => {
    return request.get('/api/audit/logs/time-range', {
      params: { startTime, endTime, page, size }
    })
  },

  // 获取失败的操作日志
  getFailedLogs: (page: number = 1, size: number = 20): Promise<PageResponse<AuditLog>> => {
    return request.get('/api/audit/logs/failed', {
      params: { page, size }
    })
  },

  // 获取最近的操作日志
  getRecentLogs: (page: number = 1, size: number = 20): Promise<PageResponse<AuditLog>> => {
    return request.get('/api/audit/logs/recent', {
      params: { page, size }
    })
  },

  // 获取审计统计数据
  getStatistics: (startTime: string, endTime: string): Promise<AuditStatistics> => {
    return request.get('/api/audit/statistics', {
      params: { startTime, endTime }
    })
  },

  // 统计操作次数
  countOperations: (startTime: string, endTime: string): Promise<number> => {
    return request.get('/api/audit/statistics/count', {
      params: { startTime, endTime }
    })
  },

  // 统计用户操作次数
  countUserOperations: (userId: number): Promise<number> => {
    return request.get(`/api/audit/statistics/users/${userId}/count`)
  },

  // 统计模块操作分布
  getModuleDistribution: (startTime: string, endTime: string): Promise<any[]> => {
    return request.get('/api/audit/statistics/module-distribution', {
      params: { startTime, endTime }
    })
  },

  // 统计操作类型分布
  getOperationTypeDistribution: (startTime: string, endTime: string): Promise<any[]> => {
    return request.get('/api/audit/statistics/operation-type-distribution', {
      params: { startTime, endTime }
    })
  },

  // 清理过期审计日志
  cleanupExpiredLogs: (beforeTime: string): Promise<number> => {
    return request.delete('/api/audit/logs/cleanup', {
      params: { beforeTime }
    })
  },

  // 导出审计日志
  exportAuditLogs: (startTime: string, endTime: string): Promise<Blob> => {
    return request.get('/api/audit/logs/export', {
      params: { startTime, endTime },
      responseType: 'blob'
    })
  },

  // 删除审计日志
  deleteAuditLog: (logId: number): Promise<void> => {
    return request.delete(`/api/audit/logs/${logId}`)
  },

  // 批量删除审计日志
  batchDeleteAuditLogs: (logIds: number[]): Promise<void> => {
    return request.delete('/api/audit/logs/batch', {
      data: logIds
    })
  }
}

// 安全事件API
export const securityEventApi = {
  // 查询安全事件
  querySecurityEvents: (query: SecurityEventQuery): Promise<PageResponse<SecurityEvent>> => {
    return request.post('/api/security/events/query', query)
  },

  // 获取安全事件详情
  getSecurityEvent: (eventId: number): Promise<SecurityEvent> => {
    return request.get(`/api/security/events/${eventId}`)
  },

  // 标记安全事件为已处理
  markAsHandled: (eventId: number): Promise<void> => {
    return request.put(`/api/security/events/${eventId}/handle`)
  },

  // 批量标记安全事件为已处理
  batchMarkAsHandled: (eventIds: number[]): Promise<void> => {
    return request.put('/api/security/events/batch-handle', {
      data: eventIds
    })
  },

  // 获取安全统计
  getSecurityStatistics: (startTime: string, endTime: string): Promise<SecurityStatistics> => {
    return request.get('/api/security/statistics', {
      params: { startTime, endTime }
    })
  },

  // 获取未处理的安全事件
  getUnhandledEvents: (page: number = 1, size: number = 20): Promise<PageResponse<SecurityEvent>> => {
    return request.get('/api/security/events/unhandled', {
      params: { page, size }
    })
  },

  // 按严重程度获取安全事件
  getEventsBySeverity: (severity: number, page: number = 1, size: number = 20): Promise<PageResponse<SecurityEvent>> => {
    return request.get(`/api/security/events/severity/${severity}`, {
      params: { page, size }
    })
  },

  // 按用户获取安全事件
  getEventsByUser: (userId: number, page: number = 1, size: number = 20): Promise<PageResponse<SecurityEvent>> => {
    return request.get(`/api/security/events/users/${userId}`, {
      params: { page, size }
    })
  },

  // 按IP地址获取安全事件
  getEventsByIP: (ipAddress: string, page: number = 1, size: number = 20): Promise<PageResponse<SecurityEvent>> => {
    return request.get('/api/security/events/ip', {
      params: { ipAddress, page, size }
    })
  },

  // 导出安全事件报告
  exportSecurityReport: (startTime: string, endTime: string): Promise<Blob> => {
    return request.get('/api/security/events/export', {
      params: { startTime, endTime },
      responseType: 'blob'
    })
  }
}
