import request from '@/utils/request'
import { PageResponse, Application, ApplicationCreate, ApplicationUpdate } from '@/types/application'

// 应用管理API
export const applicationApi = {
  // 创建应用
  createApplication: (data: ApplicationCreate): Promise<{ id: number }> => {
    return request.post('/api/applications', data)
  },

  // 更新应用
  updateApplication: (data: ApplicationUpdate): Promise<void> => {
    return request.put('/api/applications', data)
  },

  // 删除应用
  deleteApplication: (appId: number): Promise<void> => {
    return request.delete(`/api/applications/${appId}`)
  },

  // 获取应用详情
  getApplication: (appId: number): Promise<Application> => {
    return request.get(`/api/applications/${appId}`)
  },

  // 根据应用密钥获取应用
  getApplicationByAppKey: (appKey: string): Promise<Application> => {
    return request.get(`/api/applications/app-key/${appKey}`)
  },

  // 分页查询应用
  queryApplications: (params: {
    appName?: string
    appType?: number
    status?: number
    ownerId?: number
    page?: number
    size?: number
  }): Promise<PageResponse<Application>> => {
    return request.post('/api/applications/query', params)
  }
}
