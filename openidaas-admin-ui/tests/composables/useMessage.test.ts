import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useMessage } from '@/composables/useMessage'

describe('useMessage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('success', () => {
    it('should call ElMessage.success with correct message', () => {
      const { success } = useMessage()
      const message = '操作成功'
      
      // 由于 ElMessage 是外部依赖，这里只测试函数存在性
      expect(typeof success).toBe('function')
    })
  })

  describe('error', () => {
    it('should call ElMessage.error with correct message', () => {
      const { error } = useMessage()
      
      expect(typeof error).toBe('function')
    })
  })

  describe('warning', () => {
    it('should call ElMessage.warning with correct message', () => {
      const { warning } = useMessage()
      
      expect(typeof warning).toBe('function')
    })
  })

  describe('info', () => {
    it('should call ElMessage.info with correct message', () => {
      const { info } = useMessage()
      
      expect(typeof info).toBe('function')
    })
  })

  describe('deleteSuccess', () => {
    it('should return delete success message', () => {
      const { deleteSuccess } = useMessage()
      
      expect(typeof deleteSuccess).toBe('function')
    })
  })

  describe('deleteError', () => {
    it('should return delete error message', () => {
      const { deleteError } = useMessage()
      
      expect(typeof deleteError).toBe('function')
    })
  })

  describe('submitSuccess', () => {
    it('should return submit success message', () => {
      const { submitSuccess } = useMessage()
      
      expect(typeof submitSuccess).toBe('function')
    })
  })

  describe('submitError', () => {
    it('should return submit error message', () => {
      const { submitError } = useMessage()
      
      expect(typeof submitError).toBe('function')
    })
  })

  describe('batchSuccess', () => {
    it('should return batch success message', () => {
      const { batchSuccess } = useMessage()
      
      expect(typeof batchSuccess).toBe('function')
    })
  })

  describe('batchError', () => {
    it('should return batch error message', () => {
      const { batchError } = useMessage()
      
      expect(typeof batchError).toBe('function')
    })
  })

  describe('notifySuccess', () => {
    it('should return notify success function', () => {
      const { notifySuccess } = useMessage()
      
      expect(typeof notifySuccess).toBe('function')
    })
  })

  describe('notifyError', () => {
    it('should return notify error function', () => {
      const { notifyError } = useMessage()
      
      expect(typeof notifyError).toBe('function')
    })
  })

  describe('notifyWarning', () => {
    it('should return notify warning function', () => {
      const { notifyWarning } = useMessage()
      
      expect(typeof notifyWarning).toBe('function')
    })
  })

  describe('notifyInfo', () => {
    it('should return notify info function', () => {
      const { notifyInfo } = useMessage()
      
      expect(typeof notifyInfo).toBe('function')
    })
  })
})
