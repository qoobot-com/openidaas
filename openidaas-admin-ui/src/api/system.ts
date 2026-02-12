// 系统相关API
import request from '@/utils/request'

export const systemApi = {
  // 获取当前用户信息
  getCurrentUserInfo: (): Promise<any> => {
    return request.get('/api/auth/current-user')
  },

  // 获取系统配置
  getSystemConfig: (): Promise<any> => {
    return request.get('/api/system/config')
  },

  // 更新系统配置
  updateSystemConfig: (data: any): Promise<any> => {
    return request.put('/api/system/config', data)
  }
}