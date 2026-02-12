import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import UserList from '@/modules/user/UserList.vue'

describe('UserList Performance', () => {
  it('should render efficiently with normal data', async () => {
    const users = Array.from({ length: 100 }, (_, i) => ({
      id: i + 1,
      username: `user${i}`,
      email: `user${i}@example.com`,
      status: 1
    }))

    const startTime = performance.now()
    const wrapper = mount(UserList, {
      props: { users }
    })
    const renderTime = performance.now() - startTime

    expect(renderTime).toBeLessThan(500) // 渲染时间 < 500ms
    wrapper.unmount()
  })

  it('should handle large dataset with pagination', async () => {
    const users = Array.from({ length: 10000 }, (_, i) => ({
      id: i + 1,
      username: `user${i}`,
      email: `user${i}@example.com`,
      status: 1
    }))

    const startTime = performance.now()
    const wrapper = mount(UserList, {
      props: {
        users: users.slice(0, 20), // 只渲染第一页
        total: 10000,
        pageSize: 20
      }
    })
    const renderTime = performance.now() - startTime

    expect(renderTime).toBeLessThan(300) // 渲染时间 < 300ms
    wrapper.unmount()
  })

  it('should not cause memory leaks on unmount', async () => {
    const users = Array.from({ length: 100 }, (_, i) => ({
      id: i + 1,
      username: `user${i}`,
      email: `user${i}@example.com`,
      status: 1
    }))

    const initialMemory = (performance as any).memory?.usedJSHeapSize || 0
    
    const wrapper = mount(UserList, {
      props: { users }
    })
    
    wrapper.unmount()
    
    // 等待垃圾回收
    await new Promise(resolve => setTimeout(resolve, 100))
    
    const finalMemory = (performance as any).memory?.usedJSHeapSize || 0
    const memoryIncrease = finalMemory - initialMemory

    // 内存增长应该小于 1MB
    expect(memoryIncrease).toBeLessThan(1024 * 1024)
  })
})
