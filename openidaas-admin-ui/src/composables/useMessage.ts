import { ElMessage, ElNotification } from 'element-plus'

interface MessageOptions {
  message: string
  duration?: number
  showClose?: boolean
}

interface NotificationOptions {
  title?: string
  message: string
  duration?: number
  position?: 'top-right' | 'top-left' | 'bottom-right' | 'bottom-left'
  type?: 'success' | 'warning' | 'info' | 'error'
}

export function useMessage() {
  const success = (options: string | MessageOptions) => {
    const config = typeof options === 'string' ? { message: options } : options
    return ElMessage.success({
      duration: 3000,
      showClose: true,
      ...config
    })
  }

  const error = (options: string | MessageOptions) => {
    const config = typeof options === 'string' ? { message: options } : options
    return ElMessage.error({
      duration: 5000,
      showClose: true,
      ...config
    })
  }

  const warning = (options: string | MessageOptions) => {
    const config = typeof options === 'string' ? { message: options } : options
    return ElMessage.warning({
      duration: 4000,
      showClose: true,
      ...config
    })
  }

  const info = (options: string | MessageOptions) => {
    const config = typeof options === 'string' ? { message: options } : options
    return ElMessage.info({
      duration: 3000,
      showClose: true,
      ...config
    })
  }

  const notifySuccess = (options: string | NotificationOptions) => {
    const config = typeof options === 'string' ? { message: options } : options
    return ElNotification.success({
      duration: 4500,
      position: 'top-right',
      ...config
    })
  }

  const notifyError = (options: string | NotificationOptions) => {
    const config = typeof options === 'string' ? { message: options } : options
    return ElNotification.error({
      duration: 6000,
      position: 'top-right',
      ...config
    })
  }

  const notifyWarning = (options: string | NotificationOptions) => {
    const config = typeof options === 'string' ? { message: options } : options
    return ElNotification.warning({
      duration: 5000,
      position: 'top-right',
      ...config
    })
  }

  const notifyInfo = (options: string | NotificationOptions) => {
    const config = typeof options === 'string' ? { message: options } : options
    return ElNotification.info({
      duration: 4500,
      position: 'top-right',
      ...config
    })
  }

  // 批量操作提示
  const batchSuccess = (successCount: number, totalCount: number) => {
    if (successCount === totalCount) {
      return success(`成功完成 ${totalCount} 项操作`)
    } else {
      return warning(`部分成功：${successCount}/${totalCount} 项操作完成`)
    }
  }

  const batchError = (errorCount: number, totalCount: number) => {
    return error(`${errorCount}/${totalCount} 项操作失败`)
  }

  // 表单提交提示
  const submitSuccess = (action: string = '提交') => {
    return success(`${action}成功`)
  }

  const submitError = (action: string = '提交', errorMessage?: string) => {
    return error(errorMessage ? `${action}失败: ${errorMessage}` : `${action}失败`)
  }

  // 删除操作提示
  const deleteSuccess = () => success('删除成功')
  const deleteError = (errorMessage?: string) => error(errorMessage || '删除失败')

  // 加载提示
  const loading = (message: string = '加载中...') => ElMessage.info(message)

  return {
    success,
    error,
    warning,
    info,
    notifySuccess,
    notifyError,
    notifyWarning,
    notifyInfo,
    batchSuccess,
    batchError,
    submitSuccess,
    submitError,
    deleteSuccess,
    deleteError,
    loading
  }
}

export default useMessage
