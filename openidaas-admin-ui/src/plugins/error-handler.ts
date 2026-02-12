import { App, Plugin } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'

// 错误类型定义
export interface AppError extends Error {
  code?: string
  status?: number
  data?: any
}

// 错误处理配置
interface ErrorHandlerConfig {
  showMessage?: boolean
  showNotification?: boolean
  logError?: boolean
  customHandler?: (error: AppError) => void
}

const defaultConfig: ErrorHandlerConfig = {
  showMessage: true,
  showNotification: false,
  logError: true,
  customHandler: undefined
}

// 全局错误处理类
class ErrorHandler {
  private config: ErrorHandlerConfig

  constructor(config: ErrorHandlerConfig = {}) {
    this.config = { ...defaultConfig, ...config }
  }

  // 处理错误
  handleError(error: AppError, context?: string): void {
    // 自定义处理
    if (this.config.customHandler) {
      this.config.customHandler(error)
      return
    }

    // 日志记录
    if (this.config.logError) {
      console.error(`[${context || 'App'}] Error:`, error)
    }

    // 根据错误类型处理
    const errorMessage = this.getErrorMessage(error)
    
    if (this.config.showNotification) {
      ElNotification.error({
        title: '系统错误',
        message: errorMessage,
        duration: 5000
      })
    } else if (this.config.showMessage) {
      ElMessage.error(errorMessage)
    }
  }

  // 获取友好的错误消息
  private getErrorMessage(error: AppError): string {
    if (error.code) {
      switch (error.code) {
        case 'NETWORK_ERROR':
          return '网络连接失败，请检查网络设置'
        case 'TIMEOUT_ERROR':
          return '请求超时，请稍后重试'
        case 'UNAUTHORIZED':
          return '未授权访问，请重新登录'
        case 'FORBIDDEN':
          return '权限不足，无法执行此操作'
        case 'NOT_FOUND':
          return '请求的资源不存在'
        case 'SERVER_ERROR':
          return '服务器内部错误，请联系管理员'
        default:
          return error.message || '操作失败'
      }
    }
    
    return error.message || '未知错误'
  }

  // 设置配置
  setConfig(config: Partial<ErrorHandlerConfig>): void {
    this.config = { ...this.config, ...config }
  }
}

// 创建全局错误处理实例
const errorHandler = new ErrorHandler()

// Vue插件
const errorHandlerPlugin: Plugin = {
  install(app: App) {
    // 全局错误处理
    app.config.errorHandler = (err, _instance, info) => {
      errorHandler.handleError(err as AppError, info)
    }

    // 全局Promise拒绝处理
    window.addEventListener('unhandledrejection', (event) => {
      errorHandler.handleError(event.reason as AppError, 'Unhandled Promise Rejection')
      event.preventDefault()
    })

    // 全局错误处理方法
    app.config.globalProperties.$handleError = (error: AppError, context?: string) => {
      errorHandler.handleError(error, context)
    }

    // 提供错误处理实例
    app.provide('errorHandler', errorHandler)
  }
}

// 组合式函数
export const useErrorHandler = () => {
  const handleError = (error: AppError, context?: string) => {
    errorHandler.handleError(error, context)
  }

  const setConfig = (config: Partial<ErrorHandlerConfig>) => {
    errorHandler.setConfig(config)
  }

  return {
    handleError,
    setConfig
  }
}

export default errorHandlerPlugin