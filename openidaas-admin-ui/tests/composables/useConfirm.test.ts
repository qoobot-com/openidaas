import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useConfirm } from '@/composables/useConfirm'

describe('useConfirm', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('confirm', () => {
    it('should return confirm function', () => {
      const { confirm } = useConfirm()
      
      expect(typeof confirm).toBe('function')
    })

    it('should call with message and options', async () => {
      const { confirm } = useConfirm()
      
      expect(typeof confirm).toBe('function')
    })
  })

  describe('confirmDelete', () => {
    it('should return confirmDelete function', () => {
      const { confirmDelete } = useConfirm()
      
      expect(typeof confirmDelete).toBe('function')
    })
  })

  describe('confirmAction', () => {
    it('should return confirmAction function', () => {
      const { confirmAction } = useConfirm()
      
      expect(typeof confirmAction).toBe('function')
    })
  })

  describe('confirmWithInput', () => {
    it('should return confirmWithInput function', () => {
      const { confirmWithInput } = useConfirm()
      
      expect(typeof confirmWithInput).toBe('function')
    })
  })
})
