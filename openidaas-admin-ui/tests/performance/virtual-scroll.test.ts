import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import VirtualTable from '@/components/VirtualTable.vue'

describe('VirtualTable Performance', () => {
  it('should handle 10000 items efficiently', async () => {
    const items = Array.from({ length: 10000 }, (_, i) => ({
      id: i,
      name: `Item ${i}`,
      value: i * 10
    }))

    const startTime = performance.now()
    const wrapper = mount(VirtualTable, {
      props: { 
        items, 
        itemHeight: 50, 
        containerHeight: 500,
        bufferSize: 5 
      }
    })
    const renderTime = performance.now() - startTime

    expect(renderTime).toBeLessThan(500) // 初始渲染 < 500ms
    
    // 验证只渲染可见项目
    const visibleItems = wrapper.vm.visibleItems
    const expectedVisibleItems = Math.ceil(500 / 50) + 5 * 2 // 容器高度 + 缓冲区
    expect(visibleItems.length).toBeLessThanOrEqual(expectedVisibleItems)

    wrapper.unmount()
  })

  it('should handle 100000 items with virtual scrolling', async () => {
    const items = Array.from({ length: 100000 }, (_, i) => ({
      id: i,
      name: `Item ${i}`,
      value: i * 10
    }))

    const startTime = performance.now()
    const wrapper = mount(VirtualTable, {
      props: { 
        items, 
        itemHeight: 50, 
        containerHeight: 500,
        bufferSize: 10 
      }
    })
    const renderTime = performance.now() - startTime

    expect(renderTime).toBeLessThan(1000) // 初始渲染 < 1秒

    // 验证只渲染可见项目
    const visibleItems = wrapper.vm.visibleItems
    expect(visibleItems.length).toBeLessThan(200)

    wrapper.unmount()
  })

  it('should maintain performance when scrolling', async () => {
    const items = Array.from({ length: 10000 }, (_, i) => ({
      id: i,
      name: `Item ${i}`,
      value: i * 10
    }))

    const wrapper = mount(VirtualTable, {
      props: { 
        items, 
        itemHeight: 50, 
        containerHeight: 500,
        bufferSize: 10 
      }
    })

    // 模拟滚动
    for (let scrollPos = 0; scrollPos < 5000; scrollPos += 100) {
      const startTime = performance.now()
      await wrapper.vm.handleScroll({ target: { scrollTop: scrollPos } })
      const updateTime = performance.now() - startTime
      
      // 每次更新应该很快
      expect(updateTime).toBeLessThan(16) // < 16ms (60 FPS)
    }

    wrapper.unmount()
  })

  it('should not cause memory leaks with large datasets', async () => {
    const items = Array.from({ length: 50000 }, (_, i) => ({
      id: i,
      name: `Item ${i}`,
      value: i * 10
    }))

    const initialMemory = (performance as any).memory?.usedJSHeapSize || 0
    
    const wrapper = mount(VirtualTable, {
      props: { 
        items, 
        itemHeight: 50, 
        containerHeight: 500,
        bufferSize: 10 
      }
    })
    
    wrapper.unmount()
    
    // 等待垃圾回收
    await new Promise(resolve => setTimeout(resolve, 200))
    
    const finalMemory = (performance as any).memory?.usedJSHeapSize || 0
    const memoryIncrease = finalMemory - initialMemory

    // 内存增长应该小于 5MB
    expect(memoryIncrease).toBeLessThan(5 * 1024 * 1024)
  })
})
