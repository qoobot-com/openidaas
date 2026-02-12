import { ElMessageBox } from 'element-plus'

interface ConfirmOptions {
  title?: string
  message: string
  confirmButtonText?: string
  cancelButtonText?: string
  type?: 'success' | 'warning' | 'info' | 'error'
  showInput?: boolean
  inputValue?: string
  inputPlaceholder?: string
  inputValidator?: (value: string) => boolean | string
  inputPattern?: RegExp
}

export function useConfirm() {
  const confirm = (options: ConfirmOptions): Promise<string | null> => {
    const {
      title = '提示',
      message,
      confirmButtonText = '确定',
      cancelButtonText = '取消',
      type = 'warning',
      showInput = false,
      inputValue = '',
      inputPlaceholder = '',
      inputValidator,
      inputPattern
    } = options

    return ElMessageBox.confirm(message, title, {
      confirmButtonText,
      cancelButtonText,
      type,
      showInput,
      inputValue,
      inputPlaceholder,
      inputValidator,
      inputPattern,
      customClass: 'custom-confirm-dialog'
    }).then(() => {
      return inputValue
    }).catch(() => {
      return null
    })
  }

  const confirmDelete = (itemName: string, itemNameDisplay = itemName): Promise<boolean> => {
    return confirm({
      title: '确认删除',
      message: `确定要删除 "${itemNameDisplay}" 吗？此操作不可恢复！`,
      type: 'error'
    }).then(() => true)
  }

  const confirmAction = (actionName: string, itemName = ''): Promise<boolean> => {
    const message = itemName ? `确定要${actionName} "${itemName}" 吗？` : `确定要${actionName}吗？`
    return confirm({
      title: '确认操作',
      message,
      type: 'warning'
    }).then(() => true)
  }

  const confirmAsync = async (options: ConfirmOptions, callback?: () => Promise<void>): Promise<boolean> => {
    const result = await confirm(options)
    if (result !== null && callback) {
      await callback()
    }
    return result !== null
  }

  const confirmWithInput = (
    options: Omit<ConfirmOptions, 'showInput'> & { inputPlaceholder?: string }
  ): Promise<{ confirmed: boolean; inputValue: string }> => {
    return confirm({
      ...options,
      showInput: true,
      inputPlaceholder: options.inputPlaceholder || '请输入内容'
    }).then((value) => ({
      confirmed: true,
      inputValue: value || ''
    })).catch(() => ({
      confirmed: false,
      inputValue: ''
    }))
  }

  return {
    confirm,
    confirmDelete,
    confirmAction,
    confirmAsync,
    confirmWithInput
  }
}

export default useConfirm
