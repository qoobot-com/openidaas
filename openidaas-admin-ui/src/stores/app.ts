import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 状态
  const sidebar = ref({
    opened: true,
    withoutAnimation: false
  })
  const device = ref('desktop')

  // Actions
  const toggleSideBar = () => {
    sidebar.value.opened = !sidebar.value.opened
    sidebar.value.withoutAnimation = false
  }

  const closeSideBar = (withoutAnimation: boolean) => {
    sidebar.value.opened = false
    sidebar.value.withoutAnimation = withoutAnimation
  }

  const toggleDevice = (deviceType: string) => {
    device.value = deviceType
  }

  // 标签页相关方法（委托给 tagsView store）
  const addVisitedView = (_view: any) => {
    // 这些方法应该在 tagsView store 中实现
    console.warn('addVisitedView should be called on tagsView store')
  }

  const addCachedView = (_view: any) => {
    // 这些方法应该在 tagsView store 中实现
    console.warn('addCachedView should be called on tagsView store')
  }

  return {
    // 状态
    sidebar,
    device,
    
    // Actions
    toggleSideBar,
    closeSideBar,
    toggleDevice,
    addVisitedView,
    addCachedView
  }
})